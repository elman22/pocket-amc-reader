package com.holdingscythe.pocketamcreader.settings;

import android.support.annotation.NonNull;

import com.nononsenseapps.filepicker.FilePickerFragment;

import java.io.File;

/**
 * Created by elman on 9.5.2017.
 * Based on http://spacecowboy.github.io/NoNonsense-FilePicker/example/filter_file_extension/
 */

public class SettingsFilePickerFragment extends FilePickerFragment {

    // File extension to filter on
    private static final String EXTENSION = ".xml";

    /**
     * Read extension of the file
     *
     * @param file File to check extension.
     * @return The file extension. If file has no extension, it returns null.
     */
    private String getExtension(@NonNull File file) {
        String path = file.getPath();
        int i = path.lastIndexOf(".");
        if (i < 0) {
            return null;
        } else {
            return path.substring(i);
        }
    }

    @Override
    protected boolean isItemVisible(final File file) {
        boolean ret = super.isItemVisible(file);
        if (ret && !isDir(file) && (mode == MODE_FILE || mode == MODE_FILE_AND_DIR)) {
            String ext = getExtension(file);
            return ext != null && EXTENSION.equalsIgnoreCase(ext);
        }
        return ret;
    }

}
