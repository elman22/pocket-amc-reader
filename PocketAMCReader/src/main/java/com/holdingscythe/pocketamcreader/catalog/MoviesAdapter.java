package com.holdingscythe.pocketamcreader.catalog;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import com.holdingscythe.pocketamcreader.R;
import com.holdingscythe.pocketamcreader.S;
import com.holdingscythe.pocketamcreader.utils.SharedObjects;
import com.holdingscythe.pocketamcreader.utils.Utils;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.imageaware.ImageViewAware;

import java.util.ArrayList;
import java.util.Date;

public class MoviesAdapter extends SimpleCursorAdapter {
    private int mLayout;
    private Boolean mShowThumbs;
    private String mPicturesFolder;
    private String mSortOrder;
    private String[] mListFieldsLine1;
    private String[] mListFieldsLine2;
    private String[] mListFieldsLine3;
    private Boolean mSettingListForceSortField;
    private String mSettingMoviesListSeparator;
    private ImageLoader mImageLoader;
    private DisplayImageOptions mImageOptions;

    private static class MovieHolder {
        TextView FormattedTitle_text;
        TextView movieShortDescription_text;
        TextView movieShortDescription2_text;
        TextView movieColorTag;
        ImageViewAware moviePictureView;
    }

    public MoviesAdapter(Activity a, int layout, Cursor c, String[] from, int[] to) {
        // If set the adapter will register a content observer on the cursor and will call onContentChanged()
        // when a notification comes in. Be careful when using this flag: you will need to unset the current Cursor
        // from the adapter to avoid leaks due to its registered observers. This flag is not needed when using a
        // CursorAdapter with a CursorLoader.
        super(a.getBaseContext(), layout, c, from, to, FLAG_REGISTER_CONTENT_OBSERVER);
        mLayout = layout;

        loadConfiguration(a.getBaseContext());
    }

    @Override
    public View newView(Context context, Cursor c, ViewGroup parent) {
        final LayoutInflater inflater = LayoutInflater.from(context);
        View v = inflater.inflate(mLayout, parent, false);

        // Find all required views
        TextView FormattedTitle_text = (TextView) v.findViewById(R.id.movieTitle);
        TextView movieShortDescription_text = (TextView) v.findViewById(R.id.movieShortDescription);
        TextView movieShortDescription2_text = (TextView) v.findViewById(R.id.movieShortDescription2);
        TextView movieColorTag = (TextView) v.findViewById(R.id.movieColorTag);
        ImageView moviePictureView = (ImageView) v.findViewById(R.id.imageCover);
        if (!mShowThumbs) {
            moviePictureView.setVisibility(View.GONE);
        }

        // Assign views to holder
        MovieHolder holder = new MovieHolder();
        holder.FormattedTitle_text = FormattedTitle_text;
        holder.movieShortDescription_text = movieShortDescription_text;
        holder.movieShortDescription2_text = movieShortDescription2_text;
        holder.movieColorTag = movieColorTag;
        if (mShowThumbs) {
            holder.moviePictureView = new ImageViewAware(moviePictureView, false);
        }
        v.setTag(holder);

        return v;
    }

    @Override
    public void bindView(View v, Context context, Cursor c) {
        MovieHolder holder = (MovieHolder) v.getTag();

        if (S.VERBOSE)
            Log.v(S.TAG, "Using view: " + v.getTag().toString());

        String sortedField = Movies.SettingsSortFieldsMap.get(this.mSortOrder);
        Boolean sortedFieldDisplayed = false;

        ArrayList<String> line1 = new ArrayList<String>();
        ArrayList<String> line2 = new ArrayList<String>();
        ArrayList<String> line3 = new ArrayList<String>();

        if (!mListFieldsLine1[0].equals("")) {
            for (String field : mListFieldsLine1) {
                line1.add(getDBValue(field, context, c));
                if (field.equals(sortedField))
                    sortedFieldDisplayed = true;
            }
        }

        if (!mListFieldsLine2[0].equals("")) {
            for (String field : mListFieldsLine2) {
                line2.add(getDBValue(field, context, c));
                if (field.equals(sortedField))
                    sortedFieldDisplayed = true;
            }
        }

        if (!mListFieldsLine3[0].equals("")) {
            for (String field : mListFieldsLine3) {
                line3.add(getDBValue(field, context, c));
                if (field.equals(sortedField))
                    sortedFieldDisplayed = true;
            }
        }

        if (holder.FormattedTitle_text != null && line1.size() > 0) {
            holder.FormattedTitle_text.setText(Utils.arrayToString(line1.toArray(new String[line1.size()]),
                    mSettingMoviesListSeparator));
        }

        if (holder.movieShortDescription_text != null && line2.size() > 0) {
            holder.movieShortDescription_text.setText(Utils.arrayToString(line2.toArray(new String[line2.size()]),
                    mSettingMoviesListSeparator));
        }

        if (holder.movieShortDescription2_text != null) {
            if (this.mSettingListForceSortField && !sortedFieldDisplayed) {
                holder.movieShortDescription2_text.setText(getDBValue(sortedField, context, c));
            } else if (line3.size() > 0) {
                holder.movieShortDescription2_text.setText(Utils.arrayToString(line3.toArray(new String[line3.size()]),
                        mSettingMoviesListSeparator));
            }
        }

        // Display image with Image Loader
        if (mShowThumbs) {
            // Make full path
            String movieCatalogPicture = c.getString(c.getColumnIndex(Movies.PICTURE));
            if (movieCatalogPicture != null && !movieCatalogPicture.equals("")) {
                movieCatalogPicture = "file://" + mPicturesFolder + movieCatalogPicture;
            }

            if (S.VERBOSE)
                Log.v(S.TAG, "Requesting picture: " + movieCatalogPicture);

            mImageLoader.displayImage(movieCatalogPicture, holder.moviePictureView, mImageOptions);
        }

        //<editor-fold desc="Repaint color indicator to match Color tag">
        String currentColor = getDBValue(Movies.COLOR_TAG, context, c);
        if (currentColor == null || currentColor.equals("")) {
            currentColor = "0";
        }
        if (S.VERBOSE)
            Log.v(S.TAG, "Setting color to: " + currentColor);

        int background;
        switch (Integer.valueOf(currentColor)) {
            case 1:
                background = R.drawable.color_tag_1;
                break;
            case 2:
                background = R.drawable.color_tag_2;
                break;
            case 3:
                background = R.drawable.color_tag_3;
                break;
            case 4:
                background = R.drawable.color_tag_4;
                break;
            case 5:
                background = R.drawable.color_tag_5;
                break;
            case 6:
                background = R.drawable.color_tag_6;
                break;
            case 7:
                background = R.drawable.color_tag_7;
                break;
            case 8:
                background = R.drawable.color_tag_8;
                break;
            case 9:
                background = R.drawable.color_tag_9;
                break;
            case 10:
                background = R.drawable.color_tag_10;
                break;
            case 11:
                background = R.drawable.color_tag_11;
                break;
            case 12:
                background = R.drawable.color_tag_12;
                break;
            default:
                background = R.drawable.color_tag_0;
        }

        holder.movieColorTag.setBackgroundResource(background);
        //</editor-fold>
    }

    public void loadConfiguration(Context c) {
        // Get preferences
        SharedPreferences preferences = SharedObjects.getInstance().preferences;
        mShowThumbs = preferences.getBoolean("settingShowThumbs", true);
        mPicturesFolder = preferences.getString("settingPicturesFolder", "/");
        mSortOrder = preferences.getString("settingMovieListOrder", Movies.DEFAULT_SORT_ORDER);
        mListFieldsLine1 = preferences.getString("settingMoviesListLine1", Movies.defaultListFieldsLine1).split(",");
        mListFieldsLine2 = preferences.getString("settingMoviesListLine2", Movies.defaultListFieldsLine2).split(",");
        mListFieldsLine3 = preferences.getString("settingMoviesListLine3", Movies.defaultListFieldsLine3).split(",");
        mSettingListForceSortField = preferences.getBoolean("settingListForceSortField", true);
        mSettingMoviesListSeparator = preferences.getString("settingMoviesListSeparator", c.getString(R.string.items_separator_default));

        if (mShowThumbs) {
            // Prepare Image Loader
            mImageLoader = ImageLoader.getInstance();
            if (mImageOptions == null) {
                mImageOptions = new DisplayImageOptions.Builder()
                        .resetViewBeforeLoading(true)
                        .showImageOnLoading(R.drawable.movie_thumb_stub)
                        .showImageForEmptyUri(R.drawable.movie_thumb_stub)
                        .cacheInMemory(true)
                        .cacheOnDisk(true)
                        .build();
            }
        }
    }

    private String getDBValue(String fld, Context context, Cursor c) {
        String value = c.getString(c.getColumnIndex(fld));

        if (fld.equals(Movies.NUMBER)) {
            if (value != null)
                value = context.getString(R.string.number_prefix) + value;
        } else if (fld.equals(Movies.LENGTH)) {
            if (value != null && value.length() > 0)
                value += " " + context.getString(R.string.display_minutes_suffix);
        } else if (fld.equals(Movies.RATING) || fld.equals(Movies.USER_RATING)) {
            if (value != null && value.length() > 0) {
                value += context.getString(R.string.rating_suffix_list);
            }
        } else if (fld.equals(Movies.CHECKED)) {
            if (value != null && value.equals("True"))
                value = context.getString(R.string.list_seen_true);
            else
                value = context.getString(R.string.list_seen_false);
        } else if (fld.equals(Movies.DATE) || fld.equals(Movies.DATE_WATCHED)) {
            if (value != null) {
                try {
                    Date date = SharedObjects.getInstance().dateAddedFormat.parse(value);
                    value = SharedObjects.getInstance().dateFormat.format(date);
                } catch (Exception e) {
                    // don't do anything, keep date as is
                }
            }
        }

        return value;
    }

    public void stopImageLoader() {
        if (mImageLoader != null)
            mImageLoader.stop();
    }
}
