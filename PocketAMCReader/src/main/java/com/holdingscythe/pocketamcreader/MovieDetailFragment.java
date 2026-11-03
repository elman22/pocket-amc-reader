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
import com.holdingscythe.pocketamcreader.filters.Filters;
import com.holdingscythe.pocketamcreader.settings.SettingsConstants;
import com.holdingscythe.pocketamcreader.utils.SafHelper;
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
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceManager;

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
        final Typeface tf = ResourcesCompat.getFont(requireContext(), R.font.roboto_condensed);

        // Setup filters
        mFilters = new Filters();
        mFilters.setContext(getContext());

        // Read preferences
        if (SharedObjects.getInstance().preferences == null) {
            // Prepare Shared Objects
            SharedObjects.getInstance().preferences = PreferenceManager.getDefaultSharedPreferences
                    (Objects.requireNonNull(getActivity()).getApplicationContext());
        }

        SharedPreferences preferences = SharedObjects.getInstance().preferences;
        String settingMultivaluedSeparator =
                Utils.coalesce(preferences.getString(SettingsConstants.KEY_PREF_DETAIL_SEPARATOR,
                        ",/"), ",/");

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
            mMovie = new Movie(cursor, getView(), this, Objects.requireNonNull(getActivity()));
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
            CollapsingToolbarLayout collapsingToolbar = Objects.requireNonNull(getView()).findViewById(R.id.collapsingToolbar);
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

                int mDeviceWidthPixels = SharedObjects.getInstance().deviceWidthPixels;
                int mActionBarHeight = SharedObjects.getInstance().actionBarHeight;
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
                            int color = Utils.getColorFromColorTag(mMovie.getColorTag());
                            collapsingToolbar.setCollapsedTitleTextColor(ContextCompat.getColor(Objects.requireNonNull(getContext()), color));
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
            case R.id.Picture -> {
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
            }
            case R.id.Certification -> {
                filterClick(new Filter(Movies.FILTER_CERTIFICATION, Movies.FILTER_OPERATOR_EQUALS,
                        ((TextView) view).getText().toString()));
            }
            case R.id.Checked -> {
                filterClick(new Filter(Movies.FILTER_CHECKED, Movies.FILTER_OPERATOR_EQUALS,
                        ((TextView) view).getText().toString(), getString(R.string.details_boolean_true)));
            }
            case R.id.UserRating -> {
                filterClick(new Filter(Movies.FILTER_USER_RATING, Movies.FILTER_OPERATOR_EQUALS,
                        ((TextView) view).getText().toString()));
            }
            case R.id.Year -> {
                filterClick(new Filter(Movies.FILTER_YEAR, Movies.FILTER_OPERATOR_EQUALS,
                        ((TextView) view).getText().toString()));
            }
            case R.id.Length -> {
                filterClick(new Filter(Movies.FILTER_LENGTH, Movies.FILTER_OPERATOR_EQUALS,
                        ((TextView) view).getText().toString()));
            }
            case R.id.Rating -> {
                filterClick(new Filter(Movies.FILTER_RATING, Movies.FILTER_OPERATOR_EQUALS,
                        ((TextView) view).getText().toString()));
            }
            case R.id.Category -> chooseMultivaluedFieldValue(Movies.FILTER_CATEGORY, view);
            case R.id.Director -> chooseMultivaluedFieldValue(Movies.FILTER_DIRECTOR, view);
            case R.id.Actors -> chooseMultivaluedFieldValue(Movies.FILTER_ACTORS, view);
            case R.id.Producer -> chooseMultivaluedFieldValue(Movies.FILTER_PRODUCER, view);
            case R.id.Writer -> chooseMultivaluedFieldValue(Movies.FILTER_WRITER, view);
            case R.id.Composer -> chooseMultivaluedFieldValue(Movies.FILTER_COMPOSER, view);
            case R.id.Country -> chooseMultivaluedFieldValue(Movies.FILTER_COUNTRY, view);
            case R.id.MediaLabel -> {
                filterClick(new Filter(Movies.FILTER_MEDIA_LABEL, Movies.FILTER_OPERATOR_EQUALS,
                        ((TextView) view).getText().toString()));
            }
            case R.id.MediaType -> {
                filterClick(new Filter(Movies.FILTER_MEDIA_TYPE, Movies.FILTER_OPERATOR_EQUALS,
                        ((TextView) view).getText().toString()));
            }
            case R.id.Source -> {
                filterClick(new Filter(Movies.FILTER_SOURCE, Movies.FILTER_OPERATOR_EQUALS,
                        ((TextView) view).getText().toString()));
            }
            case R.id.Borrower -> {
                filterClick(new Filter(Movies.FILTER_BORROWER, Movies.FILTER_OPERATOR_EQUALS,
                        ((TextView) view).getText().toString()));
            }
            case R.id.Languages -> chooseMultivaluedFieldValue(Movies.FILTER_LANGUAGES, view);
            case R.id.Subtitles -> chooseMultivaluedFieldValue(Movies.FILTER_SUBTITLES, view);
            case R.id.VideoFormat -> {
                filterClick(new Filter(Movies.FILTER_VIDEO_FORMAT, Movies.FILTER_OPERATOR_EQUALS,
                        ((TextView) view).getText().toString()));
            }
            case R.id.AudioFormat -> {
                filterClick(new Filter(Movies.FILTER_AUDIO_FORMAT, Movies.FILTER_OPERATOR_EQUALS,
                        ((TextView) view).getText().toString()));
            }
            case R.id.Resolution -> {
                filterClick(new Filter(Movies.FILTER_RESOLUTION, Movies.FILTER_OPERATOR_EQUALS,
                        ((TextView) view).getText().toString()));
            }
            case R.id.Framerate -> {
                filterClick(new Filter(Movies.FILTER_FRAMERATE, Movies.FILTER_OPERATOR_EQUALS,
                        ((TextView) view).getText().toString()));
            }
            case R.id.URL, R.id.FilePath -> {
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
                        // IMDb app not installed, fall back to browser
                        try {
                            String webUrl = url.contains(S.INTENT_SCHEME_PORTION) ? url : S.INTENT_SCHEME_HTTPS + url;
                            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(webUrl)));
                        } catch (ActivityNotFoundException e2) {
                            Toast.makeText(view.getContext(),
                                    view.getContext().getText(R.string.link_not_opened_hint),
                                    Toast.LENGTH_LONG).show();
                        }
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
                        // Local file: relative to the catalog folder (granted by the user), or absolute path
                        if (url.startsWith(S.INTENT_SCHEME_FILE))
                            url = url.replace(S.INTENT_SCHEME_FILE, "");

                        try {
                            String treeString = SharedObjects.getInstance().preferences
                                    .getString(SettingsConstants.KEY_PREF_CATALOG_TREE, "");
                            Uri treeUri = SafHelper.isContentUri(treeString) ? Uri.parse(treeString) : null;
                            Uri fileUri = SafHelper.resolveFileLink(view.getContext(), url, treeUri);

                            if (fileUri == null) {
                                errorResource = R.string.file_not_found;
                            } else {
                                intent.setDataAndType(fileUri, Utils.getMimeFromUrl(url, S.MIME_VIDEO));
                                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                                isIntentValid = true;
                            }
                        } catch (Exception e) {
                            if (S.WARN)
                                Log.w(S.TAG, "Error opening file.", e);
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
            }
        }
    }

    /**
     * Prepare select to pick from available values in multivalued field
     */
    private void chooseMultivaluedFieldValue(final FilterField field, View v) {
        final String[] availableValues = separateMultivaluedField(v);

        // If only one field is available, make direct filter
        if (availableValues.length == 1) {
            filterClick(new Filter(field, Movies.FILTER_OPERATOR_CONTAINS, availableValues[0]));
        } else if (availableValues.length > 1) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle(mFilters.getFilterFieldHumanName(field.resId) + " " + getString(Movies.FILTER_OPERATOR_CONTAINS.resId));
            builder.setItems(availableValues, (dialog, item) -> filterClick(new Filter(field,
                    Movies.FILTER_OPERATOR_CONTAINS, availableValues[item])));
            builder.setNegativeButton(getString(R.string.dialog_negative),
                    (dialog, whichButton) -> {
                    });
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
            if (!value.isEmpty())
                availableValues.add(value);
        }

        return availableValues.toArray(new String[0]);
    }

    /**
     * Add filter and return to list activity
     */
    private void filterClick(Filter filter) {
        String val = filter.getHumanValue();
        if (SharedObjects.getInstance().movieListFragment != null && val != null && !val.isEmpty()) {
            SharedObjects.getInstance().movieListFragment.addExternalFilter(filter);
            if (getActivity() instanceof MovieDetailActivity) {
                getActivity().finish();
            }
        }
    }

}
