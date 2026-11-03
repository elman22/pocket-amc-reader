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

package com.holdingscythe.pocketamcreader.settings;

import android.Manifest;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.holdingscythe.pocketamcreader.R;
import com.holdingscythe.pocketamcreader.S;
import com.holdingscythe.pocketamcreader.utils.SafHelper;
import com.holdingscythe.pocketamcreader.utils.SharedObjects;
import com.holdingscythe.pocketamcreader.utils.TreeIndex;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.documentfile.provider.DocumentFile;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

/**
 * Settings Fragment
 * Created by Elman on 5.7.2014.
 */
public class SettingsFragment extends PreferenceFragmentCompat implements SharedPreferences
        .OnSharedPreferenceChangeListener, Preference.OnPreferenceClickListener {

    private SharedPreferences mPrefs;
    private Map<String, String> defaultTexts;

    private final ActivityResultLauncher<Intent> mFolderLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(), result -> onPickerResult(result, true));
    private final ActivityResultLauncher<Intent> mFileLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(), result -> onPickerResult(result, false));
    // Import works without the permission, the notification is only informative
    private final ActivityResultLauncher<String> mNotificationLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(), granted -> {
            });

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        // Load the preferences from an XML resource
        setPreferencesFromResource(R.xml.preferences, rootKey);

        mPrefs = getPreferenceManager().getSharedPreferences();

        // Attach onClick listeners
        Preference locationPref = findPreference(SettingsConstants.KEY_PREF_CATALOG_LOCATION);
        if (locationPref != null)
            locationPref.setOnPreferenceClickListener(this);
        Preference linesPref = findPreference(SettingsConstants.KEY_PREF_LIST_FIELDS);
        if (linesPref != null)
            linesPref.setOnPreferenceClickListener(this);

        // Prefetch default text hints
        defaultTexts = new HashMap<>();
        defaultTexts.put(SettingsConstants.KEY_PREF_CATALOG_LOCATION, getString(R.string.pref_setting_catalog_summary));
        defaultTexts.put(SettingsConstants.KEY_PREF_CATALOG_ENCODING, getString(R.string.pref_setting_encoding_summary));
        defaultTexts.put(SettingsConstants.KEY_PREF_LIST_SEPARATOR, getString(R.string.pref_setting_list_separator_summary));
        defaultTexts.put(SettingsConstants.KEY_PREF_DETAIL_SEPARATOR, getString(R.string.pref_setting_multivalue_summary));
        defaultTexts.put(SettingsConstants.KEY_PREF_THEME, getString(R.string.pref_setting_theme_summary));

        // Update current values
        for (String key : defaultTexts.keySet())
            updateSummary(mPrefs, key);
    }

    @Override
    public void onStart() {
        super.onStart();
        mPrefs.registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onStop() {
        mPrefs.unregisterOnSharedPreferenceChangeListener(this);
        super.onStop();
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key == null)
            return;

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
    public boolean onPreferenceClick(@NonNull Preference preference) {
        String key = preference.getKey();

        if (SettingsConstants.KEY_PREF_CATALOG_LOCATION.equals(key)) {
            showCatalogPicker();
            return true;
        }

        if (SettingsConstants.KEY_PREF_LIST_FIELDS.equals(key)) {
            startActivity(new Intent(requireContext(), SettingsListFieldsActivity.class));
            return true;
        }

        return false;
    }

    /**
     * Ask what to pick. Folder is recommended: it gives the app access to both the XML and the pictures.
     * Android doesn't allow to pick some folders (e.g. the root of the storage), so the XML alone can be picked too.
     */
    private void showCatalogPicker() {
        String[] items = {
                getString(R.string.pref_catalog_pick_folder),
                getString(R.string.pref_catalog_pick_file)
        };

        new AlertDialog.Builder(requireContext())
                .setTitle(R.string.pref_setting_catalog)
                .setItems(items, (dialog, which) -> {
                    Intent intent;

                    if (which == 0) {
                        intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
                    } else {
                        intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                        intent.addCategory(Intent.CATEGORY_OPENABLE);
                        intent.setType("*/*");
                    }

                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION
                            | Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);

                    try {
                        (which == 0 ? mFolderLauncher : mFileLauncher).launch(intent);
                    } catch (ActivityNotFoundException e) {
                        Toast.makeText(requireContext(), R.string.catalog_picker_unavailable, Toast.LENGTH_LONG)
                                .show();
                    }
                })
                .setNegativeButton(R.string.dialog_negative, null)
                .show();
    }

    private void onPickerResult(ActivityResult result, boolean folder) {
        Intent data = result.getData();
        if (result.getResultCode() != Activity.RESULT_OK || data == null || data.getData() == null)
            return;

        Uri uri = data.getData();

        if (S.INFO)
            Log.i(S.TAG, "Catalog Uri (" + uri + ")");

        if (folder)
            onFolderPicked(uri);
        else
            onFilePicked(uri);
    }

    private void onFolderPicked(Uri treeUri) {
        Context context = requireContext();

        if (!SafHelper.takeReadPermission(context, treeUri)) {
            Toast.makeText(context, R.string.catalog_picker_denied, Toast.LENGTH_LONG).show();
            return;
        }

        TreeIndex index = new TreeIndex(context.getContentResolver(), treeUri);
        List<TreeIndex.Entry> xmlFiles = SafHelper.listXmlFiles(index);

        if (xmlFiles.isEmpty()) {
            Toast.makeText(context, R.string.catalog_no_xml_found, Toast.LENGTH_LONG).show();
        } else if (xmlFiles.size() == 1) {
            applyFolder(treeUri, index, xmlFiles.get(0));
        } else {
            String[] names = xmlFiles.stream().map(entry -> entry.name).toArray(String[]::new);

            new AlertDialog.Builder(context)
                    .setTitle(R.string.catalog_choose_xml)
                    .setItems(names, (dialog, which) -> applyFolder(treeUri, index, xmlFiles.get(which)))
                    .setNegativeButton(R.string.dialog_negative, null)
                    .show();
        }
    }

    private void applyFolder(Uri treeUri, TreeIndex index, TreeIndex.Entry xml) {
        DocumentFile folder = DocumentFile.fromTreeUri(requireContext(), treeUri);
        String folderName = folder == null ? null : folder.getName();
        String display = folderName == null || folderName.isEmpty() ? xml.name : folderName + "/" + xml.name;

        storeCatalog(index.uriOf(xml), treeUri, display);
    }

    private void onFilePicked(Uri fileUri) {
        Context context = requireContext();

        if (!SafHelper.takeReadPermission(context, fileUri)) {
            Toast.makeText(context, R.string.catalog_picker_denied, Toast.LENGTH_LONG).show();
            return;
        }

        DocumentFile file = DocumentFile.fromSingleUri(context, fileUri);
        String name = file == null ? null : file.getName();

        storeCatalog(fileUri, null, name == null ? fileUri.getLastPathSegment() : name);
    }

    /**
     * Remember the selected catalog (and the folder it comes from) and schedule import
     */
    private void storeCatalog(Uri catalogUri, Uri treeUri, String displayName) {
        Context context = requireContext();

        String oldLocation = mPrefs.getString(SettingsConstants.KEY_PREF_CATALOG_LOCATION, "");
        String oldTree = mPrefs.getString(SettingsConstants.KEY_PREF_CATALOG_TREE, "");
        String newTree = treeUri == null ? "" : treeUri.toString();

        try {
            // Size 0 and pictures not synchronized: import everything again
            mPrefs.edit()
                    .putString(SettingsConstants.KEY_PREF_CATALOG_TREE, newTree)
                    .putString(SettingsConstants.KEY_PREF_CATALOG_NAME, displayName)
                    .putLong(SettingsConstants.KEY_PREF_LAST_IMPORTED_SIZE, 0)
                    .putBoolean(SettingsConstants.KEY_PREF_PICTURES_SYNCED, false)
                    .putString(SettingsConstants.KEY_PREF_CATALOG_LOCATION, catalogUri.toString())
                    .apply();

            // Don't keep access to locations we don't use anymore
            if (!oldTree.isEmpty() && !oldTree.equals(newTree))
                SafHelper.releasePermission(context, oldTree);
            if (oldTree.isEmpty() && !oldLocation.equals(catalogUri.toString()))
                SafHelper.releasePermission(context, oldLocation);

            SharedObjects.getInstance().restartAppRequested = true;
            updateSummary(mPrefs, SettingsConstants.KEY_PREF_CATALOG_LOCATION);
            requestNotificationPermission();
        } catch (Exception e) {
            if (S.ERROR)
                Log.e(S.TAG, "File select error " + e.getLocalizedMessage());
        }
    }

    /**
     * Import runs as a foreground service with a progress notification. Android 13+ needs permission to show it
     * (import works also without it). Asked only once, right before the first import.
     */
    private void requestNotificationPermission() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU)
            return;

        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.POST_NOTIFICATIONS)
                == PackageManager.PERMISSION_GRANTED
                || mPrefs.getBoolean(SettingsConstants.KEY_PREF_NOTIFICATIONS_ASKED, false))
            return;

        mPrefs.edit().putBoolean(SettingsConstants.KEY_PREF_NOTIFICATIONS_ASKED, true).apply();
        mNotificationLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
    }

    private void updateSummary(SharedPreferences sharedPreferences, String key) {
        Preference pref = findPreference(key);
        if (pref == null)
            return;

        String defaultText = defaultTexts.get(key);

        switch (key) {
            // Catalog is a content Uri, show its name
            case SettingsConstants.KEY_PREF_CATALOG_LOCATION -> {
                String location = sharedPreferences.getString(key, "");

                if (location.isEmpty())
                    pref.setSummary(defaultText);
                else if (!SafHelper.isContentUri(location))
                    pref.setSummary(R.string.pref_setting_catalog_legacy);
                else
                    pref.setSummary(sharedPreferences.getString(SettingsConstants.KEY_PREF_CATALOG_NAME, location));
            }

            // String preferences
            case SettingsConstants.KEY_PREF_LIST_SEPARATOR, SettingsConstants.KEY_PREF_DETAIL_SEPARATOR ->
                    pref.setSummary(sharedPreferences.getString(key, defaultText));

            // List preferences
            case SettingsConstants.KEY_PREF_CATALOG_ENCODING, SettingsConstants.KEY_PREF_THEME -> {
                ListPreference listPref = (ListPreference) pref;
                pref.setSummary(listPref.getValue() == null ? defaultText : listPref.getEntry());
            }

            default -> {
                // no summary to update
            }
        }
    }
}
