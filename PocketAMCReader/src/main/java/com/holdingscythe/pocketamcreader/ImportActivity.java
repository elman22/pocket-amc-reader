package com.holdingscythe.pocketamcreader;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.StrictMode;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.holdingscythe.pocketamcreader.utils.SharedObjects;

import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

/**
 * This Activity displays the screen's UI and starts a single TaskFragment that
 * will retain itself when configuration changes occur.
 */
public class ImportActivity extends FragmentActivity implements ImportFragment.TaskCallbacks {
    private static final String KEY_CURRENT_LABEL = "current_label";
    private static final String KEY_CURRENT_PROGRESS = "current_progress";
    private ProgressBar mProgressBar;
    private TextView mTextView;
    private boolean mDelayMainActivity = false;

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

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_import);

        // Prepare Shared Objects
        SharedObjects.getInstance().preferences =
                PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        // Set default font
        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                        .setDefaultFontPath(S.DEFAULT_FONT)
                        .setFontAttrId(R.attr.fontPath)
                        .build()
        );

        // Initialize views
        mTextView = (TextView) findViewById(R.id.progress_status);
        mProgressBar = (ProgressBar) findViewById(R.id.progress_horizontal);

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
    public void onSaveInstanceState(Bundle outState) {
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
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
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
                mDelayMainActivity = true;

                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle(getString(R.string.error_import_label));
                builder.setMessage(String.format(getString(R.string.error_import),
                        getString(R.string.pref_setting_encoding)));
                builder.setNeutralButton(getString(R.string.dialog_positive),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                                startMainActivity();
                            }
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
        if (!mDelayMainActivity) {
            mProgressBar.setProgress(mProgressBar.getMax());
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