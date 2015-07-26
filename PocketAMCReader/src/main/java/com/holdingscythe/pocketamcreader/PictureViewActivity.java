package com.holdingscythe.pocketamcreader;

/**
 * Created by Elman on 26. 7. 2015.
 * Implementation based on
 * http://www.androidhive.info/2013/09/android-fullscreen-image-slider-with-swipe-and-pinch-zoom-gestures/
 */

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.widget.Toast;

import com.holdingscythe.pocketamcreader.images.TouchImageAdapter;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class PictureViewActivity extends Activity {

    // TODO: REMOVE
    // Number of columns of Grid View
    public static final int NUM_OF_COLUMNS = 3;
    // Gridview image padding
    public static final int GRID_PADDING = 8; // in dp
    // SD card image directory
    public static final String PHOTO_ALBUM = "androidhive";
    // supported file formats
    public static final List<String> FILE_EXTN = Arrays.asList("jpg", "jpeg", "png");
    // TODO: REMOVE END

    private TouchImageAdapter adapter;
    private ViewPager viewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fullscreen_view);

        Intent i = getIntent();
        int position = i.getIntExtra(MovieDetailFragment.ARG_FULLSCREEN_IMAGE_ID, 0);

        viewPager = (ViewPager) findViewById(R.id.fullscreen_picture_pager);
        adapter = new TouchImageAdapter(PictureViewActivity.this, getFilePaths());
        viewPager.setAdapter(adapter);

        // displaying selected image first
        viewPager.setCurrentItem(position);
    }

    // TODO: Remove
    // Reading file paths from SDCard
    public ArrayList<String> getFilePaths() {
        ArrayList<String> filePaths = new ArrayList<String>();

//        File directory = new File(
//                android.os.Environment.getExternalStorageDirectory()
//                        + File.separator + PHOTO_ALBUM);

        File directory = new File("/storage/emulated/legacy/Catalog/");

        // check for directory
        if (directory.isDirectory()) {
            // getting list of file paths
            File[] listFiles = directory.listFiles();

            // Check for count
            if (listFiles.length > 0) {

                // loop through all files
                for (int i = 0; i < listFiles.length; i++) {

                    // get file path
                    String filePath = listFiles[i].getAbsolutePath();

                    // check for supported file extension
                    if (IsSupportedFile(filePath)) {
                        // Add image path to array list
                        filePaths.add(filePath);
                    }
                }
            } else {
                // image directory is empty
                Toast.makeText(
                        getApplicationContext(),
                        PHOTO_ALBUM
                                + " is empty. Please load some images in it !",
                        Toast.LENGTH_LONG).show();
            }

        } else {
            AlertDialog.Builder alert = new AlertDialog.Builder(getApplicationContext());
            alert.setTitle("Error!");
            alert.setMessage(PHOTO_ALBUM
                    + " directory path is not valid! Please set the image directory name AppConstant.java class");
            alert.setPositiveButton("OK", null);
            alert.show();
        }

        return filePaths;
    }

    // TODO: Remove
    // Check supported file extensions
    private boolean IsSupportedFile(String filePath) {
        String ext = filePath.substring((filePath.lastIndexOf(".") + 1),
                filePath.length());

        if (FILE_EXTN
                .contains(ext.toLowerCase(Locale.getDefault())))
            return true;
        else
            return false;

    }

}