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

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.StrictMode;
import android.preference.PreferenceManager;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.holdingscythe.pocketamcreader.settings.SettingsActivity;
import com.holdingscythe.pocketamcreader.settings.SettingsConstants;
import com.holdingscythe.pocketamcreader.utils.SafHelper;
import com.holdingscythe.pocketamcreader.utils.SharedObjects;
import com.holdingscythe.pocketamcreader.utils.Theme;

import java.util.UUID;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.work.ExistingWorkPolicy;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;
import io.github.inflationx.calligraphy3.CalligraphyConfig;
import io.github.inflationx.calligraphy3.CalligraphyInterceptor;
import io.github.inflationx.viewpump.ViewPump;
import io.github.inflationx.viewpump.ViewPumpContextWrapper;

/**
 * Shows the progress of the catalog import ({@link CatalogImportWorker}) and starts the movie list when it is done.
 * The import itself is not tied to this screen: it keeps running when the screen is rotated or the app is minimized.
 */
public class ImportActivity extends AppCompatActivity {

    private static final String KEY_CURRENT_LABEL = "current_label";
    private static final String KEY_CURRENT_PROGRESS = "current_progress";
    private static final String KEY_WORK_ID = "work_id";

    private ProgressBar mProgressBar;
    private TextView mTextView;
    private UUID mWorkId;
    private AlertDialog mDialog;
    private boolean mFinished = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (S.STRICT) {
            StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
                    .detectDiskReads()
                    .detectDiskWrites()
                    .detectAll()
                    .penaltyLog()
                    .build());

            StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
                    .detectLeakedSqlLiteObjects()
                    .detectLeakedClosableObjects()
                    .penaltyLog()
                    .penaltyDeath()
                    .build());
        }

        setTheme(new Theme(this).getTheme());
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_import);

        // Prepare Shared Objects
        SharedObjects.getInstance().preferences =
                PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        // Set default font
        ViewPump.init(ViewPump.builder()
                .addInterceptor(new CalligraphyInterceptor(
                        new CalligraphyConfig.Builder()
                                .setDefaultFontPath(S.DEFAULT_FONT)
                                .setFontAttrId(R.attr.fontPath)
                                .build()))
                .build());

        Toolbar mToolbar = findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);

        // Initialize views
        mTextView = findViewById(R.id.progress_status);
        mProgressBar = findViewById(R.id.progress_horizontal);

        // Restore saved state
        if (savedInstanceState != null) {
            mTextView.setText(savedInstanceState.getString(KEY_CURRENT_LABEL));
            mProgressBar.setProgress(savedInstanceState.getInt(KEY_CURRENT_PROGRESS));

            String workId = savedInstanceState.getString(KEY_WORK_ID);
            if (workId != null)
                mWorkId = UUID.fromString(workId);
        }

        WorkManager workManager = WorkManager.getInstance(getApplicationContext());

        // Start the import only once, not again after rotation (work id is restored then).
        // A new launch replaces an import which was left running in the background.
        if (mWorkId == null) {
            OneTimeWorkRequest request = new OneTimeWorkRequest.Builder(CatalogImportWorker.class).build();
            workManager.enqueueUniqueWork(CatalogImportWorker.WORK_NAME, ExistingWorkPolicy.REPLACE, request);
            mWorkId = request.getId();
        }

        workManager.getWorkInfoByIdLiveData(mWorkId).observe(this, this::onWorkInfo);
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(KEY_CURRENT_LABEL, mTextView.getText().toString());
        outState.putInt(KEY_CURRENT_PROGRESS, mProgressBar.getProgress());
        if (mWorkId != null)
            outState.putString(KEY_WORK_ID, mWorkId.toString());
    }

    @Override
    protected void onDestroy() {
        if (mDialog != null && mDialog.isShowing())
            mDialog.dismiss();
        super.onDestroy();
    }

    /**
     * Wrap the Activity Context for Calligraphy
     */
    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(ViewPumpContextWrapper.wrap(newBase));
    }

    private void onWorkInfo(WorkInfo info) {
        if (info == null || mFinished)
            return;

        switch (info.getState()) {
            case RUNNING:
                showProgress(info.getProgress().getInt(CatalogImportWorker.KEY_PHASE, 0),
                        info.getProgress().getInt(CatalogImportWorker.KEY_PERCENT, -1));
                break;
            case SUCCEEDED:
            case CANCELLED:
                onFinished(0);
                break;
            case FAILED:
                onFinished(info.getOutputData().getInt(CatalogImportWorker.KEY_ERROR, S.IMPORT_ERROR_LOADING));
                break;
            default:
                // enqueued or blocked: waiting to start
                break;
        }
    }

    private void showProgress(int phase, int percent) {
        switch (phase) {
            case S.IMPORT_LOADING_START:
                mTextView.setText(R.string.import_in_progress);
                break;
            case S.IMPORT_INDEXING_START:
                mTextView.setText(R.string.import_add_indexes);
                break;
            case S.IMPORT_PICTURES_START:
                mTextView.setText(R.string.import_copy_pictures);
                break;
            default:
                break;
        }

        if (percent < 0) {
            mProgressBar.setIndeterminate(true);
        } else {
            mProgressBar.setIndeterminate(false);
            mProgressBar.setProgress(percent * mProgressBar.getMax() / 100);
        }
    }

    private void onFinished(int error) {
        switch (error) {
            case 0:
                mFinished = true;
                mTextView.setText(R.string.import_check_finished);
                mProgressBar.setIndeterminate(true);
                startMainActivity(false);
                break;

            case S.IMPORT_ERROR_NOT_CONFIGURED:
                String location = SharedObjects.getInstance().preferences
                        .getString(SettingsConstants.KEY_PREF_CATALOG_LOCATION, "");
                if (location.isEmpty() || SafHelper.isContentUri(location)) {
                    // First start, the list will guide the user to settings
                    mFinished = true;
                    startMainActivity(false);
                } else {
                    // Catalog selected by an older version as a plain path, it can't be read anymore
                    showDialog(R.string.import_storage_migration_title, R.string.import_storage_migration_message);
                }
                break;

            case S.IMPORT_ERROR_ACCESS:
                showDialog(R.string.import_access_lost_title, R.string.import_access_lost_message);
                break;

            case S.IMPORT_ERROR_LOADING:
            default:
                showImportErrorDialog();
                break;
        }
    }

    private void showImportErrorDialog() {
        if (mDialog != null && mDialog.isShowing())
            return;

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.error_import_label));
        builder.setMessage(String.format(getString(R.string.error_import2),
                getString(R.string.pref_setting_remove_bad_chars), getString(R.string.pref_setting_encoding)));
        builder.setCancelable(false);
        builder.setNeutralButton(getString(R.string.dialog_positive), (dialog, id) -> {
            dialog.cancel();
            mFinished = true;
            startMainActivity(false);
        });
        mDialog = builder.create();
        mDialog.show();
    }

    /**
     * Catalog is not accessible: let the user pick it again in settings, or continue with the data already imported
     */
    private void showDialog(int title, int message) {
        if (mDialog != null && mDialog.isShowing())
            return;

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(title));
        builder.setMessage(getString(message));
        builder.setCancelable(false);
        builder.setPositiveButton(getString(R.string.dialog_open_settings), (dialog, id) -> {
            dialog.cancel();
            mFinished = true;
            startMainActivity(true);
        });
        builder.setNegativeButton(getString(R.string.dialog_later), (dialog, id) -> {
            dialog.cancel();
            mFinished = true;
            startMainActivity(false);
        });
        mDialog = builder.create();
        mDialog.show();
    }

    private void startMainActivity(boolean openSettings) {
        // Start movie list activity (settings on top of it, so "back" leads to the list)
        startActivity(new Intent(this, MovieListActivity.class));
        if (openSettings)
            startActivity(new Intent(this, SettingsActivity.class));
        finish();
    }
}
