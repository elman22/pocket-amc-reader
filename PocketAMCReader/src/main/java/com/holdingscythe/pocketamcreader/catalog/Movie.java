package com.holdingscythe.pocketamcreader.catalog;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
 * PocketAMCReader
 * Created by Elman on 11.10.2014.
 */
public class Movie {
    private MovieModel mMovie;
    private View mView;
    private View.OnClickListener mClickListener;
    private Activity mActivity;
    private Resources mResources;
    private Context mContext;
    private String mPreferencePicturesDirectory;

    // Logical data types
    final int STRING_REGULAR = 0;
    final int STRING_EXPANDABLE = 1;
    final int STRING_CLICKABLE = 2;
    final int BOOLEAN_REGULAR = 3;
    final int BOOLEAN_CLICKABLE = 4;
    final int DATE_REGULAR = 5;

    // String extensions
    final int EXT_STRING_NONE = 0;
    final int EXT_STRING_PREFIX = 1;
    final int EXT_STRING_SUFFIX = 2;
    final int EXT_STRING_PREFIX_PADDED = 3;
    final int EXT_STRING_SUFFIX_PADDED = 4;
    final int EXT_PLURALS_PREFIX_PADDED = 5;
    final int EXT_PLURALS_SUFFIX_PADDED = 6;

    public Movie(Cursor cursor, View view, View.OnClickListener clickListener, Activity activity) {
        mView = view;
        mClickListener = clickListener;
        mActivity = activity;
        mResources = activity.getResources();
        mContext = activity.getBaseContext();

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
//        this.settingIMDb = this.preferences.getString("settingIMDb", "original");
//        this.settingDefaultTab = Integer.valueOf(this.preferences.getString("settingDefaultTab", "0"));
//        this.settingShowColorTags = this.preferences.getBoolean("settingShowColorTags", true);
//        this.settingMultifieldSeparator = this.preferences.getString("settingMultivalueSeparator", ",");
//        this.settingPlotInBasic = Integer.valueOf(this.preferences.getString("settingPlotInBasic", "1"));
//        this.settingFontSize = Integer.valueOf(this.preferences.getString("settingFontSize", "0"));
//        this.settingHideUnusedFields = this.preferences.getBoolean("hideUnusedFields", false);

        // Fill model
        mMovie = new MovieModel(
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
        fillPictureIntoView(Movies.PICTURE, mMovie.getPicture());

        fillStringIntoView(Movies.FORMATTED_TITLE, mMovie.getFormattedTitle(), STRING_REGULAR);
        fillStringIntoView(Movies.NUMBER, mMovie.getNumber(), STRING_REGULAR, EXT_STRING_PREFIX,
                R.string.number_prefix);
        fillStringIntoView(Movies.CERTIFICATION, mMovie.getCertification(), STRING_CLICKABLE);
        fillStringIntoView(Movies.CHECKED, mMovie.getChecked(), BOOLEAN_CLICKABLE);
        fillStringIntoView(Movies.USER_RATING, mMovie.getUserRating(), STRING_CLICKABLE);

        fillStringIntoView(Movies.YEAR, mMovie.getYear(), STRING_CLICKABLE, R.string.year_none);
        fillStringIntoView(Movies.LENGTH, mMovie.getLength(), STRING_CLICKABLE, R.string.length_none);
        fillStringIntoView(Movies.RATING, mMovie.getRating(), STRING_CLICKABLE, R.string.rating_none);

        fillStringIntoView(Movies.CATEGORY, mMovie.getCategory(), STRING_CLICKABLE);
        fillStringIntoView(Movies.DESCRIPTION, mMovie.getDescription(), STRING_EXPANDABLE);
        fillStringIntoView(Movies.DIRECTOR, mMovie.getDirector(), STRING_CLICKABLE);
        fillStringIntoView(Movies.ACTORS, mMovie.getActors(), STRING_CLICKABLE);
        fillStringIntoView(Movies.PRODUCER, mMovie.getProducer(), STRING_CLICKABLE);
        fillStringIntoView(Movies.WRITER, mMovie.getWriter(), STRING_CLICKABLE);
        fillStringIntoView(Movies.COMPOSER, mMovie.getComposer(), STRING_CLICKABLE);
        fillStringIntoView(Movies.COUNTRY, mMovie.getCountry(), STRING_CLICKABLE);
        fillStringIntoView(Movies.COMMENTS, mMovie.getComments(), STRING_EXPANDABLE);
        fillStringIntoView(Movies.URL, mMovie.getURL(), STRING_CLICKABLE);

        fillStringIntoView(Movies.MEDIA_LABEL, mMovie.getMediaLabel(), STRING_CLICKABLE);
        fillStringIntoView(Movies.MEDIA_TYPE, mMovie.getMediaType(), STRING_CLICKABLE);
        fillStringIntoView(Movies.SOURCE, mMovie.getSource(), STRING_CLICKABLE);
        fillStringIntoView(Movies.DATE, mMovie.getDate(), DATE_REGULAR);
        fillStringIntoView(Movies.DATE_WATCHED, mMovie.getDateWatched(), DATE_REGULAR);
        fillStringIntoView(Movies.BORROWER, mMovie.getBorrower(), STRING_CLICKABLE);
        fillStringIntoView(Movies.ORIGINAL_TITLE, mMovie.getOriginalTitle(), STRING_REGULAR);
        fillStringIntoView(Movies.TRANSLATED_TITLE, mMovie.getTranslatedTitle(), STRING_REGULAR);

        fillStringIntoView(Movies.FILE_PATH, mMovie.getFilePath(), STRING_REGULAR);
        fillStringIntoView(Movies.LANGUAGES, mMovie.getLanguages(), STRING_CLICKABLE);
        fillStringIntoView(Movies.SUBTITLES, mMovie.getSubtitles(), STRING_CLICKABLE);
        fillStringIntoView(Movies.VIDEO_FORMAT, mMovie.getVideoFormat(), STRING_CLICKABLE);
        fillStringIntoView(Movies.VIDEO_BITRATE, mMovie.getVideoBitrate(), STRING_REGULAR, EXT_STRING_SUFFIX_PADDED,
                R.string.display_bitrate_suffix);
        fillStringIntoView(Movies.AUDIO_FORMAT, mMovie.getAudioFormat(), STRING_CLICKABLE);
        fillStringIntoView(Movies.AUDIO_BITRATE, mMovie.getAudioBitrate(), STRING_REGULAR, EXT_STRING_SUFFIX_PADDED,
                R.string.display_bitrate_suffix);
        fillStringIntoView(Movies.RESOLUTION, mMovie.getResolution(), STRING_CLICKABLE);
        fillStringIntoView(Movies.FRAMERATE, mMovie.getFramerate(), STRING_CLICKABLE);
        fillStringIntoView(Movies.SIZE, mMovie.getSize(), STRING_REGULAR, EXT_STRING_SUFFIX_PADDED,
                R.string.display_filessizes_suffix);
        fillStringIntoView(Movies.DISKS, mMovie.getDisks(), STRING_REGULAR, EXT_PLURALS_SUFFIX_PADDED,
                R.plurals.details_disks);

        fillColorIntoView(Movies.COLOR_TAG, mMovie.getColorTag());
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
     * Params: column name, data type, extension type, extension value id,  default value id
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
                value = mActivity.getResources().getQuantityString(valueExtensionId, Integer.valueOf(value)) +
                        mActivity.getString(R.string.string_extension_separator) + value;
                break;
            case EXT_PLURALS_SUFFIX_PADDED:
                value += mActivity.getString(R.string.string_extension_separator) + mActivity.getResources()
                        .getQuantityString(valueExtensionId, Integer.valueOf(value));
                break;
        }

        // Fill strings into views
        switch (dataType) {
            case STRING_REGULAR:
                // Fill regular string from database into view.
                TextView tv = (TextView) mView.findViewById(mResources.getIdentifier(columnName, "id",
                        mContext.getPackageName()));
                tv.setText(value);
                break;
            case STRING_EXPANDABLE:
                // Fill expandable string from database into view.
                ExpandableTextView etv = (ExpandableTextView) mView.findViewById(mResources.getIdentifier
                        (columnName, "id", mContext.getPackageName()));
                etv.setText(value);
                break;
            case STRING_CLICKABLE:
                // Fill clickable string from database into view.
                TextView ctv = (TextView) mView.findViewById(mResources.getIdentifier(columnName, "id",
                        mContext.getPackageName()));
                ctv.setText(Utils.markClickableText(value));
                ctv.setOnClickListener(mClickListener);
                break;
            case BOOLEAN_REGULAR:
                TextView btv = (TextView) mView.findViewById(mResources.getIdentifier(columnName, "id",
                        mContext.getPackageName()));
                if (value.equals("True"))
                    btv.setText(mActivity.getString(R.string.details_boolean_true));
                else
                    btv.setText(mActivity.getString(R.string.details_boolean_false));
                break;
            case BOOLEAN_CLICKABLE:
                TextView bctv = (TextView) mView.findViewById(mResources.getIdentifier(columnName, "id",
                        mContext.getPackageName()));
                if (value.equals("True"))
                    bctv.setText(Utils.markClickableText(mActivity.getString(R.string.details_boolean_true)));
                else
                    bctv.setText(Utils.markClickableText(mActivity.getString(R.string.details_boolean_false)));
                bctv.setOnClickListener(mClickListener);
                break;
            case DATE_REGULAR:
                TextView dtv = (TextView) mView.findViewById(mResources.getIdentifier(columnName, "id",
                        mContext.getPackageName()));
                try {
                    Date parsedDate = SharedObjects.getInstance().dateAddedFormat.parse(value);
                    value = SharedObjects.getInstance().dateFormat.format(parsedDate);
                } catch (Exception e) {
                    // don't do anything, keep date as is
                }
                dtv.setText(value);
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
                } catch (Exception e) {
                    // file can't be read
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
        if (currentColor != null && S.COLOR_TAGS.containsKey(currentColor)) {
            if (S.DEBUG)
                Log.d(S.TAG, "Setting color to: " + currentColor);
            tv.setBackgroundColor(mContext.getResources().getColor(S.COLOR_TAGS.get(currentColor)));
        } else {
            tv.setBackgroundColor(mContext.getResources().getColor(S.COLOR_TAGS.get("0")));
        }
    }

    /**
     * Hide label and view if empty
     */
    private void hideEmptyView(String columnName) {
        View v = mView.findViewById(mResources.getIdentifier(columnName, "id", mContext.getPackageName()));
        v.setVisibility(View.GONE);

        // Also hide label, if shown
        String labelId = columnName + "Label";
        v = mView.findViewById(mResources.getIdentifier(labelId, "id", mContext.getPackageName()));
        if (v != null)
            v.setVisibility(View.GONE);
    }

    /*
     * Return array list with all available pictures for the movie
     */
    public ArrayList<String> getPicturesList() {
        ArrayList<String> pictureList = new ArrayList<>();
        pictureList.add(mPreferencePicturesDirectory + mMovie.getPicture());
        return pictureList;
    }

    /*
    * Return title of the movie
    */
    public String getTitle() {
        return mMovie.getFormattedTitle();
    }

}
