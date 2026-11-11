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

package com.holdingscythe.pocketamcreader.catalog;

import android.content.SharedPreferences;
import android.database.Cursor;
import android.util.Log;

import com.holdingscythe.pocketamcreader.S;
import com.holdingscythe.pocketamcreader.model.ExtraModel;
import com.holdingscythe.pocketamcreader.settings.SettingsConstants;
import com.holdingscythe.pocketamcreader.utils.SharedObjects;

import java.io.File;
import java.util.ArrayList;

/**
 * Load all available extras
 * Created by Elman on 30. 12. 2015.
 */
public class Extras {
    private final ArrayList<ExtraModel> mExtras;

    public Extras(Cursor cursor) {
        mExtras = new ArrayList<>();

        if (cursor.getCount() > 0) {

            // Read preferences
            SharedPreferences preferences = SharedObjects.getInstance().preferences;
            String preferencePicturesDirectory = preferences.getString(SettingsConstants.KEY_PREF_PICTURES_FOLDER,
                    "/");

            // add all extras from DB to the array list
            try {
                do {
                    // extra without picture or with a missing file is skipped
                    String extraPicture = cursor.getString(cursor.getColumnIndexOrThrow(Movies.E_PICTURE));
                    if (extraPicture == null || !new File(preferencePicturesDirectory + extraPicture).isFile()) {
                        continue;
                    }

                    mExtras.add(new ExtraModel(
                            preferencePicturesDirectory + extraPicture,
                            S.CATALOG_TRUE.equals(cursor.getString(cursor.getColumnIndexOrThrow(Movies.E_CHECKED))),
                            cursor.getString(cursor.getColumnIndexOrThrow(Movies.E_TAG)),
                            cursor.getString(cursor.getColumnIndexOrThrow(Movies.E_TITLE)),
                            cursor.getString(cursor.getColumnIndexOrThrow(Movies.E_CATEGORY)),
                            cursor.getString(cursor.getColumnIndexOrThrow(Movies.E_URL)),
                            cursor.getString(cursor.getColumnIndexOrThrow(Movies.E_DESCRIPTION)),
                            cursor.getString(cursor.getColumnIndexOrThrow(Movies.E_COMMENTS)),
                            cursor.getString(cursor.getColumnIndexOrThrow(Movies.E_CREATED_BY))
                    ));
                } while (cursor.moveToNext());

                if (S.DEBUG)
                    Log.d(S.TAG, "Total extras found: " + mExtras.size());

            } catch (Exception e) {
                if (S.ERROR)
                    Log.e(S.TAG, "Extras couldn't be loaded from DB: " + e);
            }
        }
    }

    /**
     * Return array list with all available pictures for the movie
     */
    public ArrayList<String> getPicturesList() {
        ArrayList<String> pictureList = new ArrayList<>();

        // get all extra images
        for (ExtraModel extra : mExtras) {
            pictureList.add(extra.getEPicture());
        }

        return pictureList;
    }
}