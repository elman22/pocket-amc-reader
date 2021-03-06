/*
    This file is part of Pocket AMC Reader.
    Copyright © 2010-2020 Elman <holdingscythe@zoznam.sk>

    Pocket AMC Reader is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    Pocket AMC Reader is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with Pocket AMC Reader.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.holdingscythe.pocketamcreader;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.util.Log;

import com.holdingscythe.pocketamcreader.catalog.MoviesDataProvider;
import com.holdingscythe.pocketamcreader.catalog.MoviesSAXHandler;
import com.holdingscythe.pocketamcreader.settings.SettingsConstants;
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
import java.util.Objects;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

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
    interface TaskCallbacks {
        void onPreExecute();

        void onProgressUpdate(int percent);

        void onPostExecute();
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
    public void onAttach(@NonNull Activity activity) {
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
        private long bytesToBeImported;

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
            PowerManager.WakeLock wakeLock = null;

            long startTime;
            long endTime;

            File sourceCatalog = null;
            InputStream sourceCatalogStream = null;
            long fileSizeToBeImported = 0;
            String settingPicturesFolder = "";

            // Read settings for import
            if (SharedObjects.getInstance().preferences == null) {
                // Prepare Shared Objects
                SharedObjects.getInstance().preferences = PreferenceManager.getDefaultSharedPreferences
                        (Objects.requireNonNull(getActivity()).getApplicationContext());
            }

            SharedPreferences preferences = SharedObjects.getInstance().preferences;
            String settingCatalogLocation = preferences.getString(SettingsConstants.KEY_PREF_CATALOG_LOCATION, "");
            String settingCatalogEncoding = preferences.getString(SettingsConstants.KEY_PREF_CATALOG_ENCODING,
                    "Cp1252");
            Boolean settingRemoveBadChars = preferences.getBoolean(SettingsConstants.KEY_PREF_REMOVE_BAD_CHARS, false);
            long settingLastImportedSize = preferences.getLong(SettingsConstants.KEY_PREF_LAST_IMPORTED_SIZE, 0);

            // Get partial wake lock
            PowerManager pm = (PowerManager) mContext.getSystemService(Context.POWER_SERVICE);
            if (pm != null) {
                wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, S.WAKE_LOCK_TAG);
                wakeLock.acquire(10 * 60 * 1000L /*10 minutes*/);

                if (S.DEBUG)
                    Log.d(S.TAG, "Wake Lock acquired.");
            }

            // Open file for size check
            try {
                // Fix for Windows backslashes
                assert settingCatalogLocation != null;
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

            // Convert file to UTF-8
            if (isImportFileReady) {
                publishProgress(S.IMPORT_CONVERSION_START);

                try {
                    String convertedCatalog = FileEncoder.encodeToUtf(mContext, settingCatalogLocation,
                            S.CATALOG_TMP_FILENAME, settingCatalogEncoding, settingRemoveBadChars);
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

            // Import data
            if (isImportFileConverted) {
                publishProgress(S.IMPORT_LOADING_START);

                try {
                    // Get a SAXParser from the SAXParserFactory.
                    SAXParserFactory sFactory = SAXParserFactory.newInstance();
                    SAXParser sParser = sFactory.newSAXParser();

                    // Get the XMLReader of the SAXParser we created.
                    XMLReader sReader = sParser.getXMLReader();

                    // Create a new ContentHandler and apply it to the XML-Reader
                    MoviesSAXHandler sHandler = new MoviesSAXHandler(moviesDataProvider);
                    sReader.setContentHandler(sHandler);
                    sReader.setErrorHandler(sHandler);

                    // Parse the XML-data from our URL.
                    InputSource sInput = new InputSource(sourceCatalogStream);
                    sInput.setEncoding("UTF-8");

                    startTime = System.currentTimeMillis();
                    if (S.DEBUG) {
                        Log.d(S.TAG, "Import start time: " + startTime);
                    }

                    // Drop indexes for faster import
                    moviesDataProvider.dropIndexes();

                    // Do actual import
                    sReader.parse(sInput);

                    // Recreate indexes
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
                    editor.putLong(SettingsConstants.KEY_PREF_LAST_IMPORTED_SIZE, 0);
                    editor.apply();

                    if (S.ERROR)
                        Log.e(S.TAG, "Couldn't import XML file.");

                    publishProgress(S.IMPORT_ERROR_LOADING);
                }

                // Update preferences for imported data
                if (isImportFileFinished) {
                    try {
                        SharedPreferences.Editor editor = preferences.edit();
                        editor.putLong(SettingsConstants.KEY_PREF_LAST_IMPORTED_SIZE, fileSizeToBeImported);
                        editor.putString(SettingsConstants.KEY_PREF_LAST_IMPORTED_DATE, new Date().toString());
                        editor.putString(SettingsConstants.KEY_PREF_PICTURES_FOLDER, settingPicturesFolder);
                        editor.apply();
                    } catch (Exception e) {
                        if (S.ERROR)
                            Log.e(S.TAG, "Couldn't open update import statistics.");
                    }
                }
            }

            // Do some cleaning
            if (wakeLock != null) {
                wakeLock.release();

                if (S.DEBUG)
                    Log.d(S.TAG, "Wake Lock released.");
            }

            moviesDataProvider.closeDatabase();

            return null;
        }

        /* Publish state from external sources e.g. from {@link ProgressFilterInputStream} */
        public void publishExternalProgress(long state) {
            if (S.VERBOSE)
                Log.v(S.TAG, "Publishing state: " + (int) ((float) state / bytesToBeImported * 100) +
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