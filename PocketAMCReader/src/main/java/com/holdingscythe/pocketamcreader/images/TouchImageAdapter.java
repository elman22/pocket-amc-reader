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

package com.holdingscythe.pocketamcreader.images;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.holdingscythe.pocketamcreader.R;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;

/**
 * Display title image + extras for a movie
 * Created by Elman on 26. 7. 2015.
 */
public class TouchImageAdapter extends PagerAdapter {

    private Activity mActivity;
    private ArrayList<String> mImagePaths;
    private TouchImageView.ScaleType mScaleType;

    // constructor
    public TouchImageAdapter(Activity activity, ArrayList<String> imagePaths, Boolean settingFitPicture) {
        this.mActivity = activity;
        this.mImagePaths = imagePaths;
        this.mScaleType = (settingFitPicture) ? TouchImageView.ScaleType.FIT_CENTER : TouchImageView.ScaleType.CENTER;
    }

    @Override
    public int getCount() {
        return this.mImagePaths.size();
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view == object;
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        TouchImageView imgDisplay;
        TextView pictureCount;

        LayoutInflater inflater = (LayoutInflater) mActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View viewLayout = inflater.inflate(R.layout.fullscreen_image, container, false);

        imgDisplay = viewLayout.findViewById(R.id.picture_display);

        // set scale according to preferences
        if (mScaleType == TouchImageView.ScaleType.CENTER)
            imgDisplay.setScaleType(TouchImageView.ScaleType.CENTER);

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        Bitmap bitmap = BitmapFactory.decodeFile(mImagePaths.get(position), options);
        imgDisplay.setImageBitmap(bitmap);

        // display image counter, but only if at least one extra exists
        pictureCount = viewLayout.findViewById(R.id.picture_counter);
        if (getCount() > 1) {
            pictureCount.setText(String.format(mActivity.getString(R.string.picture_counter),
                    position + 1, getCount()));
        } else {
            pictureCount.setText(null);
        }

        container.addView(viewLayout);

        return viewLayout;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, @NonNull Object object) {
        container.removeView((RelativeLayout) object);
    }
}