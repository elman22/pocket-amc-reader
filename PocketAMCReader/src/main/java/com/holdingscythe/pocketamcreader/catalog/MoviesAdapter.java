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

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.holdingscythe.pocketamcreader.R;
import com.holdingscythe.pocketamcreader.S;
import com.holdingscythe.pocketamcreader.utils.SharedObjects;
import com.holdingscythe.pocketamcreader.utils.Utils;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;

/**
 * Adapter for movies stored in database
 */
public class MoviesAdapter extends CursorRecyclerViewAdapter<MoviesAdapter.MovieHolder> {
    private Context mContext;
    private Boolean mShowThumbs;
    private String mPicturesFolder;
    private String mSortedField;
    private String[] mListFieldsLine1;
    private String[] mListFieldsLine2;
    private String[] mListFieldsLine3;
    private Boolean mSettingListForceSortField;
    private String mSettingMoviesListSeparator;
    private ImageLoader mImageLoader;
    private DisplayImageOptions mImageOptions;

    // Recycler views
    private final int GRID = 0;
    private final int LIST = 1;
    private int mViewType;
    private MoviesAdapterClickListener mListener;

    /**
     * Recycler view holder
     */
    class MovieHolder extends RecyclerView.ViewHolder {
        TextView FormattedTitle_text;
        TextView movieShortDescription_text;
        TextView movieShortDescription2_text;
        TextView movieColorTag;
        ImageView moviePictureView;

        /**
         * @param itemView itemView
         * @param viewType viewType
         */
        MovieHolder(View itemView, int viewType) {
            super(itemView);

            if (viewType == GRID) {
                FormattedTitle_text = (TextView) itemView.findViewById(R.id.movieTitleGrid);
                movieShortDescription_text = null;
                movieShortDescription2_text = null;
                movieColorTag = (TextView) itemView.findViewById(R.id.ListColorTagGrid);
                moviePictureView = (ImageView) itemView.findViewById(R.id.imageCoverGrid);
            } else {
                FormattedTitle_text = (TextView) itemView.findViewById(R.id.movieTitle);
                movieShortDescription_text = (TextView) itemView.findViewById(R.id.movieShortDescription);
                movieShortDescription2_text = (TextView) itemView.findViewById(R.id.movieShortDescription2);
                movieColorTag = (TextView) itemView.findViewById(R.id.ListColorTag);
                moviePictureView = (ImageView) itemView.findViewById(R.id.imageCover);
                if (!mShowThumbs) {
                    moviePictureView.setVisibility(View.GONE);
                }
            }
        }
    }

    /**
     * @param context context
     * @param cursor cursor
     * @param viewType viewType
     */
    public MoviesAdapter(Context context, Cursor cursor, int viewType, MoviesAdapterClickListener listener) {
        super(cursor);
        mContext = context;
        mViewType = viewType;
        mListener = listener;
        loadConfiguration(context);
    }

    // Inflating views if the existing layout items are not being recycled
    @NonNull
    @Override
    public MovieHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        final LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View itemView;
        if (viewType == GRID) {
            // Inflate the grid cell as a view item
            itemView = inflater.inflate(R.layout.grid_movie_item, parent, false);
        } else {
            // Inflate the list row as a view item
            itemView = inflater.inflate(R.layout.list_movie_item, parent, false);
        }

        final MovieHolder mMovieHolder = new MovieHolder(itemView, viewType);

        itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.onItemClick(v, mMovieHolder.getAdapterPosition());
            }
        });
        return mMovieHolder;
    }

    // Using the variable "mViewType" to check which layout is to be displayed
    @Override
    public int getItemViewType(int position) {
        if (mViewType == GRID) {
            return GRID;
        } else {
            return LIST;
        }
    }

    @Override
    public void onBindViewHolder(@NonNull MovieHolder holder, Cursor cursor) {
        Boolean sortedFieldDisplayed = false;

        ArrayList<String> line1 = new ArrayList<>();
        ArrayList<String> line2 = new ArrayList<>();
        ArrayList<String> line3 = new ArrayList<>();

        if (!mListFieldsLine1[0].isEmpty()) {
            for (String field : mListFieldsLine1) {
                line1.add(getDBValue(field, cursor));
                if (field.equals(mSortedField))
                    sortedFieldDisplayed = true;
            }
        }

        if (holder.movieShortDescription_text != null) {
            if (!mListFieldsLine2[0].isEmpty()) {
                for (String field : mListFieldsLine2) {
                    line2.add(getDBValue(field, cursor));
                    if (field.equals(mSortedField))
                        sortedFieldDisplayed = true;
                }
            }
        }

        if (holder.movieShortDescription2_text != null) {
            if (!mListFieldsLine3[0].isEmpty()) {
                for (String field : mListFieldsLine3) {
                    line3.add(getDBValue(field, cursor));
                    if (field.equals(mSortedField))
                        sortedFieldDisplayed = true;
                }
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
            if (mSettingListForceSortField && !sortedFieldDisplayed) {
                holder.movieShortDescription2_text.setText(getDBValue(mSortedField, cursor));
            } else if (line3.size() > 0) {
                holder.movieShortDescription2_text.setText(Utils.arrayToString(line3.toArray(new String[line3.size()]),
                        mSettingMoviesListSeparator));
            }
        }

        // Display image with Image Loader
        if (mShowThumbs) {
            // Make full path or keep null if picture is not present
            String movieCatalogPicture = cursor.getString(cursor.getColumnIndex(Movies.PICTURE));

            if (movieCatalogPicture != null && !movieCatalogPicture.equals("")) {
                if (new File(mPicturesFolder + movieCatalogPicture).isFile()) {
                    movieCatalogPicture = "file://" + mPicturesFolder + movieCatalogPicture;

                    if (S.VERBOSE)
                        Log.v(S.TAG, "Requesting picture: " + movieCatalogPicture);
                } else {
                    if (S.VERBOSE)
                        Log.v(S.TAG, "Missing picture: " + mPicturesFolder + movieCatalogPicture);

                    movieCatalogPicture = null;
                }
            }

            mImageLoader.displayImage(movieCatalogPicture, holder.moviePictureView, mImageOptions);
        }

        //<editor-fold desc="Repaint color indicator to match Color tag">
        String currentColor = getDBValue(Movies.COLOR_TAG, cursor);
        if (currentColor == null || currentColor.equals("")) {
            currentColor = "0";
        }
        if (S.VERBOSE)
            Log.v(S.TAG, "Setting color to: " + currentColor);

        int background;
        switch (Integer.valueOf(currentColor)) {
            case 1:
                background = R.color.color_tag_1;
                break;
            case 2:
                background = R.color.color_tag_2;
                break;
            case 3:
                background = R.color.color_tag_3;
                break;
            case 4:
                background = R.color.color_tag_4;
                break;
            case 5:
                background = R.color.color_tag_5;
                break;
            case 6:
                background = R.color.color_tag_6;
                break;
            case 7:
                background = R.color.color_tag_7;
                break;
            case 8:
                background = R.color.color_tag_8;
                break;
            case 9:
                background = R.color.color_tag_9;
                break;
            case 10:
                background = R.color.color_tag_10;
                break;
            case 11:
                background = R.color.color_tag_11;
                break;
            case 12:
                background = R.color.color_tag_12;
                break;
            default:
                background = R.color.color_tag_0;
        }

        // If no color is assigned, hide view
        if (Integer.valueOf(currentColor) == 0) {
            holder.movieColorTag.setVisibility(View.GONE);
        } else {
            holder.movieColorTag.setVisibility(View.VISIBLE);
            holder.movieColorTag.setBackgroundResource(background);
        }
        //</editor-fold>
    }

    /**
     * Load configuration for current instance
     *
     * @param c context
     */
    private void loadConfiguration(Context c) {
        // Get preferences
        SharedPreferences preferences = SharedObjects.getInstance().preferences;

        // Always set to true if grid view is used
        mShowThumbs = preferences.getBoolean("settingShowThumbs", true);
        if (mViewType == GRID) mShowThumbs = true;

        mPicturesFolder = preferences.getString("settingPicturesFolder", "/");
        String sortOrder = preferences.getString("settingMovieListOrder", Movies.DEFAULT_SORT_ORDER);
        mSortedField = Movies.SettingsSortFieldsMap.get(sortOrder);
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
                        .cacheOnDisk(false)
                        .build();
            }
        }
    }

    /**
     * Get field value from cursor
     * @param fld field
     * @param c cursor
     * @return value
     */
    private String getDBValue(String fld, Cursor c) {
        String value = c.getString(c.getColumnIndex(fld));

        switch (fld) {
            case Movies.NUMBER:
                if (value != null)
                    value = mContext.getString(R.string.number_prefix) + value;
                break;
            case Movies.LENGTH:
                if (value != null && value.length() > 0)
                    value += " " + mContext.getString(R.string.display_minutes_suffix);
                break;
            case Movies.RATING:
            case Movies.USER_RATING:
                if (value != null && value.length() > 0)
                    value += mContext.getString(R.string.rating_suffix_list);
                break;
            case Movies.CHECKED:
                if (value != null && value.equals("True"))
                    value = mContext.getString(R.string.list_seen_true);
                else
                    value = mContext.getString(R.string.list_seen_false);
                break;
            case Movies.DATE:
            case Movies.DATE_WATCHED:
                if (value != null) {
                    try {
                        Date date = SharedObjects.getInstance().dateAddedFormat.parse(value);
                        value = SharedObjects.getInstance().dateFormat.format(date);
                    } catch (Exception e) {
                        // don't do anything, keep date as is
                    }
                }
                break;
        }

        return value;
    }

    /**
     * Stop loading of images in background thread
     */
    public void stopImageLoader() {
        if (mImageLoader != null)
            mImageLoader.stop();
    }
}
