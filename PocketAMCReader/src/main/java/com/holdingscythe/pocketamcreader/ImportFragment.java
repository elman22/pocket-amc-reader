package com.holdingscythe.pocketamcreader;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.PowerManager;
import android.support.v4.app.Fragment;
import android.util.Log;

import com.holdingscythe.pocketamcreader.catalog.MoviesDataProvider;
import com.holdingscythe.pocketamcreader.catalog.MoviesSAXHandler;
import com.holdingscythe.pocketamcreader.utils.FileEncoder;
import com.holdingscythe.pocketamcreader.utils.ProgressFilterInputStream;
import com.holdingscythe.pocketamcreader.utils.SharedObjects;

import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Date;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

/**
 * This Fragment manages a single background task and retains itself across
 * configuration changes.
 */
public class ImportFragment extends Fragment {
    private TaskCallbacks mTaskCallbacks = sDummyTaskCallbacks;
    private Context mContext;
    private boolean mRunning;

    /**
     * Callback interface through which the fragment can report the task's
     * progress and results back to the Activity.
     */
    static interface TaskCallbacks {
        public void onPreExecute();

        public void onProgressUpdate(int percent);

        public void onPostExecute();
    }

    /**
     * A dummy implementation of the {@link TaskCallbacks} interface that does
     * nothing. Used only when this fragment is not attached to an activity.
     */
    private static TaskCallbacks sDummyTaskCallbacks = new TaskCallbacks() {
        @Override
        public void onPreExecute() {
        }

        @Override
        public void onProgressUpdate(int percent) {
        }

        @Override
        public void onPostExecute() {
        }
    };

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public ImportFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    /**
     * Android passes us a reference to the newly created Activity by calling this
     * method after each configuration change.
     */
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        if (!(activity instanceof TaskCallbacks)) {
            throw new IllegalStateException("Activity must implement the TaskCallbacks interface.");
        }

        // Hold a reference to the parent Activity so we can report back the task's current
        // progress and results.
        mTaskCallbacks = (TaskCallbacks) activity;
    }

    @Override
    public void onDetach() {
        super.onDetach();

        // Reset the active callbacks interface to the dummy implementation.
        mTaskCallbacks = sDummyTaskCallbacks;
    }

    /**
     * Start the background task.
     * Since async task can start without activity, context must be passed.
     */
    public void start(Context c) {
        if (!mRunning) {
            mContext = c;
            CatalogImportTask catalogImportTask = new CatalogImportTask();
            catalogImportTask.execute();
            mRunning = true;
        }
    }

    /**
     * A task that performs background catalog import and proxies progress
     * updates and results back to the Activity.
     */
    public class CatalogImportTask extends AsyncTask<Void, Integer, Void> {
        protected long bytesToBeImported;

        public CatalogImportTask() {
        }

        @Override
        protected void onPreExecute() {
            // Proxy the call to the Activity
            mTaskCallbacks.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... ignore) {
            MoviesDataProvider moviesDataProvider = new MoviesDataProvider(mContext);

            boolean isImportFileReady = false;
            boolean isImportFileConverted = false;
            boolean isImportFileFinished = false;

            long startTime;
            long endTime;

            File sourceCatalog = null;
            InputStream sourceCatalogStream = null;
            long fileSizeToBeImported = 0;
            String settingPicturesFolder = "";

            /** Read settings for import */
            SharedPreferences preferences = SharedObjects.getInstance().preferences;
            String settingCatalogLocation = preferences.getString("settingCatalogLocation", "");
            String settingCatalogEncoding = preferences.getString("settingCatalogEncoding", "Cp1252");
            long settingLastImportedSize = preferences.getLong("settingLastImportedSize", 0);

            /** Get partial wake lock */
            PowerManager pm = (PowerManager) mContext.getSystemService(Context.POWER_SERVICE);
            PowerManager.WakeLock wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, S.TAG);
            wakeLock.acquire();

            if (S.DEBUG)
                Log.d(S.TAG, "Wake Lock acquired.");

            /** Open file for size check */
            try {
                // Fix for Windows backslashes
                settingCatalogLocation = settingCatalogLocation.replace("\\", "/");
                sourceCatalog = new File(settingCatalogLocation);

                if (!sourceCatalog.exists())
                    throw new FileNotFoundException();
                if (sourceCatalog.length() != settingLastImportedSize) {
                    fileSizeToBeImported = sourceCatalog.length();
                    settingPicturesFolder = sourceCatalog.getParent() + "/";
                    isImportFileReady = true;
                }
            } catch (FileNotFoundException e) {
                if (S.WARN)
                    Log.w(S.TAG, "Couldn't open catalog for size check.");
            } catch (Exception e) {
                if (S.WARN)
                    Log.w(S.TAG, "Error processing catalog for conversion.");
            }

            /** Convert file to UTF-8 */
            if (isImportFileReady) {
                publishProgress(S.IMPORT_CONVERSION_START);

                try {
                    String convertedCatalog = FileEncoder.encodeToUtf(mContext, settingCatalogLocation,
                            S.CATALOG_TMP_FILENAME, settingCatalogEncoding);
                    if (convertedCatalog == null)
                        throw new Exception();
                    sourceCatalog = new File(convertedCatalog);
                    bytesToBeImported = sourceCatalog.length();
                    sourceCatalogStream = new ProgressFilterInputStream(new FileInputStream(sourceCatalog), this);
                    isImportFileConverted = true;
                } catch (FileNotFoundException e) {
                    if (S.ERROR)
                        Log.e(S.TAG, "Couldn't open converted temporary file.");

                    publishProgress(S.IMPORT_ERROR_CONVERSION);
                } catch (IllegalArgumentException e) {
                    // File is too large for progress bar
                    if (S.ERROR)
                        Log.e(S.TAG, "File is too large for progress bar.");
                } catch (Exception e) {
                    if (S.ERROR)
                        Log.e(S.TAG, "Couldn't create temporary file.");

                    publishProgress(S.IMPORT_ERROR_CONVERSION);
                }
            }

            /** Import data */
            if (isImportFileConverted) {
                publishProgress(S.IMPORT_LOADING_START);

                try {
                    /* Get a SAXParser from the SAXParserFactory. */
                    SAXParserFactory sFactory = SAXParserFactory.newInstance();
                    SAXParser sParser = sFactory.newSAXParser();

				    /* Get the XMLReader of the SAXParser we created. */
                    XMLReader sReader = sParser.getXMLReader();

				    /* Create a new ContentHandler and apply it to the XML-Reader */
                    MoviesSAXHandler sHandler = new MoviesSAXHandler(moviesDataProvider);
                    sReader.setContentHandler(sHandler);
                    sReader.setErrorHandler(sHandler);

				    /* Parse the XML-data from our URL. */
                    InputSource sInput = new InputSource(sourceCatalogStream);
                    sInput.setEncoding("UTF-8");

                    startTime = System.currentTimeMillis();
                    if (S.DEBUG) {
                        Log.d(S.TAG, "Import start time: " + startTime);
                    }

				    /* Drop indexes for faster import */
                    moviesDataProvider.dropIndexes();

				    /* Do actual import */
                    sReader.parse(sInput);

				    /* Recreate indexes */
                    publishProgress(S.IMPORT_INDEXING_START);
                    moviesDataProvider.createIndexes();

                    endTime = System.currentTimeMillis();
                    if (S.DEBUG) {
                        Log.d(S.TAG, "Import end time: " + endTime);
                        Log.d(S.TAG, "Import elapsed time: " + (endTime - startTime) + " ms.");
                    }

                    sourceCatalogStream.close();
                    if (!sourceCatalog.delete()) {
                        if (S.ERROR)
                            Log.e(S.TAG, "Couldn't create temporary file.");
                    }

                    isImportFileFinished = true;
                } catch (Exception e) {
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putLong("settingLastImportedSize", 0);
                    editor.apply();

                    if (S.ERROR)
                        Log.e(S.TAG, "Couldn't import XML file.");

                    publishProgress(S.IMPORT_ERROR_LOADING);
                }

			    /* Update preferences for imported data */
                if (isImportFileFinished) {
                    try {
                        SharedPreferences.Editor editor = preferences.edit();
                        editor.putLong("settingLastImportedSize", fileSizeToBeImported);
                        editor.putString("settingLastImportedDate", new Date().toString());
                        editor.putString("settingPicturesFolder", settingPicturesFolder);
                        editor.apply();
                    } catch (Exception e) {
                        if (S.ERROR)
                            Log.e(S.TAG, "Couldn't open update import statistics.");
                    }
                }
            }

            /** Do some cleaning */
            wakeLock.release();
            if (S.DEBUG)
                Log.d(S.TAG, "Wake Lock released.");

            moviesDataProvider.closeDatabase();

            return null;
        }

        /* Publish state from external sources e.g. from {@link ProgressFilterInputStream} */
        public void publishExternalProgress(long state) {
            if (S.VERBOSE)
                Log.v(S.TAG, "Publishing state: " + String.valueOf((int) ((float) state / bytesToBeImported * 100)) +
                        " %");

            publishProgress((int) ((float) state / bytesToBeImported * 100));
        }

        @Override
        protected void onProgressUpdate(Integer... state) {
            // Proxy the call to the Activity
            mTaskCallbacks.onProgressUpdate(state[0]);
        }

        @Override
        protected void onPostExecute(Void ignore) {
            // Proxy the call to the Activity
            mTaskCallbacks.onPostExecute();
            mRunning = false;
        }
    }
}