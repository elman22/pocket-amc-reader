/*
    This file is part of Pocket AMC Reader.
    Copyright © 2010-2026 Elman <holdingscythe@zoznam.sk>

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
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.holdingscythe.pocketamcreader.R;
import com.holdingscythe.pocketamcreader.S;
import com.holdingscythe.pocketamcreader.model.MovieModel;
import com.holdingscythe.pocketamcreader.settings.SettingsConstants;
import com.holdingscythe.pocketamcreader.utils.SharedObjects;
import com.holdingscythe.pocketamcreader.utils.BitmapUtils;
import com.holdingscythe.pocketamcreader.utils.Utils;
import com.holdingscythe.pocketamcreader.views.ExpandableTextView;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

/**
 * Movie model
 * Created by Elman on 11.10.2014.
 */
@SuppressWarnings("FieldCanBeLocal")
public class Movie {
    // Longer side of the picture of the movie (bigger pictures are scaled down, the viewer shows the original)
    private static final int MAX_HERO_PICTURE_SIDE = 2048;

    // Views of catalog columns. The ids are listed here (and not looked up by name with Resources.getIdentifier())
    // so the compiler and the resource shrinker know the views are used.
    private static final Map<String, Integer> COLUMN_VIEW_IDS = new HashMap<>();
    private static final Map<String, Integer> COLUMN_LABEL_IDS = new HashMap<>();

    static {
        COLUMN_VIEW_IDS.put(Movies.NUMBER, R.id.Number);
        COLUMN_VIEW_IDS.put(Movies.CHECKED, R.id.Checked);
        COLUMN_VIEW_IDS.put(Movies.FORMATTED_TITLE, R.id.FormattedTitle);
        COLUMN_VIEW_IDS.put(Movies.MEDIA_LABEL, R.id.MediaLabel);
        COLUMN_VIEW_IDS.put(Movies.MEDIA_TYPE, R.id.MediaType);
        COLUMN_VIEW_IDS.put(Movies.SOURCE, R.id.Source);
        COLUMN_VIEW_IDS.put(Movies.DATE, R.id.Date);
        COLUMN_VIEW_IDS.put(Movies.BORROWER, R.id.Borrower);
        COLUMN_VIEW_IDS.put(Movies.RATING, R.id.Rating);
        COLUMN_VIEW_IDS.put(Movies.ORIGINAL_TITLE, R.id.OriginalTitle);
        COLUMN_VIEW_IDS.put(Movies.TRANSLATED_TITLE, R.id.TranslatedTitle);
        COLUMN_VIEW_IDS.put(Movies.DIRECTOR, R.id.Director);
        COLUMN_VIEW_IDS.put(Movies.PRODUCER, R.id.Producer);
        COLUMN_VIEW_IDS.put(Movies.COUNTRY, R.id.Country);
        COLUMN_VIEW_IDS.put(Movies.CATEGORY, R.id.Category);
        COLUMN_VIEW_IDS.put(Movies.LENGTH, R.id.Length);
        COLUMN_VIEW_IDS.put(Movies.YEAR, R.id.Year);
        COLUMN_VIEW_IDS.put(Movies.ACTORS, R.id.Actors);
        COLUMN_VIEW_IDS.put(Movies.URL, R.id.URL);
        COLUMN_VIEW_IDS.put(Movies.DESCRIPTION, R.id.Description);
        COLUMN_VIEW_IDS.put(Movies.COMMENTS, R.id.Comments);
        COLUMN_VIEW_IDS.put(Movies.VIDEO_FORMAT, R.id.VideoFormat);
        COLUMN_VIEW_IDS.put(Movies.VIDEO_BITRATE, R.id.VideoBitrate);
        COLUMN_VIEW_IDS.put(Movies.AUDIO_FORMAT, R.id.AudioFormat);
        COLUMN_VIEW_IDS.put(Movies.AUDIO_BITRATE, R.id.AudioBitrate);
        COLUMN_VIEW_IDS.put(Movies.RESOLUTION, R.id.Resolution);
        COLUMN_VIEW_IDS.put(Movies.FRAMERATE, R.id.Framerate);
        COLUMN_VIEW_IDS.put(Movies.LANGUAGES, R.id.Languages);
        COLUMN_VIEW_IDS.put(Movies.SUBTITLES, R.id.Subtitles);
        COLUMN_VIEW_IDS.put(Movies.SIZE, R.id.Size);
        COLUMN_VIEW_IDS.put(Movies.DISKS, R.id.Disks);
        COLUMN_VIEW_IDS.put(Movies.PICTURE, R.id.Picture);
        COLUMN_VIEW_IDS.put(Movies.COLOR_TAG, R.id.ColorTag);
        COLUMN_VIEW_IDS.put(Movies.DATE_WATCHED, R.id.DateWatched);
        COLUMN_VIEW_IDS.put(Movies.USER_RATING, R.id.UserRating);
        COLUMN_VIEW_IDS.put(Movies.WRITER, R.id.Writer);
        COLUMN_VIEW_IDS.put(Movies.COMPOSER, R.id.Composer);
        COLUMN_VIEW_IDS.put(Movies.CERTIFICATION, R.id.Certification);
        COLUMN_VIEW_IDS.put(Movies.FILE_PATH, R.id.FilePath);

        COLUMN_LABEL_IDS.put(Movies.CHECKED, R.id.CheckedLabel);
        COLUMN_LABEL_IDS.put(Movies.MEDIA_LABEL, R.id.MediaLabelLabel);
        COLUMN_LABEL_IDS.put(Movies.MEDIA_TYPE, R.id.MediaTypeLabel);
        COLUMN_LABEL_IDS.put(Movies.SOURCE, R.id.SourceLabel);
        COLUMN_LABEL_IDS.put(Movies.DATE, R.id.DateLabel);
        COLUMN_LABEL_IDS.put(Movies.BORROWER, R.id.BorrowerLabel);
        COLUMN_LABEL_IDS.put(Movies.RATING, R.id.RatingLabel);
        COLUMN_LABEL_IDS.put(Movies.ORIGINAL_TITLE, R.id.OriginalTitleLabel);
        COLUMN_LABEL_IDS.put(Movies.TRANSLATED_TITLE, R.id.TranslatedTitleLabel);
        COLUMN_LABEL_IDS.put(Movies.DIRECTOR, R.id.DirectorLabel);
        COLUMN_LABEL_IDS.put(Movies.PRODUCER, R.id.ProducerLabel);
        COLUMN_LABEL_IDS.put(Movies.COUNTRY, R.id.CountryLabel);
        COLUMN_LABEL_IDS.put(Movies.ACTORS, R.id.ActorsLabel);
        COLUMN_LABEL_IDS.put(Movies.URL, R.id.URLLabel);
        COLUMN_LABEL_IDS.put(Movies.COMMENTS, R.id.CommentsLabel);
        COLUMN_LABEL_IDS.put(Movies.VIDEO_FORMAT, R.id.VideoFormatLabel);
        COLUMN_LABEL_IDS.put(Movies.AUDIO_FORMAT, R.id.AudioFormatLabel);
        COLUMN_LABEL_IDS.put(Movies.RESOLUTION, R.id.ResolutionLabel);
        COLUMN_LABEL_IDS.put(Movies.FRAMERATE, R.id.FramerateLabel);
        COLUMN_LABEL_IDS.put(Movies.LANGUAGES, R.id.LanguagesLabel);
        COLUMN_LABEL_IDS.put(Movies.SUBTITLES, R.id.SubtitlesLabel);
        COLUMN_LABEL_IDS.put(Movies.SIZE, R.id.SizeLabel);
        COLUMN_LABEL_IDS.put(Movies.DATE_WATCHED, R.id.DateWatchedLabel);
        COLUMN_LABEL_IDS.put(Movies.USER_RATING, R.id.UserRatingLabel);
        COLUMN_LABEL_IDS.put(Movies.WRITER, R.id.WriterLabel);
        COLUMN_LABEL_IDS.put(Movies.COMPOSER, R.id.ComposerLabel);
        COLUMN_LABEL_IDS.put(Movies.CERTIFICATION, R.id.CertificationLabel);
        COLUMN_LABEL_IDS.put(Movies.FILE_PATH, R.id.FilePathLabel);
    }
    private MovieModel mMovieModel;
    private final View mView;
    private final View.OnClickListener mClickListener;
    private final Activity mActivity;
    private final Context mContext;
    private String mPreferencePicturesDirectory;
    private final ArrayList<ImageView> mPictureViews;
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
        mPreferencePicturesDirectory = mPreferences.getString(SettingsConstants.KEY_PREF_PICTURES_FOLDER, "/");
        boolean mPreferenceFilePathLink = mPreferences.getBoolean(SettingsConstants.KEY_PREF_FILE_PATH_LINK, false);
        boolean mColorTagTitle = mPreferences.getBoolean(SettingsConstants.KEY_PREF_COLOR_TAG_TITLE, false);

        // Fill model
        mMovieModel = new MovieModel(
                cursor.getString(cursor.getColumnIndexOrThrow(Movies.NUMBER)),
                cursor.getString(cursor.getColumnIndexOrThrow(Movies.CHECKED)),
                cursor.getString(cursor.getColumnIndexOrThrow(Movies.FORMATTED_TITLE)),
                cursor.getString(cursor.getColumnIndexOrThrow(Movies.MEDIA_LABEL)),
                cursor.getString(cursor.getColumnIndexOrThrow(Movies.MEDIA_TYPE)),
                cursor.getString(cursor.getColumnIndexOrThrow(Movies.SOURCE)),
                cursor.getString(cursor.getColumnIndexOrThrow(Movies.DATE)),
                cursor.getString(cursor.getColumnIndexOrThrow(Movies.BORROWER)),
                cursor.getString(cursor.getColumnIndexOrThrow(Movies.RATING)),
                cursor.getString(cursor.getColumnIndexOrThrow(Movies.ORIGINAL_TITLE)),
                cursor.getString(cursor.getColumnIndexOrThrow(Movies.TRANSLATED_TITLE)),
                cursor.getString(cursor.getColumnIndexOrThrow(Movies.DIRECTOR)),
                cursor.getString(cursor.getColumnIndexOrThrow(Movies.PRODUCER)),
                cursor.getString(cursor.getColumnIndexOrThrow(Movies.COUNTRY)),
                cursor.getString(cursor.getColumnIndexOrThrow(Movies.CATEGORY)),
                cursor.getString(cursor.getColumnIndexOrThrow(Movies.LENGTH)),
                cursor.getString(cursor.getColumnIndexOrThrow(Movies.YEAR)),
                cursor.getString(cursor.getColumnIndexOrThrow(Movies.ACTORS)),
                cursor.getString(cursor.getColumnIndexOrThrow(Movies.URL)),
                cursor.getString(cursor.getColumnIndexOrThrow(Movies.DESCRIPTION)),
                cursor.getString(cursor.getColumnIndexOrThrow(Movies.COMMENTS)),
                cursor.getString(cursor.getColumnIndexOrThrow(Movies.VIDEO_FORMAT)),
                cursor.getString(cursor.getColumnIndexOrThrow(Movies.VIDEO_BITRATE)),
                cursor.getString(cursor.getColumnIndexOrThrow(Movies.AUDIO_FORMAT)),
                cursor.getString(cursor.getColumnIndexOrThrow(Movies.AUDIO_BITRATE)),
                cursor.getString(cursor.getColumnIndexOrThrow(Movies.RESOLUTION)),
                cursor.getString(cursor.getColumnIndexOrThrow(Movies.FRAMERATE)),
                cursor.getString(cursor.getColumnIndexOrThrow(Movies.LANGUAGES)),
                cursor.getString(cursor.getColumnIndexOrThrow(Movies.SUBTITLES)),
                cursor.getString(cursor.getColumnIndexOrThrow(Movies.SIZE)),
                cursor.getString(cursor.getColumnIndexOrThrow(Movies.DISKS)),
                cursor.getString(cursor.getColumnIndexOrThrow(Movies.PICTURE)),
                cursor.getString(cursor.getColumnIndexOrThrow(Movies.COLOR_TAG)),
                cursor.getString(cursor.getColumnIndexOrThrow(Movies.DATE_WATCHED)),
                cursor.getString(cursor.getColumnIndexOrThrow(Movies.USER_RATING)),
                cursor.getString(cursor.getColumnIndexOrThrow(Movies.WRITER)),
                cursor.getString(cursor.getColumnIndexOrThrow(Movies.COMPOSER)),
                cursor.getString(cursor.getColumnIndexOrThrow(Movies.CERTIFICATION)),
                cursor.getString(cursor.getColumnIndexOrThrow(Movies.FILE_PATH))
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

        if (mPreferenceFilePathLink) {
            fillStringIntoView(Movies.FILE_PATH, mMovieModel.getFilePath(), STRING_CLICKABLE);
        } else {
            fillStringIntoView(Movies.FILE_PATH, mMovieModel.getFilePath(), STRING_REGULAR);
        }
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

        // Set movie color
        String colorTag = mMovieModel.getColorTag();
        int color = Utils.getColorFromColorTag(colorTag);
        boolean isCustomColorTagSet = Utils.isCustomColorTagSet(colorTag);

        if (S.DEBUG)
            Log.d(S.TAG, "Setting color to: " + colorTag);

        if (mColorTagTitle) {
            setTextColor(Movies.FORMATTED_TITLE, color);
            fillColorIntoView(Movies.COLOR_TAG, false, color);
        } else {
            fillColorIntoView(Movies.COLOR_TAG, isCustomColorTagSet, color);
        }
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
        if (value == null || value.isEmpty()) {
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
            case EXT_STRING_NONE -> {
                // nothing to do
            }
            case EXT_STRING_PREFIX -> value = mActivity.getString(valueExtensionId) + value;
            case EXT_STRING_SUFFIX -> value += mActivity.getString(valueExtensionId);
            case EXT_STRING_PREFIX_PADDED -> value = mActivity.getString(valueExtensionId) +
                    mActivity.getString(R.string.string_extension_separator) + value;
            case EXT_STRING_SUFFIX_PADDED -> value += mActivity.getString(R.string.string_extension_separator) +
                    mActivity.getString(valueExtensionId);
            case EXT_PLURALS_PREFIX_PADDED -> value = mActivity.getResources().getQuantityString(valueExtensionId,
                    Integer.parseInt(value)) + mActivity.getString(R.string.string_extension_separator) + value;
            case EXT_PLURALS_SUFFIX_PADDED -> value += mActivity.getString(R.string.string_extension_separator) +
                    mActivity.getResources().getQuantityString(valueExtensionId, Integer.parseInt(value));
        }

        // Fill strings into views
        switch (dataType) {
            case STRING_REGULAR -> {
                // Fill regular string from database into view.
                TextView tv = findColumnView(columnName);
                if (tv != null) {
                    tv.setText(value);
                }
            }
            case STRING_EXPANDABLE -> {
                // Fill expandable string from database into view.
                ExpandableTextView etv = findColumnView(columnName);
                if (etv != null) {
                    etv.setText(value);
                }
            }
            case STRING_CLICKABLE -> {
                // Fill clickable string from database into view.
                TextView ctv = findColumnView(columnName);
                if (ctv != null) {
                    ctv.setText(Utils.markClickableText(value));
                    ctv.setOnClickListener(mClickListener);
                }
            }
            case BOOLEAN_REGULAR -> {
                TextView btv = findColumnView(columnName);
                if (btv != null) {
                    if (value.equals(S.CATALOG_TRUE))
                        btv.setText(mActivity.getString(R.string.details_boolean_true));
                    else
                        btv.setText(mActivity.getString(R.string.details_boolean_false));
                }
            }
            case BOOLEAN_CLICKABLE -> {
                TextView bctv = findColumnView(columnName);
                if (bctv != null) {
                    if (value.equals(S.CATALOG_TRUE))
                        bctv.setText(Utils.markClickableText(mActivity.getString(R.string.details_boolean_true)));
                    else
                        bctv.setText(Utils.markClickableText(mActivity.getString(R.string.details_boolean_false)));
                    bctv.setOnClickListener(mClickListener);
                }
            }
            case DATE_REGULAR -> {
                TextView dtv = findColumnView(columnName);
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
            }
        }
    }

    /**
     * Fill image from database into view. Images can be clickable.
     */
    private void fillPictureIntoView(String columnName, String pictureName) {
        String picturePath = mPreferencePicturesDirectory + pictureName;
        ImageView iv = findColumnView(columnName);

        if (pictureName != null) {
            // picture in catalog is present
            if (S.DEBUG)
                Log.d(S.TAG, "Searching for picture: " + picturePath);

            File pictureFile = new File(picturePath);

            if (pictureFile.canRead()) {
                try {
                    Bitmap picture = BitmapUtils.decodeFile(picturePath, MAX_HERO_PICTURE_SIDE);
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
    private void fillColorIntoView(String columnName, boolean isCustomColorTagSet, int color) {
        TextView tv = findColumnView(columnName);
        if (tv != null) {
            if (isCustomColorTagSet) {
                tv.setVisibility(View.VISIBLE);
                tv.setBackgroundColor(ContextCompat.getColor(mContext, color));
            } else {
                tv.setVisibility(View.GONE);
            }
        }
    }

    /**
     * Set text color to a text view.
     */
    private void setTextColor(String columnName, int color) {
        TextView tv = findColumnView(columnName);
        if (tv != null) {
            tv.setTextColor(ContextCompat.getColor(mContext, color));
        }
    }

    /**
     * Find the view of a catalog column, null if the layout doesn't have it
     */
    @Nullable
    private <T extends View> T findColumnView(String columnName) {
        Integer id = COLUMN_VIEW_IDS.get(columnName);
        return id == null ? null : mView.findViewById(id);
    }

    /**
     * Hide label and view if empty
     */
    private void hideEmptyView(String columnName) {
        View v = findColumnView(columnName);
        if (v != null) {
            v.setVisibility(View.GONE);
        }

        // Also hide label, if shown
        Integer labelId = COLUMN_LABEL_IDS.get(columnName);
        v = labelId == null ? null : mView.findViewById(labelId);
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
     * Return color tag
     */
    public String getColorTag() {
        return mMovieModel.getColorTag();
    }

    /*
     * Unbind data
     */
    public void unbindData() {
        for (ImageView view : mPictureViews) {
            Drawable drawable = view.getDrawable();
            if (drawable instanceof BitmapDrawable bitmapDrawable) {
                Bitmap bitmap = bitmapDrawable.getBitmap();
                bitmap.recycle();
            }
        }
    }

}
