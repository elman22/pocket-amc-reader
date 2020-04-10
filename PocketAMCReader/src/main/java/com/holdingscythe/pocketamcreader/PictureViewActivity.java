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

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;

import com.holdingscythe.pocketamcreader.images.TouchImageAdapter;
import com.holdingscythe.pocketamcreader.utils.SharedObjects;

import java.util.ArrayList;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;
import io.github.inflationx.calligraphy3.CalligraphyConfig;
import io.github.inflationx.calligraphy3.CalligraphyInterceptor;
import io.github.inflationx.viewpump.ViewPump;
import io.github.inflationx.viewpump.ViewPumpContextWrapper;

/**
 * Created by Elman on 26. 7. 2015.
 * Implementation based on
 * http://www.androidhive.info/2013/09/android-fullscreen-image-slider-with-swipe-and-pinch-zoom-gestures/
 */
public class PictureViewActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_picture_view);

        // Set default font
        ViewPump.init(ViewPump.builder()
                .addInterceptor(new CalligraphyInterceptor(
                        new CalligraphyConfig.Builder()
                                .setDefaultFontPath(S.DEFAULT_FONT)
                                .setFontAttrId(R.attr.fontPath)
                                .build()))
                .build());

        // Read preferences
        if (SharedObjects.getInstance().preferences == null) {
            // Prepare Shared Objects
            SharedObjects.getInstance().preferences = PreferenceManager.getDefaultSharedPreferences
                    (getApplicationContext());
        }
        boolean settingFitPicture = SharedObjects.getInstance().preferences.getBoolean("settingFitPicture", true);

        // Read picture list
        Intent i = getIntent();
        ArrayList<String> filePaths = i.getStringArrayListExtra(MovieDetailFragment.ARG_MOVIE_PICTURES_LIST);
        // String title = i.getStringExtra(MovieDetailFragment.ARG_MOVIE_TITLE);

        // if there is no file in file paths, create empty arrayList
        if (filePaths == null) {
            filePaths = new ArrayList<>();
        }

        // create viewpager
        ViewPager viewPager = findViewById(R.id.fullscreen_picture_pager);
        TouchImageAdapter adapter = new TouchImageAdapter(PictureViewActivity.this, filePaths, settingFitPicture);
        viewPager.setAdapter(adapter);

        // change title of the activity
        // setTitle(title);
    }

    /**
     * Wrap the Activity Context for Calligraphy
     */
    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(ViewPumpContextWrapper.wrap(newBase));
    }

}