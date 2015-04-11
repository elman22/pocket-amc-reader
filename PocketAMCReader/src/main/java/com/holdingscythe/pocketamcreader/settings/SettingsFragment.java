package com.holdingscythe.pocketamcreader.settings;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.util.Log;

import com.holdingscythe.pocketamcreader.R;
import com.holdingscythe.pocketamcreader.S;
import com.holdingscythe.pocketamcreader.utils.SharedObjects;
import com.ipaulpro.afilechooser.utils.FileUtils;

// todo: clean up

/**
 * Pocket AMC Reader
 * Created by Elman on 5.7.2014.
 */
public class SettingsFragment extends PreferenceFragment implements SharedPreferences
        .OnSharedPreferenceChangeListener, Preference.OnPreferenceClickListener {
    public static final String KEY_PREF_CATALOG_LOCATION = "settingCatalogLocation";
    public static final String KEY_PREF_CATALOG_ENCODING = "settingCatalogEncoding";
    public static final String KEY_PREF_LIST_SEPARATOR = "settingMoviesListSeparator";

    private static final int REQUEST_CODE = 2753; // onActivityResult request code
    private static SharedPreferences mPrefs;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.preferences);

        // Attach onChange listener
        mPrefs = SharedObjects.getInstance().preferences;
        mPrefs.registerOnSharedPreferenceChangeListener(this);

        // Attach onClick listeners
        Preference pref1 = findPreference(KEY_PREF_CATALOG_LOCATION);
        pref1.setOnPreferenceClickListener(this);
//        Preference line1Pref = getPreferenceScreen().findPreference("settingMoviesListLine1");
//        line1Pref.setOnPreferenceClickListener(this);
//        Preference line2Pref = getPreferenceScreen().findPreference("settingMoviesListLine2");
//        line2Pref.setOnPreferenceClickListener(this);
//        Preference line3Pref = getPreferenceScreen().findPreference("settingMoviesListLine3");
//        line3Pref.setOnPreferenceClickListener(this);

        updateSummary(mPrefs, KEY_PREF_CATALOG_LOCATION);
        updateSummary(mPrefs, KEY_PREF_CATALOG_ENCODING);
        updateSummary(mPrefs, KEY_PREF_LIST_SEPARATOR);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        updateSummary(sharedPreferences, key);

        // Schedule app restart
        if (S.SETTINGS_REQUESTING_RESTART.containsKey(key)) {
            SharedObjects.getInstance().restartAppRequested = true;

            if (S.INFO)
                Log.i(S.TAG, "Restart requested (" + key + ")");
        }

        // Schedule list refresh
        if (S.SETTINGS_REQUESTING_REFRESH.containsKey(key)) {
            SharedObjects.getInstance().moviesListActivityRefreshRequested = true;

            if (S.INFO)
                Log.i(S.TAG, "Refresh requested (" + key + ")");
        }

        // If encoding changed, force re-import. Have to set size to 0, otherwise import wouldn't start
        if (key.equals(KEY_PREF_CATALOG_ENCODING)) {
            SharedPreferences.Editor editor = mPrefs.edit();
            editor.putLong("settingLastImportedSize", 0);
            editor.apply();

            if (S.INFO)
                Log.i(S.TAG, "Re-import forced (" + key + ")");
        }
    }

    @Override
    public boolean onPreferenceClick(Preference preference) {
        String key = preference.getKey();

        if (key.equals(KEY_PREF_CATALOG_LOCATION)) {
            // Use the GET_CONTENT intent from the utility class
            Intent getContentIntent = FileUtils.createGetContentIntent();
            // Create the chooser Intent
            Intent intent = Intent.createChooser(getContentIntent, getString(R.string.chooser_title));
            startActivityForResult(intent, REQUEST_CODE);
            return true;
        }

        // todo add activities
//        if (preference.getKey().equals("settingMoviesListLine1")) {
//            Intent settingData = new Intent(this, SettingsListActivity.class);
//            settingData.putExtra("LINE", 1);
//            startActivity(settingData);
//            return true;
//        } else if (preference.getKey().equals("settingMoviesListLine2")) {
//            Intent settingData = new Intent(this, SettingsListActivity.class);
//            settingData.putExtra("LINE", 2);
//            startActivity(settingData);
//            return true;
//        } else if (preference.getKey().equals("settingMoviesListLine3")) {
//            Intent settingData = new Intent(this, SettingsListActivity.class);
//            settingData.putExtra("LINE", 3);
//            startActivity(settingData);
//            return true;
//        }
        return false;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                if (data != null) {
                    // Get the URI of the selected file
                    final Uri uri = data.getData();

                    if (S.INFO)
                        Log.i(S.TAG, "XML path Uri (" + uri.toString() + ")");

                    try {
                        // Get the file path from the URI
                        final String path = FileUtils.getPath(getActivity().getBaseContext(), uri);
                        SharedPreferences.Editor editor = mPrefs.edit();
                        editor.putString("settingCatalogLocation", path);
                        editor.apply();
                        updateSummary(mPrefs, KEY_PREF_CATALOG_LOCATION);
                    } catch (Exception e) {
                        if (S.ERROR)
                            Log.e(S.TAG, "File select error " + e.getLocalizedMessage());
                    }
                }
            }
        }
    }

    private void updateSummary(SharedPreferences sharedPreferences, String key) {
        Preference pref = findPreference(key);

        if (key.equals(KEY_PREF_CATALOG_LOCATION)) {
            pref.setSummary(sharedPreferences.getString(key, getString(R.string.pref_setting_catalog_summary)));
        }

        if (key.equals(KEY_PREF_CATALOG_ENCODING)) {
            ListPreference listPref = (ListPreference) pref;
            pref.setSummary(listPref.getValue() == null ? getString(R.string.pref_setting_encoding_summary)
                    : listPref.getEntry());
        }

        if (key.equals(KEY_PREF_LIST_SEPARATOR)) {
            pref.setSummary(sharedPreferences.getString(key, getString(R.string.pref_setting_list_separator_summary)));
        }

        // Setup the initial values
//        mSettingDefaultTab
//                .setSummary(mSettingDefaultTab.getValue() == null ? getString(R.string.pref_setting_default_tab_summary)
//                        : mSettingDefaultTab.getEntry());
//        mSettingIMDb.setSummary(mSettingIMDb.getValue() == null ? getString(R.string.pref_setting_imdb_summary) : mSettingIMDb
//                .getEntry());
//        mSettingPlotInBasic
//                .setSummary(mSettingPlotInBasic.getValue() == null ? getString(R.string.pref_setting_plot_basic_summary)
//                        : mSettingPlotInBasic.getEntry());
//        mSettingFontSize.setSummary(mSettingFontSize.getValue() == null ? getString(R.string.pref_setting_font_size_summary)
//                : mSettingFontSize.getEntry());
//        mSettingMultifieldSeparator
//                .setSummary(mSettingMultifieldSeparator.getText() == null ? getString(R.string.pref_setting_multivalue_summary)
//                        : mSettingMultifieldSeparator.getText());

    }
}
