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

package com.holdingscythe.pocketamcreader;

import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.database.Cursor;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.holdingscythe.pocketamcreader.catalog.CustomFields;
import com.holdingscythe.pocketamcreader.catalog.Extras;
import com.holdingscythe.pocketamcreader.catalog.Movie;
import com.holdingscythe.pocketamcreader.catalog.Movies;
import com.holdingscythe.pocketamcreader.catalog.MoviesDataProvider;
import com.holdingscythe.pocketamcreader.filters.Filter;
import com.holdingscythe.pocketamcreader.filters.FilterField;
import com.holdingscythe.pocketamcreader.filters.FilterOperator;
import com.holdingscythe.pocketamcreader.filters.Filters;
import com.holdingscythe.pocketamcreader.settings.SettingsConstants;
import com.holdingscythe.pocketamcreader.utils.SharedObjects;
import com.holdingscythe.pocketamcreader.utils.Utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import androidx.annotation.NonNull;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.fragment.app.Fragment;

/**
 * A fragment representing a single Movie detail screen.
 * This fragment is either contained in a {@link MovieListActivity}
 * in two-pane mode (on tablets) or a {@link MovieDetailActivity}
 * on handsets.
 */
public class MovieDetailFragment extends Fragment implements OnClickListener {
    private Movie mMovie;
    private Extras mExtras;
    private Filters mFilters;
    private String mPicturesFolder;
    private int mDeviceWidthPixels;
    private int mActionBarHeight;
    private int mHeroImageHeight;

    private Pattern regExpMultivaluedCleaner;
    private String regExpMultivaluedSeparator;

    /**
     * The fragment argument representing the movie ID that this fragment represents.
     */
    public static final String ARG_MOVIE_ID = "movie_id";
    public static final String ARG_MOVIE_PICTURES_LIST = "pictures_list";
    public static final String ARG_MOVIE_TITLE = "movie_title";

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public MovieDetailFragment() {
    }

    public static MovieDetailFragment newInstance(Bundle args) {
        MovieDetailFragment fragment = new MovieDetailFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // Prepare custom font for the toolbar
        final Typeface tf = Typeface.createFromAsset(getContext().getAssets(), S.DEFAULT_FONT);

        // Setup filters
        mFilters = new Filters();
        mFilters.setContext(getContext());

        // Read preferences
        if (SharedObjects.getInstance().preferences == null) {
            // Prepare Shared Objects
            SharedObjects.getInstance().preferences = PreferenceManager.getDefaultSharedPreferences
                    (getActivity().getApplicationContext());
        }

        SharedPreferences preferences = SharedObjects.getInstance().preferences;
        String settingMultivaluedSeparator = preferences.getString(SettingsConstants.KEY_PREF_DETAIL_SEPARATOR, ",/");
        mPicturesFolder = preferences.getString(SettingsConstants.KEY_PREF_PICTURES_FOLDER, "/");

        if (getArguments() != null && getArguments().containsKey(ARG_MOVIE_ID)) {
            // Prepare Data Provider
            MoviesDataProvider moviesDataProvider = new MoviesDataProvider(getActivity());

            // Set (and compile) regular expressions
            regExpMultivaluedCleaner = Pattern.compile("\\s*\\([^)]*\\)+");
            regExpMultivaluedSeparator = "\\s*[" + Pattern.quote(settingMultivaluedSeparator) + "]\\s*";

            // Benchmark start
            long startTime;
            long endTime;

            if (S.INFO) {
                startTime = System.currentTimeMillis();
            }

            // Fetch movie
            Uri uri = Uri.withAppendedPath(S.CONTENT_URI, "_id/" + getArguments().getString(ARG_MOVIE_ID, "0"));
            Cursor cursor = moviesDataProvider.fetchMovie(uri);
            cursor.moveToFirst();
            mMovie = new Movie(cursor, getView(), this, getActivity());
            cursor.close();

            // Fetch custom fields
            Cursor cursorCustomFields = moviesDataProvider.fetchMovieCustomFields(Uri.withAppendedPath(uri, "custom"));
            cursorCustomFields.moveToFirst();
            new CustomFields(cursorCustomFields, getView(), getActivity());
            cursorCustomFields.close();

            // Fetch extras
            Cursor cursorExtras = moviesDataProvider.fetchMovieExtras(Uri.withAppendedPath(uri, "extras"));
            cursorExtras.moveToFirst();
            mExtras = new Extras(cursorExtras);
            cursorExtras.close();

            // Close database
            moviesDataProvider.closeDatabase();

            // Set collapsing toolbar title
            CollapsingToolbarLayout collapsingToolbar = getView().findViewById(R.id.collapsingToolbar);
            AppBarLayout appBarLayout = getView().findViewById(R.id.appBarLayout);

            if (collapsingToolbar != null && appBarLayout != null) {

                // Set default variables from runtime
                if (SharedObjects.getInstance().deviceWidthPixels == 0 ||
                        SharedObjects.getInstance().actionBarHeight == 0 ||
                        SharedObjects.getInstance().heroImageHeight == 0) {

                    // Set device width pixels
                    SharedObjects.getInstance().deviceWidthPixels =
                            getResources().getDisplayMetrics().widthPixels;

                    // Set action bar height
                    final TypedArray styledAttributes =
                            getContext().getTheme().obtainStyledAttributes(new int[]{android.R.attr.actionBarSize});
                    SharedObjects.getInstance().actionBarHeight =
                            (int) styledAttributes.getDimension(0, 0);
                    styledAttributes.recycle();

                    // Set hero image height
                    SharedObjects.getInstance().heroImageHeight =
                            (int) getResources().getDimension(R.dimen.detail_hero_img_height);
                }

                mDeviceWidthPixels = SharedObjects.getInstance().deviceWidthPixels;
                mActionBarHeight = SharedObjects.getInstance().actionBarHeight;
                mHeroImageHeight = SharedObjects.getInstance().heroImageHeight;

                // Set custom font to the toolbar
                collapsingToolbar.setCollapsedTitleTypeface(tf);
                collapsingToolbar.setExpandedTitleTypeface(tf);

                // Title must be space or else application name will be used
                collapsingToolbar.setTitle(" ");

                // Get adjusted height for AppBarLayout
                int adjustedHeight;

                if (mMovie.getPictureAspectRatio() > 0) {
                    adjustedHeight =
                            (int) Math.floor((long) mDeviceWidthPixels / mMovie.getPictureAspectRatio());
                } else {
                    adjustedHeight = mHeroImageHeight + mActionBarHeight;
                }

                // Set adjusted height to AppBarLayout
                CoordinatorLayout.LayoutParams params =
                        (CoordinatorLayout.LayoutParams) appBarLayout.getLayoutParams();
                params.height = adjustedHeight;
                appBarLayout.setLayoutParams(params);

                // Add listener to dynamically change toolbar title
                appBarLayout.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
                    boolean isShow = false;
                    int scrollRange = -1;

                    @Override
                    public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
                        if (scrollRange == -1) {
                            scrollRange = appBarLayout.getTotalScrollRange();

                            // Scroll collapsing toolbar to default position
                            CoordinatorLayout.LayoutParams params =
                                    (CoordinatorLayout.LayoutParams) appBarLayout.getLayoutParams();
                            AppBarLayout.Behavior behavior =
                                    (AppBarLayout.Behavior) params.getBehavior();
                            if (behavior != null) {
                                behavior.setTopAndBottomOffset(mHeroImageHeight - scrollRange);
                            }
                            appBarLayout.setLayoutParams(params);
                        }
                        if (scrollRange + verticalOffset == 0) {
                            // when collapsingToolbar is collapsed, display actionbar title
                            collapsingToolbar.setTitle(mMovie.getTitle());
                            isShow = true;
                        } else if (isShow) {
                            // there must a space between double quote otherwise it won't work
                            collapsingToolbar.setTitle(" ");
                            isShow = false;
                        }
                    }
                });
            }

            // Benchmark end
            if (S.INFO) {
                endTime = System.currentTimeMillis();
                Log.i(S.TAG, "Details display elapsed time: " + (endTime - startTime) + " ms.");
            }

        } else {
            Log.e(S.TAG, "Movie details called without ARG_MOVIE_ID");
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_movie_detail, container, false);
    }

    @Override
    public void onDestroyView() {
        mMovie.unbindData();

        super.onDestroyView();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.Picture:
                try {
                    // prepare pictures array
                    Intent i = new Intent(getActivity(), PictureViewActivity.class);
                    ArrayList<String> al = new ArrayList<>();
                    al.addAll(mMovie.getPicturesList());
                    al.addAll(mExtras.getPicturesList());
                    i.putExtra(MovieDetailFragment.ARG_MOVIE_PICTURES_LIST, al);
                    i.putExtra(MovieDetailFragment.ARG_MOVIE_TITLE, mMovie.getTitle());
                    startActivity(i);
                } catch (Exception e) {
                    // TODO: fix exception
                    e.printStackTrace();
                }
                break;

            case R.id.Certification:
                filterClick(new Filter(Movies.FILTER_CERTIFICATION, Movies.FILTER_OPERATOR_EQUALS,
                        ((TextView) view).getText().toString()));
                break;
            case R.id.Checked:
                filterClick(new Filter(Movies.FILTER_CHECKED, Movies.FILTER_OPERATOR_EQUALS,
                        ((TextView) view).getText().toString(), getString(R.string.details_boolean_true)));
                break;
            case R.id.UserRating:
                filterClick(new Filter(Movies.FILTER_USER_RATING, Movies.FILTER_OPERATOR_EQUALS,
                        ((TextView) view).getText().toString()));
                break;

            case R.id.Year:
                filterClick(new Filter(Movies.FILTER_YEAR, Movies.FILTER_OPERATOR_EQUALS,
                        ((TextView) view).getText().toString()));
                break;
            case R.id.Length:
                filterClick(new Filter(Movies.FILTER_LENGTH, Movies.FILTER_OPERATOR_EQUALS,
                        ((TextView) view).getText().toString()));
                break;
            case R.id.Rating:
                filterClick(new Filter(Movies.FILTER_RATING, Movies.FILTER_OPERATOR_EQUALS,
                        ((TextView) view).getText().toString()));
                break;

            case R.id.Category:
                chooseMultivaluedFieldValue(Movies.FILTER_CATEGORY, Movies.FILTER_OPERATOR_CONTAINS, view);
                break;
            case R.id.Director:
                chooseMultivaluedFieldValue(Movies.FILTER_DIRECTOR, Movies.FILTER_OPERATOR_CONTAINS, view);
                break;
            case R.id.Actors:
                chooseMultivaluedFieldValue(Movies.FILTER_ACTORS, Movies.FILTER_OPERATOR_CONTAINS, view);
                break;
            case R.id.Producer:
                chooseMultivaluedFieldValue(Movies.FILTER_PRODUCER, Movies.FILTER_OPERATOR_CONTAINS, view);
                break;
            case R.id.Writer:
                chooseMultivaluedFieldValue(Movies.FILTER_WRITER, Movies.FILTER_OPERATOR_CONTAINS, view);
                break;
            case R.id.Composer:
                chooseMultivaluedFieldValue(Movies.FILTER_COMPOSER, Movies.FILTER_OPERATOR_CONTAINS, view);
                break;
            case R.id.Country:
                chooseMultivaluedFieldValue(Movies.FILTER_COUNTRY, Movies.FILTER_OPERATOR_CONTAINS, view);
                break;

            case R.id.MediaLabel:
                filterClick(new Filter(Movies.FILTER_MEDIA_LABEL, Movies.FILTER_OPERATOR_EQUALS,
                        ((TextView) view).getText().toString()));
                break;
            case R.id.MediaType:
                filterClick(new Filter(Movies.FILTER_MEDIA_TYPE, Movies.FILTER_OPERATOR_EQUALS,
                        ((TextView) view).getText().toString()));
                break;
            case R.id.Source:
                filterClick(new Filter(Movies.FILTER_SOURCE, Movies.FILTER_OPERATOR_EQUALS,
                        ((TextView) view).getText().toString()));
                break;
            case R.id.Borrower:
                filterClick(new Filter(Movies.FILTER_BORROWER, Movies.FILTER_OPERATOR_EQUALS,
                        ((TextView) view).getText().toString()));
                break;

            case R.id.Languages:
                chooseMultivaluedFieldValue(Movies.FILTER_LANGUAGES, Movies.FILTER_OPERATOR_CONTAINS, view);
                break;
            case R.id.Subtitles:
                chooseMultivaluedFieldValue(Movies.FILTER_SUBTITLES, Movies.FILTER_OPERATOR_CONTAINS, view);
                break;
            case R.id.VideoFormat:
                filterClick(new Filter(Movies.FILTER_VIDEO_FORMAT, Movies.FILTER_OPERATOR_EQUALS,
                        ((TextView) view).getText().toString()));
                break;
            case R.id.AudioFormat:
                filterClick(new Filter(Movies.FILTER_AUDIO_FORMAT, Movies.FILTER_OPERATOR_EQUALS,
                        ((TextView) view).getText().toString()));
                break;
            case R.id.Resolution:
                filterClick(new Filter(Movies.FILTER_RESOLUTION, Movies.FILTER_OPERATOR_EQUALS,
                        ((TextView) view).getText().toString()));
                break;
            case R.id.Framerate:
                filterClick(new Filter(Movies.FILTER_FRAMERATE, Movies.FILTER_OPERATOR_EQUALS,
                        ((TextView) view).getText().toString()));
                break;

            case R.id.URL:
            case R.id.FilePath:
                // If link points to IMDb, open IMDb app if available
                String url = ((TextView) view).getText().toString();
                Pattern regExpImdb = Pattern.compile("imdb\\.[\\w]+/(Title\\?|title/tt)(\\d{6,7})/?");
                Matcher m = regExpImdb.matcher(url);

                if (m.find() && m.group(2) != null) {
                    try {
                        // prepare uri
                        Uri uri = Uri.parse("imdb:///title/tt" + String.format(Locale.US, "%07d",
                                Integer.parseInt(Objects.requireNonNull(m.group(2)))) + "/");

                        // open intent
                        Intent i = new Intent(Intent.ACTION_VIEW);
                        i.setData(uri);
                        startActivity(i);
                    } catch (ActivityNotFoundException e) {
                        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                        startActivity(browserIntent);
                    }
                } else {
                    boolean isIntentValid = false;
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    int errorResource = R.string.link_not_opened;

                    if (url.startsWith(S.INTENT_SCHEME_HTTPS) || url.startsWith(S.INTENT_SCHEME_HTTP)) {
                        // If link starts with http(s), handle it as web link
                        intent.setData(Uri.parse(url));
                        isIntentValid = true;
                    } else if (url.startsWith(S.INTENT_SCHEME_FILE) || !url.contains(S.INTENT_SCHEME_PORTION)) {
                        if (url.startsWith(S.INTENT_SCHEME_FILE)) {
                            // If link starts with file scheme, remove it and leave absolute path
                            url = url.replace(S.INTENT_SCHEME_FILE, "");
                        } else {
                            // If link does not contain any scheme, make absolute path
                            if (!url.startsWith("/")) {
                                // Fix for Windows backslashes
                                url = url.replace("\\", "/");
                                url = mPicturesFolder + url;
                            }
                        }

                        try {
                            File file = new File(url);

                            if (!file.exists()) {
                                errorResource = R.string.file_not_found;
                                throw new FileNotFoundException();
                            } else {
                                intent.setDataAndType(Uri.parse(file.getPath()),
                                        Utils.getMimeFromUrl(url, S.MIME_VIDEO));
                                isIntentValid = true;
                            }
                        } catch (FileNotFoundException e) {
                            if (S.WARN)
                                Log.w(S.TAG, "Couldn't open file \"" + url + "\" for size check.");
                        } catch (Exception e) {
                            if (S.WARN)
                                Log.w(S.TAG, "Error opening file.");
                        }
                    }

                    if (S.DEBUG)
                        Log.d(S.TAG,
                                "Opening intent: (" + isIntentValid + ") " + intent.toString());

                    if (isIntentValid) {
                        try {
                            startActivity(intent);
                        } catch (ActivityNotFoundException e) {
                            Toast.makeText(view.getContext(),
                                    view.getContext().getText(R.string.link_not_opened_hint),
                                    Toast.LENGTH_LONG).show();
                        } catch (Exception e) {
                            Log.e(S.TAG, e.toString());
                        }
                    } else {
                        Toast.makeText(view.getContext(), view.getContext().getText(errorResource),
                                Toast.LENGTH_SHORT).show();
                    }
                }

                break;
        }
    }

    /**
     * Prepare select to pick from available values in multivalued field
     */
    private void chooseMultivaluedFieldValue(final FilterField field, final FilterOperator operator, View v) {
        final String[] availableValues = separateMultivaluedField(v);

        // If only one field is available, make direct filter
        if (availableValues.length == 1) {
            filterClick(new Filter(field, operator, availableValues[0]));
        } else if (availableValues.length > 1) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle(mFilters.getFilterFieldHumanName(field.resId) + " " + getString(operator.resId));
            builder.setItems(availableValues, (dialog, item) -> filterClick(new Filter(field,
                    operator, availableValues[item])));
            builder.setNegativeButton(getString(R.string.dialog_negative),
                    (dialog, whichButton) -> {});
            AlertDialog alertDialog = builder.create();
            alertDialog.show();
        }
    }

    /**
     * Split multivalued field into respective values
     */
    private String[] separateMultivaluedField(View v) {
        ArrayList<String> availableValues = new ArrayList<>();
        TextView multiField = (TextView) v;
        String multiValue = multiField.getText().toString();

        Matcher m = regExpMultivaluedCleaner.matcher(multiValue);
        multiValue = m.replaceAll("");

        for (String value : multiValue.split(regExpMultivaluedSeparator)) {
            if (value.length() > 0)
                availableValues.add(value);
        }

        return availableValues.toArray(new String[0]);
    }

    /**
     * Add filter and return to list activity
     */
    private void filterClick(Filter filter) {
        String val = filter.getHumanValue();
        if (SharedObjects.getInstance().movieListFragment != null && val != null && !val.equals("")) {
            SharedObjects.getInstance().movieListFragment.addExternalFilter(filter);
            if (getActivity() instanceof MovieDetailActivity) {
                getActivity().finish();
            }
        }
    }

}
