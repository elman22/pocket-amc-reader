package com.holdingscythe.pocketamcreader;

/**
 * Created by Elman on 26. 7. 2015.
 * Implementation based on
 * http://www.androidhive.info/2013/09/android-fullscreen-image-slider-with-swipe-and-pinch-zoom-gestures/
 */

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;

import com.holdingscythe.pocketamcreader.images.TouchImageAdapter;
import com.holdingscythe.pocketamcreader.utils.SharedObjects;

import java.util.ArrayList;

import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class PictureViewActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_picture_view);

        // Set default font
        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                .setDefaultFontPath(S.DEFAULT_FONT)
                .setFontAttrId(R.attr.fontPath)
                .build()
        );

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
        ViewPager viewPager = (ViewPager) findViewById(R.id.fullscreen_picture_pager);
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
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

}