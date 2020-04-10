/*
    This file is part of Pocket AMC Reader.
    Copyright © 2010-2020 Elman <holdingscythe@zoznam.sk>
    Copyright © 2017 spacecowboy

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

import com.nononsenseapps.filepicker.FilePickerFragment;

import java.io.File;

import androidx.annotation.NonNull;

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
            return EXTENSION.equalsIgnoreCase(ext);
        }
        return ret;
    }

}
