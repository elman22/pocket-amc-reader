/*
    This file is part of Pocket AMC Reader.
    Copyright © 2010-2017 Elman <holdingscythe@zoznam.sk>

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

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.holdingscythe.pocketamcreader.R;
import com.holdingscythe.pocketamcreader.S;
import com.holdingscythe.pocketamcreader.model.MovieModel;
import com.holdingscythe.pocketamcreader.utils.SharedObjects;
import com.holdingscythe.pocketamcreader.utils.Utils;
import com.ms.square.android.expandabletextview.ExpandableTextView;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;

/**
 * Movie model
 * Created by Elman on 11.10.2014.
 */
public class Movie {
    private MovieModel mMovieModel;
    private View mView;
    private View.OnClickListener mClickListener;
    private Activity mActivity;
    private Resources mResources;
    private Context mContext;
    private String mPreferencePicturesDirectory;
    private ArrayList<ImageView> mPictureViews;
    private double mPictureAspectRatio = 0;

    // Logical data types
    private final int STRING_REGULAR = 0;
    private final int STRING_EXPANDABLE = 1;
    private final int STRING_CLICKABLE = 2;
    private final int BOOLEAN_REGULAR = 3;
    private final int BOOLEAN_CLICKABLE = 4;
    private final int DATE_REGULAR = 5;

    // String extensions
    private final int EXT_STRING_NONE = 0;
    private final int EXT_STRING_PREFIX = 1;
    private final int EXT_STRING_SUFFIX = 2;
    private final int EXT_STRING_PREFIX_PADDED = 3;
    private final int EXT_STRING_SUFFIX_PADDED = 4;
    private final int EXT_PLURALS_PREFIX_PADDED = 5;
    private final int EXT_PLURALS_SUFFIX_PADDED = 6;

    public Movie(Cursor cursor, View view, View.OnClickListener clickListener, Activity activity) {
        mView = view;
        mClickListener = clickListener;
        mActivity = activity;
        mResources = activity.getResources();
        mContext = activity.getBaseContext();
        mPictureViews = new ArrayList<>();

        // Verify that cursor exists
        if (cursor == null || cursor.getCount() == 0) {
            if (S.ERROR) {
                Log.e(S.TAG, "Movie details not found!");
            }

            return;
        }

        // Read preferences
        SharedPreferences mPreferences = SharedObjects.getInstance().preferences;
        mPreferencePicturesDirectory = mPreferences.getString("settingPicturesFolder", "/");

        // Fill model
        mMovieModel = new MovieModel(
                cursor.getString(cursor.getColumnIndex(Movies.NUMBER)),
                cursor.getString(cursor.getColumnIndex(Movies.CHECKED)),
                cursor.getString(cursor.getColumnIndex(Movies.FORMATTED_TITLE)),
                cursor.getString(cursor.getColumnIndex(Movies.MEDIA_LABEL)),
                cursor.getString(cursor.getColumnIndex(Movies.MEDIA_TYPE)),
                cursor.getString(cursor.getColumnIndex(Movies.SOURCE)),
                cursor.getString(cursor.getColumnIndex(Movies.DATE)),
                cursor.getString(cursor.getColumnIndex(Movies.BORROWER)),
                cursor.getString(cursor.getColumnIndex(Movies.RATING)),
                cursor.getString(cursor.getColumnIndex(Movies.ORIGINAL_TITLE)),
                cursor.getString(cursor.getColumnIndex(Movies.TRANSLATED_TITLE)),
                cursor.getString(cursor.getColumnIndex(Movies.DIRECTOR)),
                cursor.getString(cursor.getColumnIndex(Movies.PRODUCER)),
                cursor.getString(cursor.getColumnIndex(Movies.COUNTRY)),
                cursor.getString(cursor.getColumnIndex(Movies.CATEGORY)),
                cursor.getString(cursor.getColumnIndex(Movies.LENGTH)),
                cursor.getString(cursor.getColumnIndex(Movies.YEAR)),
                cursor.getString(cursor.getColumnIndex(Movies.ACTORS)),
                cursor.getString(cursor.getColumnIndex(Movies.URL)),
                cursor.getString(cursor.getColumnIndex(Movies.DESCRIPTION)),
                cursor.getString(cursor.getColumnIndex(Movies.COMMENTS)),
                cursor.getString(cursor.getColumnIndex(Movies.VIDEO_FORMAT)),
                cursor.getString(cursor.getColumnIndex(Movies.VIDEO_BITRATE)),
                cursor.getString(cursor.getColumnIndex(Movies.AUDIO_FORMAT)),
                cursor.getString(cursor.getColumnIndex(Movies.AUDIO_BITRATE)),
                cursor.getString(cursor.getColumnIndex(Movies.RESOLUTION)),
                cursor.getString(cursor.getColumnIndex(Movies.FRAMERATE)),
                cursor.getString(cursor.getColumnIndex(Movies.LANGUAGES)),
                cursor.getString(cursor.getColumnIndex(Movies.SUBTITLES)),
                cursor.getString(cursor.getColumnIndex(Movies.SIZE)),
                cursor.getString(cursor.getColumnIndex(Movies.DISKS)),
                cursor.getString(cursor.getColumnIndex(Movies.PICTURE)),
                cursor.getString(cursor.getColumnIndex(Movies.COLOR_TAG)),
                cursor.getString(cursor.getColumnIndex(Movies.DATE_WATCHED)),
                cursor.getString(cursor.getColumnIndex(Movies.USER_RATING)),
                cursor.getString(cursor.getColumnIndex(Movies.WRITER)),
                cursor.getString(cursor.getColumnIndex(Movies.COMPOSER)),
                cursor.getString(cursor.getColumnIndex(Movies.CERTIFICATION)),
                cursor.getString(cursor.getColumnIndex(Movies.FILE_PATH))
        );

        // Display data
        fillPictureIntoView(Movies.PICTURE, mMovieModel.getPicture());

        fillStringIntoView(Movies.FORMATTED_TITLE, mMovieModel.getFormattedTitle(), STRING_REGULAR);
        fillStringIntoView(Movies.NUMBER, mMovieModel.getNumber(), STRING_REGULAR, EXT_STRING_PREFIX,
                R.string.number_prefix);
        fillStringIntoView(Movies.CERTIFICATION, mMovieModel.getCertification(), STRING_CLICKABLE);
        fillStringIntoView(Movies.CHECKED, mMovieModel.getChecked(), BOOLEAN_CLICKABLE);
        fillStringIntoView(Movies.USER_RATING, mMovieModel.getUserRating(), STRING_CLICKABLE);

        fillStringIntoView(Movies.YEAR, mMovieModel.getYear(), STRING_CLICKABLE, R.string.year_none);
        fillStringIntoView(Movies.LENGTH, mMovieModel.getLength(), STRING_CLICKABLE, R.string.length_none);
        fillStringIntoView(Movies.RATING, mMovieModel.getRating(), STRING_CLICKABLE, R.string.rating_none);

        fillStringIntoView(Movies.CATEGORY, mMovieModel.getCategory(), STRING_CLICKABLE);
        fillStringIntoView(Movies.DESCRIPTION, mMovieModel.getDescription(), STRING_EXPANDABLE);
        fillStringIntoView(Movies.DIRECTOR, mMovieModel.getDirector(), STRING_CLICKABLE);
        fillStringIntoView(Movies.ACTORS, mMovieModel.getActors(), STRING_CLICKABLE);
        fillStringIntoView(Movies.PRODUCER, mMovieModel.getProducer(), STRING_CLICKABLE);
        fillStringIntoView(Movies.WRITER, mMovieModel.getWriter(), STRING_CLICKABLE);
        fillStringIntoView(Movies.COMPOSER, mMovieModel.getComposer(), STRING_CLICKABLE);
        fillStringIntoView(Movies.COUNTRY, mMovieModel.getCountry(), STRING_CLICKABLE);
        fillStringIntoView(Movies.COMMENTS, mMovieModel.getComments(), STRING_EXPANDABLE);
        fillStringIntoView(Movies.URL, mMovieModel.getURL(), STRING_CLICKABLE);

        fillStringIntoView(Movies.MEDIA_LABEL, mMovieModel.getMediaLabel(), STRING_CLICKABLE);
        fillStringIntoView(Movies.MEDIA_TYPE, mMovieModel.getMediaType(), STRING_CLICKABLE);
        fillStringIntoView(Movies.SOURCE, mMovieModel.getSource(), STRING_CLICKABLE);
        fillStringIntoView(Movies.DATE, mMovieModel.getDate(), DATE_REGULAR);
        fillStringIntoView(Movies.DATE_WATCHED, mMovieModel.getDateWatched(), DATE_REGULAR);
        fillStringIntoView(Movies.BORROWER, mMovieModel.getBorrower(), STRING_CLICKABLE);
        fillStringIntoView(Movies.ORIGINAL_TITLE, mMovieModel.getOriginalTitle(), STRING_REGULAR);
        fillStringIntoView(Movies.TRANSLATED_TITLE, mMovieModel.getTranslatedTitle(), STRING_REGULAR);

        fillStringIntoView(Movies.FILE_PATH, mMovieModel.getFilePath(), STRING_REGULAR);
        fillStringIntoView(Movies.LANGUAGES, mMovieModel.getLanguages(), STRING_CLICKABLE);
        fillStringIntoView(Movies.SUBTITLES, mMovieModel.getSubtitles(), STRING_CLICKABLE);
        fillStringIntoView(Movies.VIDEO_FORMAT, mMovieModel.getVideoFormat(), STRING_CLICKABLE);
        fillStringIntoView(Movies.VIDEO_BITRATE, mMovieModel.getVideoBitrate(), STRING_REGULAR,
                EXT_STRING_SUFFIX_PADDED, R.string.display_bitrate_suffix);
        fillStringIntoView(Movies.AUDIO_FORMAT, mMovieModel.getAudioFormat(), STRING_CLICKABLE);
        fillStringIntoView(Movies.AUDIO_BITRATE, mMovieModel.getAudioBitrate(), STRING_REGULAR,
                EXT_STRING_SUFFIX_PADDED, R.string.display_bitrate_suffix);
        fillStringIntoView(Movies.RESOLUTION, mMovieModel.getResolution(), STRING_CLICKABLE);
        fillStringIntoView(Movies.FRAMERATE, mMovieModel.getFramerate(), STRING_CLICKABLE);
        fillStringIntoView(Movies.SIZE, mMovieModel.getSize(), STRING_REGULAR,
                EXT_STRING_SUFFIX_PADDED, R.string.display_filessizes_suffix);
        fillStringIntoView(Movies.DISKS, mMovieModel.getDisks(), STRING_REGULAR,
                EXT_PLURALS_SUFFIX_PADDED, R.plurals.details_disks);

        fillColorIntoView(Movies.COLOR_TAG, mMovieModel.getColorTag());
    }

    /**
     * Fill string from database into view. String can be clickable visibly or invisibly.
     * Params: column name, data type
     */
    private void fillStringIntoView(String columnName, String value, int dataType) {
        fillStringIntoView(columnName, value, dataType, EXT_STRING_NONE, 0, 0);
    }

    /**
     * Fill string from database into view. String can be clickable visibly or invisibly.
     * Params: column name, data type, default value id
     */
    private void fillStringIntoView(String columnName, String value, int dataType, int defaultValueId) {
        fillStringIntoView(columnName, value, dataType, EXT_STRING_NONE, 0, defaultValueId);
    }

    /**
     * Fill string from database into view. String can be clickable visibly or invisibly.
     * Params: column name, data type, extension type, extension value id
     */
    private void fillStringIntoView(String columnName, String value, int dataType, int valueExtensionType, int
            valueExtensionId) {
        fillStringIntoView(columnName, value, dataType, valueExtensionType, valueExtensionId, 0);
    }

    /**
     * Fill string from database into view. String can be clickable visibly or invisibly.
     * If updating formatting, be sure also to check formatting in @CustomFields.java
     *
     * @param columnName         column name
     * @param value              column value
     * @param dataType           column data type
     * @param valueExtensionType extension type
     * @param valueExtensionId   extension id
     * @param defaultValueId     default value to be shown if empty
     */
    private void fillStringIntoView(String columnName, String value, int dataType, int valueExtensionType, int
            valueExtensionId, int defaultValueId) {

        // If field has no value, either hide it or present default value
        if (value == null || value.equals("")) {
            if (defaultValueId == 0) {
                hideEmptyView(columnName);
                return;
            } else {
                value = mActivity.getString(defaultValueId);
                dataType = STRING_REGULAR;
            }
        }

        // Process string extension
        switch (valueExtensionType) {
            case EXT_STRING_NONE:
                break;
            case EXT_STRING_PREFIX:
                value = mActivity.getString(valueExtensionId) + value;
                break;
            case EXT_STRING_SUFFIX:
                value += mActivity.getString(valueExtensionId);
                break;
            case EXT_STRING_PREFIX_PADDED:
                value = mActivity.getString(valueExtensionId) + mActivity.getString(R.string
                        .string_extension_separator) + value;
                break;
            case EXT_STRING_SUFFIX_PADDED:
                value += mActivity.getString(R.string.string_extension_separator) + mActivity.getString
                        (valueExtensionId);
                break;
            case EXT_PLURALS_PREFIX_PADDED:
                value = mActivity.getResources().getQuantityString(valueExtensionId, Integer.parseInt(value)) +
                        mActivity.getString(R.string.string_extension_separator) + value;
                break;
            case EXT_PLURALS_SUFFIX_PADDED:
                value += mActivity.getString(R.string.string_extension_separator) + mActivity.getResources()
                        .getQuantityString(valueExtensionId, Integer.parseInt(value));
                break;
        }

        // Fill strings into views
        switch (dataType) {
            case STRING_REGULAR:
                // Fill regular string from database into view.
                TextView tv = mView.findViewById(mResources.getIdentifier(columnName, "id", mContext.getPackageName()));
                if (tv != null) {
                    tv.setText(value);
                }
                break;
            case STRING_EXPANDABLE:
                // Fill expandable string from database into view.
                ExpandableTextView etv = mView.findViewById(mResources.getIdentifier(columnName, "id", mContext.getPackageName()));
                if (etv != null) {
                    etv.setText(value);
                }
                break;
            case STRING_CLICKABLE:
                // Fill clickable string from database into view.
                TextView ctv = mView.findViewById(mResources.getIdentifier(columnName, "id", mContext.getPackageName()));
                if (ctv != null) {
                    ctv.setText(Utils.markClickableText(value));
                    ctv.setOnClickListener(mClickListener);
                }
                break;
            case BOOLEAN_REGULAR:
                TextView btv = mView.findViewById(mResources.getIdentifier(columnName, "id", mContext.getPackageName()));
                if (btv != null) {
                    if (value.equals(S.CATALOG_TRUE))
                        btv.setText(mActivity.getString(R.string.details_boolean_true));
                    else
                        btv.setText(mActivity.getString(R.string.details_boolean_false));
                }
                break;
            case BOOLEAN_CLICKABLE:
                TextView bctv = mView.findViewById(mResources.getIdentifier(columnName, "id", mContext.getPackageName()));
                if (bctv != null) {
                    if (value.equals(S.CATALOG_TRUE))
                        bctv.setText(Utils.markClickableText(mActivity.getString(R.string.details_boolean_true)));
                    else
                        bctv.setText(Utils.markClickableText(mActivity.getString(R.string.details_boolean_false)));
                    bctv.setOnClickListener(mClickListener);
                }
                break;
            case DATE_REGULAR:
                TextView dtv = mView.findViewById(mResources.getIdentifier(columnName, "id", mContext.getPackageName()));
                if (dtv != null) {
                    try {
                        Date parsedDate = SharedObjects.getInstance().dateAddedFormat.parse(value);
                        assert parsedDate != null;
                        value = SharedObjects.getInstance().dateFormat.format(parsedDate);
                    } catch (Exception e) {
                        // don't do anything, keep date as is
                    }
                    dtv.setText(value);
                }
                break;
        }
    }

    /**
     * Fill image from database into view. Images can be clickable.
     */
    private void fillPictureIntoView(String columnName, String pictureName) {
        String picturePath = mPreferencePicturesDirectory + pictureName;
        ImageView iv = (ImageView) mView.findViewById(mResources.getIdentifier(columnName, "id",
                mContext.getPackageName()));

        if (pictureName != null) {
            // picture in catalog is present
            if (S.DEBUG)
                Log.d(S.TAG, "Searching for picture: " + picturePath);

            File pictureFile = new File(picturePath);

            if (pictureFile.canRead()) {
                try {
                    Bitmap picture = BitmapFactory.decodeFile(picturePath);
                    iv.setImageBitmap(picture);
                    iv.setOnClickListener(mClickListener);

                    // Save image aspect ratio
                    mPictureAspectRatio = (double) picture.getWidth() / (double) picture.getHeight();

                    // Save reference for cleanup
                    mPictureViews.add(iv);

                } catch (Exception e) {
                    // File can't be read
                    if (S.ERROR)
                        Log.e(S.TAG, "Picture " + picturePath + " could not be displayed.");
                }
            }
        } else {
            // TODO: Set view.GONE
        }
    }

    /**
     * Fill view with selected color.
     */
    private void fillColorIntoView(String columnName, String currentColor) {
        TextView tv = (TextView) mView.findViewById(mResources.getIdentifier(columnName, "id",
                mContext.getPackageName()));
        if (tv != null) {
            if (currentColor != null && S.COLOR_TAGS.containsKey(currentColor)) {
                if (S.DEBUG)
                    Log.d(S.TAG, "Setting color to: " + currentColor);
                tv.setBackgroundColor(mContext.getResources().getColor(S.COLOR_TAGS.get(currentColor)));
            } else {
                tv.setBackgroundColor(mContext.getResources().getColor(S.COLOR_TAGS.get("0")));
            }
        }
    }

    /**
     * Hide label and view if empty
     */
    private void hideEmptyView(String columnName) {
        View v = mView.findViewById(mResources.getIdentifier(columnName, "id", mContext.getPackageName()));
        if (v != null) {
            v.setVisibility(View.GONE);
        }

        // Also hide label, if shown
        String labelId = columnName + "Label";
        v = mView.findViewById(mResources.getIdentifier(labelId, "id", mContext.getPackageName()));
        if (v != null) {
            v.setVisibility(View.GONE);
        }
    }

    /*
     * Return array list with all available pictures for the movie
     */
    public ArrayList<String> getPicturesList() {
        ArrayList<String> pictureList = new ArrayList<>();
        pictureList.add(mPreferencePicturesDirectory + mMovieModel.getPicture());
        return pictureList;
    }

    /*
     * Return title of the movie
     */
    public String getTitle() {
        return mMovieModel.getFormattedTitle();
    }

    /*
     * Return picture aspect ratio
     */
    public double getPictureAspectRatio() {
        return mPictureAspectRatio;
    }

    /*
     * Unbind data
     */
    public void unbindData() {
        for (ImageView view : mPictureViews) {
            Drawable drawable = view.getDrawable();
            if (drawable instanceof BitmapDrawable) {
                BitmapDrawable bitmapDrawable = (BitmapDrawable) drawable;
                Bitmap bitmap = bitmapDrawable.getBitmap();
                bitmap.recycle();
            }
        }
    }

}
