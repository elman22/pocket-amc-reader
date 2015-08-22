package com.holdingscythe.pocketamcreader;

/**
 * Created by Elman on 26. 7. 2015.
 * Implementation based on
 * http://www.androidhive.info/2013/09/android-fullscreen-image-slider-with-swipe-and-pinch-zoom-gestures/
 */

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.ViewPager;

import com.holdingscythe.pocketamcreader.images.TouchImageAdapter;

import java.util.ArrayList;

public class PictureViewActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_picture_view);

        Intent i = getIntent();
        ArrayList<String> filePaths = i.getStringArrayListExtra(MovieDetailFragment.ARG_MOVIE_PICTURES_LIST);

        if (filePaths == null) {
            filePaths = new ArrayList<String>();
        }

        ViewPager viewPager = (ViewPager) findViewById(R.id.fullscreen_picture_pager);
        TouchImageAdapter adapter = new TouchImageAdapter(PictureViewActivity.this, filePaths);
        viewPager.setAdapter(adapter);
    }

}