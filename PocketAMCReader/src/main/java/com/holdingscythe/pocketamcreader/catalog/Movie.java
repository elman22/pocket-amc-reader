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
    private Cursor mCursor;
    private View mView;
    private View.OnClickListener mClickListener;
    private Activity mActivity;
    private Resources mResources;
    private Context mContext;

    private SharedPreferences mPreferences;
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
        mCursor = cursor;
        mView = view;
        mClickListener = clickListener;
        mActivity = activity;
        mResources = activity.getResources();
        mContext = activity.getBaseContext();

        // Verify that cursor exists
        if (mCursor == null || mCursor.getCount() == 0) {
            if (S.ERROR) {
                Log.e(S.TAG, "Movie details not found!");
            }

            return;
        }

        // Read preferences
        mPreferences = SharedObjects.getInstance().preferences;
        mPreferencePicturesDirectory = mPreferences.getString("settingPicturesFolder", "/");
//        this.settingIMDb = this.preferences.getString("settingIMDb", "original");
//        this.settingDefaultTab = Integer.valueOf(this.preferences.getString("settingDefaultTab", "0"));
//        this.settingShowColorTags = this.preferences.getBoolean("settingShowColorTags", true);
//        this.settingMultifieldSeparator = this.preferences.getString("settingMultivalueSeparator", ",");
//        this.settingPlotInBasic = Integer.valueOf(this.preferences.getString("settingPlotInBasic", "1"));
//        this.settingFontSize = Integer.valueOf(this.preferences.getString("settingFontSize", "0"));
//        this.settingHideUnusedFields = this.preferences.getBoolean("hideUnusedFields", false);

        // Display data
        fillPictureIntoView(Movies.PICTURE);

        fillStringIntoView(Movies.FORMATTED_TITLE, STRING_REGULAR);
        fillStringIntoView(Movies.NUMBER, STRING_REGULAR, EXT_STRING_PREFIX, R.string.number_prefix);
        fillStringIntoView(Movies.CERTIFICATION, STRING_CLICKABLE);
        fillStringIntoView(Movies.CHECKED, BOOLEAN_CLICKABLE);
        fillStringIntoView(Movies.USER_RATING, STRING_CLICKABLE);

        fillStringIntoView(Movies.YEAR, STRING_CLICKABLE, R.string.year_none);
        fillStringIntoView(Movies.LENGTH, STRING_CLICKABLE, R.string.length_none);
        fillStringIntoView(Movies.RATING, STRING_CLICKABLE, R.string.rating_none);

        fillStringIntoView(Movies.CATEGORY, STRING_CLICKABLE);
        fillStringIntoView(Movies.DESCRIPTION, STRING_EXPANDABLE);
        fillStringIntoView(Movies.DIRECTOR, STRING_CLICKABLE);
        fillStringIntoView(Movies.ACTORS, STRING_CLICKABLE);
        fillStringIntoView(Movies.PRODUCER, STRING_CLICKABLE);
        fillStringIntoView(Movies.WRITER, STRING_CLICKABLE);
        fillStringIntoView(Movies.COMPOSER, STRING_CLICKABLE);
        fillStringIntoView(Movies.COUNTRY, STRING_CLICKABLE);
        fillStringIntoView(Movies.COMMENTS, STRING_EXPANDABLE);
        fillStringIntoView(Movies.URL, STRING_REGULAR);

        fillStringIntoView(Movies.MEDIA_LABEL, STRING_CLICKABLE);
        fillStringIntoView(Movies.MEDIA_TYPE, STRING_CLICKABLE);
        fillStringIntoView(Movies.SOURCE, STRING_CLICKABLE);
        fillStringIntoView(Movies.DATE, DATE_REGULAR);
        fillStringIntoView(Movies.DATE_WATCHED, DATE_REGULAR);
        fillStringIntoView(Movies.BORROWER, STRING_CLICKABLE);
        fillStringIntoView(Movies.ORIGINAL_TITLE, STRING_REGULAR);
        fillStringIntoView(Movies.TRANSLATED_TITLE, STRING_REGULAR);

        fillStringIntoView(Movies.FILE_PATH, STRING_REGULAR);
        fillStringIntoView(Movies.LANGUAGES, STRING_CLICKABLE);
        fillStringIntoView(Movies.SUBTITLES, STRING_CLICKABLE);
        fillStringIntoView(Movies.VIDEO_FORMAT, STRING_CLICKABLE);
        fillStringIntoView(Movies.VIDEO_BITRATE, STRING_REGULAR, EXT_STRING_SUFFIX_PADDED, R.string
                .display_bitrate_suffix);
        fillStringIntoView(Movies.AUDIO_FORMAT, STRING_CLICKABLE);
        fillStringIntoView(Movies.AUDIO_BITRATE, STRING_REGULAR, EXT_STRING_SUFFIX_PADDED, R.string
                .display_bitrate_suffix);
        fillStringIntoView(Movies.RESOLUTION, STRING_CLICKABLE);
        fillStringIntoView(Movies.FRAMERATE, STRING_CLICKABLE);
        fillStringIntoView(Movies.SIZE, STRING_REGULAR, EXT_STRING_SUFFIX_PADDED, R.string.display_filessizes_suffix);
        fillStringIntoView(Movies.DISKS, STRING_REGULAR, EXT_PLURALS_SUFFIX_PADDED, R.plurals.details_disks);

        fillColorIntoView(Movies.COLOR_TAG);

//        // Fields mappings extras
//        public static final String MOVIES_ID = "Movies_id";
//        public static final String E_CHECKED = "EChecked";
//        public static final String E_TAG = "ETag";
//        public static final String E_TITLE = "ETitle";
//        public static final String E_CATEGORY = "ECategory";
//        public static final String E_URL = "EURL";
//        public static final String E_DESCRIPTION = "EDescription";
//        public static final String E_COMMENTS = "EComments";
//        public static final String E_CREATED_BY = "ECreatedBy";
//        public static final String E_PICTURE = "EPicture";

    }

    /**
     * Fill string from database into view. String can be clickable visibly or invisibly.
     * Params: column name, data type
     */
    private void fillStringIntoView(String columnName, int dataType) {
        fillStringIntoView(columnName, dataType, EXT_STRING_NONE, 0, 0);
    }

    /**
     * Fill string from database into view. String can be clickable visibly or invisibly.
     * Params: column name, data type, default value id
     */
    private void fillStringIntoView(String columnName, int dataType, int defaultValueId) {
        fillStringIntoView(columnName, dataType, EXT_STRING_NONE, 0, defaultValueId);
    }

    /**
     * Fill string from database into view. String can be clickable visibly or invisibly.
     * Params: column name, data type, extension type, extension value id
     */
    private void fillStringIntoView(String columnName, int dataType, int valueExtensionType, int valueExtensionId) {
        fillStringIntoView(columnName, dataType, valueExtensionType, valueExtensionId, 0);
    }

    /**
     * Fill string from database into view. String can be clickable visibly or invisibly.
     * Params: column name, data type, extension type, extension value id,  default value id
     */
    private void fillStringIntoView(String columnName, int dataType, int valueExtensionType, int valueExtensionId,
                                    int defaultValueId) {
        String value = mCursor.getString(mCursor.getColumnIndex(columnName));

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
                /** Fill regular string from database into view. */
                TextView tv = (TextView) mView.findViewById(mResources.getIdentifier(columnName, "id",
                        mContext.getPackageName()));
                tv.setText(value);
                break;
            case STRING_EXPANDABLE:
                /** Fill expandable string from database into view. */
                ExpandableTextView etv = (ExpandableTextView) mView.findViewById(mResources.getIdentifier
                        (columnName, "id", mContext.getPackageName()));
                etv.setText(value);
                break;
            case STRING_CLICKABLE:
                /** Fill clickable string from database into view. */
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
    private void fillPictureIntoView(String columnName) {
        String pictureName = mCursor.getString(mCursor.getColumnIndex(columnName));
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
    private void fillColorIntoView(String columnName) {
        TextView tv = (TextView) mView.findViewById(mResources.getIdentifier(columnName, "id",
                mContext.getPackageName()));
        String currentColor = mCursor.getString(mCursor.getColumnIndex(columnName));
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
        ArrayList<String> pictureList = new ArrayList<String>();

        // get main picture
        String pictureName = mCursor.getString(mCursor.getColumnIndex(Movies.PICTURE));
        pictureList.add(mPreferencePicturesDirectory + pictureName);

        // TODO get extras

        return pictureList;
    }

}
