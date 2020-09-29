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

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.StrictMode;
import android.preference.PreferenceManager;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.holdingscythe.pocketamcreader.utils.SharedObjects;
import com.holdingscythe.pocketamcreader.utils.Theme;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.FragmentManager;
import io.github.inflationx.calligraphy3.CalligraphyConfig;
import io.github.inflationx.calligraphy3.CalligraphyInterceptor;
import io.github.inflationx.viewpump.ViewPump;
import io.github.inflationx.viewpump.ViewPumpContextWrapper;

/**
 * This Activity displays the screen's UI and starts a single TaskFragment that
 * will retain itself when configuration changes occur.
 */
public class ImportActivity extends AppCompatActivity implements ImportFragment.TaskCallbacks {
    private static final String KEY_CURRENT_LABEL = "current_label";
    private static final String KEY_CURRENT_PROGRESS = "current_progress";
    private ProgressBar mProgressBar;
    private TextView mTextView;
    private boolean mDelayedMainActivity = false;

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
        }

        // Retain fragment across configuration changes
        FragmentManager fm = getSupportFragmentManager();
        ImportFragment mImportFragment = (ImportFragment) fm.findFragmentByTag("import");

        // If the Fragment is non-null, then it is currently being retained across a configuration change.
        if (mImportFragment == null) {
            mImportFragment = new ImportFragment();
            fm.beginTransaction().add(mImportFragment, "import").commit();
            mImportFragment.start(this);
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(KEY_CURRENT_LABEL, mTextView.getText().toString());
        outState.putInt(KEY_CURRENT_PROGRESS, mProgressBar.getProgress());
    }

    @Override
    public void onPreExecute() {
    }

    /**
     * Wrap the Activity Context for Calligraphy
     */
    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(ViewPumpContextWrapper.wrap(newBase));
    }

    @Override
    public void onProgressUpdate(int state) {
        switch (state) {
            case S.IMPORT_CONVERSION_START:
                mTextView.setText(R.string.import_read_file);
                break;
            case S.IMPORT_LOADING_START:
                mTextView.setText(R.string.import_in_progress);
                break;
            case S.IMPORT_INDEXING_START:
                mTextView.setText(R.string.import_add_indexes);
                break;
            case S.IMPORT_ERROR_CONVERSION:
            case S.IMPORT_ERROR_LOADING:
                mDelayedMainActivity = true;

                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle(getString(R.string.error_import_label));
                builder.setMessage(String.format(getString(R.string.error_import2), getString(R.string
                        .pref_setting_remove_bad_chars), getString(R.string.pref_setting_encoding)));
                builder.setNeutralButton(getString(R.string.dialog_positive),
                        (dialog, id) -> {
                            dialog.cancel();
                            startMainActivity();
                        }
                );
                AlertDialog alertDialog = builder.create();
                alertDialog.show();
                break;
            default:
                mProgressBar.setProgress(state * mProgressBar.getMax() / 100);
                break;
        }
    }

    @Override
    public void onPostExecute() {
        // Delay application start if dialog shown
        if (!mDelayedMainActivity) {
            mTextView.setText(R.string.import_check_finished);
            mProgressBar.setIndeterminate(true);
            startMainActivity();
        }
    }

    private void startMainActivity() {
        // Start movie list activity
        Intent listIntent = new Intent(this, MovieListActivity.class);
        startActivity(listIntent);
        finish();
    }
}