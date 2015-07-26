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

import com.holdingscythe.pocketamcreader.S;
import com.holdingscythe.pocketamcreader.utils.SharedObjects;
import com.holdingscythe.pocketamcreader.utils.Utils;
import com.ms.square.android.expandabletextview.ExpandableTextView;

import java.io.File;

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

//        public static final String NUMBER = "Number";
//        public static final String CHECKED = "Checked";
        fillStringIntoView(Movies.FORMATTED_TITLE, STRING_REGULAR);
//        public static final String MEDIA_LABEL = "MediaLabel";
//        public static final String MEDIA_TYPE = "MediaType";
//        public static final String SOURCE = "Source";
//        public static final String DATE = "Date";
//        public static final String BORROWER = "Borrower";
//        public static final String RATING = "Rating";
//        public static final String ORIGINAL_TITLE = "OriginalTitle";
//        public static final String TRANSLATED_TITLE = "TranslatedTitle";
//        public static final String DIRECTOR = "Director";
//        public static final String PRODUCER = "Producer";
//        public static final String COUNTRY = "Country";
//        public static final String CATEGORY = "Category";
//        public static final String LENGTH = "Length";
//        public static final String YEAR = "Year";
        fillStringIntoView(Movies.ACTORS, STRING_CLICKABLE);
//        public static final String URL = "URL";
        fillStringIntoView(Movies.DESCRIPTION, STRING_EXPANDABLE);
        fillStringIntoView(Movies.COMMENTS, STRING_EXPANDABLE);
//        public static final String VIDEO_FORMAT = "VideoFormat";
//        public static final String VIDEO_BITRATE = "VideoBitrate";
//        public static final String AUDIO_FORMAT = "AudioFormat";
//        public static final String AUDIO_BITRATE = "AudioBitrate";
//        public static final String RESOLUTION = "Resolution";
//        public static final String FRAMERATE = "Framerate";
//        public static final String LANGUAGES = "Languages";
//        public static final String SUBTITLES = "Subtitles";
//        public static final String SIZE = "Size";
//        public static final String DISKS = "Disks";
        fillPictureIntoView(Movies.PICTURE);
//        public static final String COLOR_TAG = "ColorTag";
//        public static final String DATE_WATCHED = "DateWatched";
//        public static final String USER_RATING = "UserRating";
//        public static final String WRITER = "Writer";
//        public static final String COMPOSER = "Composer";
//        public static final String CERTIFICATION = "Certification";
        fillStringIntoView(Movies.FILE_PATH, STRING_REGULAR);
//
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

//        /** Fill views: Basic */
//        // Title + Repaint according to color tag
//        TextView mFormattedTitle = fillStringValueIntoView(new int[] { R.id.detail_FormattedTitle },
//                R.id.detail_FormattedTitle, Movies.FormattedTitle, Movies.VALUE_NO_CLICK);
//        if (this.settingShowColorTags && mFormattedTitle != null) {
//            String currentColor = c.getString(c.getColumnIndex(Movies.ColorTag));
//            if (currentColor != null && Movies.ColorTags.containsKey(currentColor)) {
//                if (Movies.LOG_ENABLED)
//                    Log.d(Movies.TAG, "Setting color to: " + currentColor);
//                mFormattedTitle.setTextColor(getBaseContext().getResources().getColor(Movies.ColorTags.get(currentColor)));
//            } else {
//                mFormattedTitle.setTextColor(getBaseContext().getResources().getColor(Movies.ColorTags.get("0")));
//            }
//        }


        ////        String catalogPicture = mCursor.getString(mCursor.getColumnIndex(Movies.PICTURE));
////        ImageView mPictureThumb = (ImageView) this.vt.findCachedViewById(R.id.detail_Picture_thumb);
////        ImageView mPictureTeaser = (ImageView) rootView.f findViewById()


    }

    /**
     * Fill string from database into view. String can be clickable visibly or invisibly.
     */
    private void fillStringIntoView(String columnName, int dateType) {
        String value = mCursor.getString(mCursor.getColumnIndex(columnName));

        if (value == null || value.equals("")) {
            hideEmptyView(columnName);
        } else {
            switch (dateType) {
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
            }
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

}
