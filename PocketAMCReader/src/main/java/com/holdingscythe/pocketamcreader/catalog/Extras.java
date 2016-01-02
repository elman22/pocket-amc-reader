package com.holdingscythe.pocketamcreader.catalog;

import android.content.SharedPreferences;
import android.database.Cursor;
import android.util.Log;

import com.holdingscythe.pocketamcreader.S;
import com.holdingscythe.pocketamcreader.model.ExtraModel;
import com.holdingscythe.pocketamcreader.utils.SharedObjects;

import java.io.File;
import java.util.ArrayList;

/**
 * Load all available extras
 * Created by Elman on 30. 12. 2015.
 */
public class Extras {
    private ArrayList<ExtraModel> mExtras;

    public Extras(Cursor cursor) {
        mExtras = new ArrayList<>();

        if (cursor.getCount() > 0) {

            // Read preferences
            SharedPreferences preferences = SharedObjects.getInstance().preferences;
            String preferencePicturesDirectory = preferences.getString("settingPicturesFolder", "/");

            // add all extras from DB to the array list
            try {
                do {
                    // check if file exists, otherwise skip
                    File extraFile = new File(preferencePicturesDirectory + cursor.getString(cursor.getColumnIndex
                            (Movies.E_PICTURE)));
                    if (!extraFile.isFile()) {
                        continue;
                    }

                    mExtras.add(new ExtraModel(
                            preferencePicturesDirectory + cursor.getString(cursor.getColumnIndex(Movies.E_PICTURE)),
                            cursor.getString(cursor.getColumnIndex(Movies.E_CHECKED)).equals("True"),
                            cursor.getString(cursor.getColumnIndex(Movies.E_TAG)),
                            cursor.getString(cursor.getColumnIndex(Movies.E_TITLE)),
                            cursor.getString(cursor.getColumnIndex(Movies.E_CATEGORY)),
                            cursor.getString(cursor.getColumnIndex(Movies.E_URL)),
                            cursor.getString(cursor.getColumnIndex(Movies.E_DESCRIPTION)),
                            cursor.getString(cursor.getColumnIndex(Movies.E_COMMENTS)),
                            cursor.getString(cursor.getColumnIndex(Movies.E_CREATED_BY))
                    ));
                } while (cursor.moveToNext());

                if (S.DEBUG)
                    Log.d(S.TAG, "Total extras found: " + String.valueOf(mExtras.size()));

            } catch (Exception e) {
                if (S.ERROR)
                    Log.e(S.TAG, "Extras couldn't be loaded from DB: " + e.toString());
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