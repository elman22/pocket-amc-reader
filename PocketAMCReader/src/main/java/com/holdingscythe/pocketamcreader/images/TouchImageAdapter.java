package com.holdingscythe.pocketamcreader.images;

/**
 * Display title image + extras for a movie
 * Created by Elman on 26. 7. 2015.
 */

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.holdingscythe.pocketamcreader.R;

import java.util.ArrayList;

public class TouchImageAdapter extends PagerAdapter {

    private Activity mActivity;
    private ArrayList<String> mImagePaths;

    // constructor
    public TouchImageAdapter(Activity activity, ArrayList<String> imagePaths) {
        this.mActivity = activity;
        this.mImagePaths = imagePaths;
    }

    @Override
    public int getCount() {
        return this.mImagePaths.size();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        TouchImageView imgDisplay;
        TextView pictureCount;

        LayoutInflater inflater = (LayoutInflater) mActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View viewLayout = inflater.inflate(R.layout.fullscreen_image, container, false);

        imgDisplay = (TouchImageView) viewLayout.findViewById(R.id.picture_display);

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        Bitmap bitmap = BitmapFactory.decodeFile(mImagePaths.get(position), options);
        imgDisplay.setImageBitmap(bitmap);

        // display image counter, but only if at least one extra exists
        pictureCount = (TextView) viewLayout.findViewById(R.id.picture_counter);
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
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((RelativeLayout) object);
    }
}