/*
    This file is part of Pocket AMC Reader.
    Copyright © 2010-2026 Elman <holdingscythe@zoznam.sk>

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

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ServiceInfo;
import android.net.Uri;
import android.os.Build;
import android.util.Log;

import com.holdingscythe.pocketamcreader.catalog.CatalogFiles;
import com.holdingscythe.pocketamcreader.catalog.MoviesDataProvider;
import com.holdingscythe.pocketamcreader.catalog.MoviesSAXHandler;
import com.holdingscythe.pocketamcreader.settings.SettingsConstants;
import com.holdingscythe.pocketamcreader.utils.CatalogReader;
import com.holdingscythe.pocketamcreader.utils.ProgressFilterInputStream;
import com.holdingscythe.pocketamcreader.utils.SafHelper;
import com.holdingscythe.pocketamcreader.utils.SharedObjects;

import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

import java.io.BufferedInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.InterruptedIOException;
import java.io.Reader;
import java.nio.charset.Charset;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.Set;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.documentfile.provider.DocumentFile;
import androidx.preference.PreferenceManager;
import androidx.work.Data;
import androidx.work.ForegroundInfo;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

/**
 * Imports the catalog in the background: checks whether the catalog changed, loads the XML into the database
 * (in one transaction) and copies new pictures to app storage.
 * <p>
 * Runs as WorkManager work promoted to a foreground service while it is really importing, so it survives the app
 * going to the background. Nothing is shown when the catalog is up to date.
 */
public class CatalogImportWorker extends Worker {

    public static final String WORK_NAME = "catalog-import";
    public static final String KEY_PHASE = "phase";
    public static final String KEY_PERCENT = "percent";
    public static final String KEY_ERROR = "error";

    private static final String CHANNEL_ID = "catalog_import";
    private static final int NOTIFICATION_ID = 4711;
    private static final int BUFFER_SIZE = 64 * 1024;

    private boolean mForeground = false;
    private int mPhase = 0;
    private int mPercent = -1;

    public CatalogImportWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
    }

    @NonNull
    @Override
    public Result doWork() {
        Context context = getApplicationContext();

        // The worker can run in a fresh process without any Activity having been created
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedObjects.getInstance().preferences = preferences;

        String location = preferences.getString(SettingsConstants.KEY_PREF_CATALOG_LOCATION, "");
        if (!SafHelper.isContentUri(location)) {
            // Nothing selected yet, or a plain path selected by an older version (not accessible anymore)
            return failure(S.IMPORT_ERROR_NOT_CONFIGURED);
        }

        Uri catalogUri = Uri.parse(location);
        String treeString = preferences.getString(SettingsConstants.KEY_PREF_CATALOG_TREE, "");
        Uri treeUri = SafHelper.isContentUri(treeString) ? Uri.parse(treeString) : null;

        if (!SafHelper.hasReadPermission(context, catalogUri, treeUri))
            return failure(S.IMPORT_ERROR_ACCESS);

        DocumentFile catalog = DocumentFile.fromSingleUri(context, catalogUri);
        if (catalog == null || !catalog.isFile() || !catalog.canRead())
            return failure(S.IMPORT_ERROR_ACCESS);

        long size = catalog.length();
        long modified = catalog.lastModified();

        MoviesDataProvider moviesDataProvider = new MoviesDataProvider(context);
        try {
            long lastSize = preferences.getLong(SettingsConstants.KEY_PREF_LAST_IMPORTED_SIZE, 0);
            long lastModified = preferences.getLong(SettingsConstants.KEY_PREF_LAST_IMPORTED_MODIFIED, 0);

            // Import when file size changed, when modification time changed (not known for catalogs imported by
            // older versions, so ignored then) or when the database is empty (e.g. after restore from backup).
            boolean importNeeded = size != lastSize
                    || (lastModified != 0 && modified != lastModified)
                    || moviesDataProvider.getMoviesCount() == 0;
            boolean picturesNeeded = treeUri != null
                    && (importNeeded || !preferences.getBoolean(SettingsConstants.KEY_PREF_PICTURES_SYNCED, false));

            if (!importNeeded && !picturesNeeded)
                return Result.success();

            startForeground();

            if (importNeeded && !importCatalog(context, preferences, moviesDataProvider, catalogUri, size, modified))
                return isStopped() ? Result.failure() : failure(S.IMPORT_ERROR_LOADING);

            if (picturesNeeded) {
                if (!syncPictures(context, preferences, moviesDataProvider, treeUri))
                    return Result.failure();
            } else if (treeUri == null) {
                // Only the XML was selected, there are no pictures to show
                CatalogFiles.clear(context);
            }

            // All other code reads pictures from this folder
            preferences.edit()
                    .putString(SettingsConstants.KEY_PREF_PICTURES_FOLDER, CatalogFiles.rootPrefix(context))
                    .apply();

            return Result.success();
        } finally {
            moviesDataProvider.closeDatabase();
        }
    }

    /**
     * Load catalog XML into the database. Delete of the old data, inserts and index rebuild run in a single
     * transaction: it is much faster than committing every row and failed import keeps the previous catalog.
     */
    private boolean importCatalog(Context context, SharedPreferences preferences,
                                  MoviesDataProvider moviesDataProvider, Uri catalogUri, long size, long modified) {

        String encoding = preferences.getString(SettingsConstants.KEY_PREF_CATALOG_ENCODING, "Cp1252");
        boolean removeBadChars = preferences.getBoolean(SettingsConstants.KEY_PREF_REMOVE_BAD_CHARS, false);

        boolean transactionStarted = false;
        boolean success = false;

        try (InputStream source = context.getContentResolver().openInputStream(catalogUri)) {
            if (source == null)
                throw new FileNotFoundException(catalogUri.toString());

            ProgressFilterInputStream stream = new ProgressFilterInputStream(
                    new BufferedInputStream(source, BUFFER_SIZE), bytesRead -> {
                if (isStopped())
                    throw new InterruptedIOException("Import cancelled");
                if (size > 0)
                    publish(S.IMPORT_LOADING_START, (int) Math.min(100L, bytesRead * 100L / size));
            });

            // Catalog is decoded as characters, the XML parser doesn't need to know the file encoding
            Reader reader = new CatalogReader(new InputStreamReader(stream, Charset.forName(encoding)),
                    removeBadChars);

            SAXParserFactory factory = SAXParserFactory.newInstance();
            SAXParser parser = factory.newSAXParser();
            XMLReader xmlReader = parser.getXMLReader();

            MoviesSAXHandler handler = new MoviesSAXHandler(moviesDataProvider);
            xmlReader.setContentHandler(handler);
            xmlReader.setErrorHandler(handler);

            publish(S.IMPORT_LOADING_START, 0);

            long startTime = System.currentTimeMillis();

            moviesDataProvider.beginTransaction();
            transactionStarted = true;

            // Drop indexes for faster import
            moviesDataProvider.dropIndexes();

            xmlReader.parse(new InputSource(reader));

            publish(S.IMPORT_INDEXING_START, -1);
            moviesDataProvider.createIndexes();

            moviesDataProvider.setTransactionSuccessful();
            success = true;

            if (S.DEBUG)
                Log.d(S.TAG, "Import elapsed time: " + (System.currentTimeMillis() - startTime) + " ms.");
        } catch (Exception e) {
            if (S.ERROR)
                Log.e(S.TAG, "Couldn't import XML file.", e);

            preferences.edit().putLong(SettingsConstants.KEY_PREF_LAST_IMPORTED_SIZE, 0).apply();
        } finally {
            // Commits when successful, otherwise rolls back to the previously imported catalog
            if (transactionStarted) {
                try {
                    moviesDataProvider.endTransaction();
                } catch (Exception e) {
                    if (S.ERROR)
                        Log.e(S.TAG, "Couldn't finish import transaction.", e);
                    success = false;
                }
            }
        }

        if (success) {
            preferences.edit()
                    .putLong(SettingsConstants.KEY_PREF_LAST_IMPORTED_SIZE, size)
                    .putLong(SettingsConstants.KEY_PREF_LAST_IMPORTED_MODIFIED, modified)
                    .putString(SettingsConstants.KEY_PREF_LAST_IMPORTED_DATE, new Date().toString())
                    .apply();
        }

        return success;
    }

    /**
     * Copy pictures referenced by the catalog to app storage.
     *
     * @return false when canceled
     */
    private boolean syncPictures(Context context, SharedPreferences preferences,
                                 MoviesDataProvider moviesDataProvider, Uri treeUri) {

        // Not synchronized until finished, so an interrupted copy is repeated on the next start
        preferences.edit().putBoolean(SettingsConstants.KEY_PREF_PICTURES_SYNCED, false).apply();

        try {
            Set<String> pictures = new LinkedHashSet<>(moviesDataProvider.getPicturePaths());

            publish(S.IMPORT_PICTURES_START, 0);
            CatalogFiles.sync(context, treeUri, pictures, percent -> {
                if (isStopped())
                    throw new InterruptedIOException("Import cancelled");
                publish(S.IMPORT_PICTURES_START, percent);
            });

            preferences.edit().putBoolean(SettingsConstants.KEY_PREF_PICTURES_SYNCED, true).apply();
            return true;
        } catch (InterruptedIOException e) {
            return false;
        } catch (Exception e) {
            // Movies are imported and usable, pictures will be tried again on the next start
            if (S.ERROR)
                Log.e(S.TAG, "Couldn't copy pictures.", e);
            return !isStopped();
        }
    }

    private Result failure(int error) {
        return Result.failure(new Data.Builder().putInt(KEY_ERROR, error).build());
    }

    /**
     * Publish progress to the UI and the notification. percent < 0 means indeterminate.
     */
    private void publish(int phase, int percent) {
        if (phase == mPhase && percent == mPercent)
            return;

        mPhase = phase;
        mPercent = percent;

        setProgressAsync(new Data.Builder().putInt(KEY_PHASE, phase).putInt(KEY_PERCENT, percent).build());

        if (mForeground)
            updateNotification();
    }

    private void startForeground() {
        mForeground = true;
        updateNotification();
    }

    private void updateNotification() {
        try {
            setForegroundAsync(createForegroundInfo());
        } catch (RuntimeException e) {
            // Not allowed to start a foreground service (e.g. app in background), import still continues
            if (S.WARN)
                Log.w(S.TAG, "Couldn't promote import to foreground service.", e);
            mForeground = false;
        }
    }

    /**
     * Creates the notification of the import and the information for promoting the worker to a foreground service.
     * The foreground service type is passed from Android 10 (API 29), where it was introduced. Before that, the
     * service has no type (the type declared in the manifest is used from Android 10 only).
     *
     * @return information about the foreground service of the import
     */
    private ForegroundInfo createForegroundInfo() {
        Context context = getApplicationContext();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID,
                    context.getString(R.string.import_notification_channel), NotificationManager.IMPORTANCE_LOW);
            NotificationManager manager = context.getSystemService(NotificationManager.class);
            if (manager != null)
                manager.createNotificationChannel(channel);
        }

        int text = switch (mPhase) {
            case S.IMPORT_INDEXING_START -> R.string.import_add_indexes;
            case S.IMPORT_PICTURES_START -> R.string.import_copy_pictures;
            default -> R.string.import_in_progress;
        };

        Notification notification = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_stat_import)
                .setContentTitle(context.getString(R.string.import_notification_title))
                .setContentText(context.getString(text))
                .setOngoing(true)
                .setOnlyAlertOnce(true)
                .setProgress(100, Math.max(mPercent, 0), mPercent < 0)
                .build();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            return new ForegroundInfo(NOTIFICATION_ID, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC);
        }
        return new ForegroundInfo(NOTIFICATION_ID, notification);
    }
}
