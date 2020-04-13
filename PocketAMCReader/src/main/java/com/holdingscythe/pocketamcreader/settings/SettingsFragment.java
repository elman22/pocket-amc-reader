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

package com.holdingscythe.pocketamcreader.settings;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.util.Log;

import com.holdingscythe.pocketamcreader.R;
import com.holdingscythe.pocketamcreader.S;
import com.holdingscythe.pocketamcreader.utils.SharedObjects;
import com.nononsenseapps.filepicker.FilePickerActivity;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * Settings Fragment
 * Created by Elman on 5.7.2014.
 */
public class SettingsFragment extends PreferenceFragment implements SharedPreferences
        .OnSharedPreferenceChangeListener, Preference.OnPreferenceClickListener {

    private static final int FILE_CODE = 9510; // onActivityResult request code
    private static SharedPreferences mPrefs;
    private Map<String, String> defaultTexts;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.preferences);

        // Attach onChange listener
        mPrefs = SharedObjects.getInstance().preferences;
        mPrefs.registerOnSharedPreferenceChangeListener(this);

        // Attach onClick listeners
        Preference locationPref = findPreference(SettingsConstants.KEY_PREF_CATALOG_LOCATION);
        locationPref.setOnPreferenceClickListener(this);
        Preference linesPref = getPreferenceScreen().findPreference(SettingsConstants.KEY_PREF_LIST_FIELDS);
        linesPref.setOnPreferenceClickListener(this);

        // Prefetch default text hints
        defaultTexts = new HashMap<>();
        defaultTexts.put(SettingsConstants.KEY_PREF_CATALOG_LOCATION, getString(R.string.pref_setting_catalog_summary));
        defaultTexts.put(SettingsConstants.KEY_PREF_CATALOG_ENCODING, getString(R.string.pref_setting_encoding_summary));
        defaultTexts.put(SettingsConstants.KEY_PREF_LIST_SEPARATOR, getString(R.string.pref_setting_list_separator_summary));
        defaultTexts.put(SettingsConstants.KEY_PREF_DETAIL_SEPARATOR, getString(R.string.pref_setting_multivalue_summary));
        defaultTexts.put(SettingsConstants.KEY_PREF_THEME, getString(R.string.pref_setting_theme_summary));

        // Update current values
        updateSummary(mPrefs, SettingsConstants.KEY_PREF_CATALOG_LOCATION);
        updateSummary(mPrefs, SettingsConstants.KEY_PREF_CATALOG_ENCODING);
        updateSummary(mPrefs, SettingsConstants.KEY_PREF_LIST_SEPARATOR);
        updateSummary(mPrefs, SettingsConstants.KEY_PREF_DETAIL_SEPARATOR);
        updateSummary(mPrefs, SettingsConstants.KEY_PREF_THEME);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        updateSummary(sharedPreferences, key);

        // Schedule app import. Have to set size to 0, otherwise import wouldn't start
        if (S.SETTINGS_REQUESTING_IMPORT.containsKey(key)) {
            SharedObjects.getInstance().restartAppRequested = true;

            SharedPreferences.Editor editor = mPrefs.edit();
            editor.putLong(SettingsConstants.KEY_PREF_LAST_IMPORTED_SIZE, 0);
            editor.apply();

            if (S.INFO)
                Log.i(S.TAG, "Re-import forced (" + key + ")");
        }

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
    }

    @Override
    public boolean onPreferenceClick(Preference preference) {
        String key = preference.getKey();

        if (key.equals(SettingsConstants.KEY_PREF_CATALOG_LOCATION)) {
            Intent i = new Intent(getActivity().getBaseContext(), SettingsFilePickerActivity.class);

            i.putExtra(FilePickerActivity.EXTRA_ALLOW_MULTIPLE, false);
            i.putExtra(FilePickerActivity.EXTRA_ALLOW_CREATE_DIR, false);
            i.putExtra(FilePickerActivity.EXTRA_MODE, FilePickerActivity.MODE_FILE);
            i.putExtra(FilePickerActivity.EXTRA_START_PATH, Environment.getExternalStorageDirectory().getPath());

            startActivityForResult(i, FILE_CODE);
            return true;
        }

        if (key.equals(SettingsConstants.KEY_PREF_LIST_FIELDS)) {
            Intent i = new Intent(getActivity().getBaseContext(), SettingsListFieldsActivity.class);

            startActivity(i);
            return true;
        }

        return false;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (requestCode == FILE_CODE && resultCode == Activity.RESULT_OK) {
            if (!intent.getBooleanExtra(FilePickerActivity.EXTRA_ALLOW_MULTIPLE, false)) {
                // The URI will now be something like content://PACKAGE-NAME/root/path/to/file
                Uri uri = intent.getData();
                // A utility method is provided to transform the URI to a File object
                File file = com.nononsenseapps.filepicker.Utils.getFileForUri(uri);

                if (S.INFO)
                    Log.i(S.TAG, "XML path Uri (" + file.toString() + ")");

                try {
                    SharedPreferences.Editor editor = mPrefs.edit();
                    editor.putString(SettingsConstants.KEY_PREF_CATALOG_LOCATION, file.toString());
                    editor.apply();
                    updateSummary(mPrefs, SettingsConstants.KEY_PREF_CATALOG_LOCATION);
                } catch (Exception e) {
                    if (S.ERROR)
                        Log.e(S.TAG, "File select error " + e.getLocalizedMessage());
                }
            }
        }
    }

    private void updateSummary(SharedPreferences sharedPreferences, String key) {
        Preference pref = findPreference(key);
        String defaultText = defaultTexts.get(key);

        // String preferences
        if (key.equals(SettingsConstants.KEY_PREF_CATALOG_LOCATION) ||
                key.equals(SettingsConstants.KEY_PREF_LIST_SEPARATOR) ||
                key.equals(SettingsConstants.KEY_PREF_DETAIL_SEPARATOR)) {
            pref.setSummary(sharedPreferences.getString(key, defaultText));
        }

        // List preferences
        if (key.equals(SettingsConstants.KEY_PREF_CATALOG_ENCODING) ||
                key.equals(SettingsConstants.KEY_PREF_THEME)) {
            ListPreference listPref = (ListPreference) pref;
            pref.setSummary(listPref.getValue() == null ? defaultText : listPref.getEntry());
        }
    }
}
