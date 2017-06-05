package com.holdingscythe.pocketamcreader;

import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.TextView;

import com.holdingscythe.pocketamcreader.catalog.Extras;
import com.holdingscythe.pocketamcreader.catalog.Movie;
import com.holdingscythe.pocketamcreader.catalog.Movies;
import com.holdingscythe.pocketamcreader.catalog.MoviesDataProvider;
import com.holdingscythe.pocketamcreader.filters.Filter;
import com.holdingscythe.pocketamcreader.filters.FilterField;
import com.holdingscythe.pocketamcreader.filters.FilterOperator;
import com.holdingscythe.pocketamcreader.filters.Filters;
import com.holdingscythe.pocketamcreader.utils.SharedObjects;

import java.util.ArrayList;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// TODO: cleanup

/**
 * A fragment representing a single Movie detail screen.
 * This fragment is either contained in a {@link MovieListActivity}
 * in two-pane mode (on tablets) or a {@link MovieDetailActivity}
 * on handsets.
 */
public class MovieDetailFragment extends Fragment implements OnClickListener {
    private Movie mMovie;
    private Extras mExtras;
    private MoviesDataProvider mMoviesDataProvider;
    private Filters mFilters;

    private SharedPreferences mPreferences;
    private String mSettingMultivaluedSeparator;
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

        // Setup filters
        mFilters = new Filters(getActivity());

        // Read preferences
        if (SharedObjects.getInstance().preferences == null) {
            // Prepare Shared Objects
            SharedObjects.getInstance().preferences = PreferenceManager.getDefaultSharedPreferences
                    (getActivity().getApplicationContext());
        }

        mPreferences = SharedObjects.getInstance().preferences;
//        this.settingIMDb = this.preferences.getString("settingIMDb", "original");
//        this.settingDefaultTab = Integer.valueOf(this.preferences.getString("settingDefaultTab", "0"));
//        this.settingShowColorTags = this.preferences.getBoolean("settingShowColorTags", true);
        mSettingMultivaluedSeparator = mPreferences.getString("settingMultivalueSeparator", ",/");
//        this.settingPlotInBasic = Integer.valueOf(this.preferences.getString("settingPlotInBasic", "1"));
//        this.settingFontSize = Integer.valueOf(this.preferences.getString("settingFontSize", "0"));
//        this.settingHideUnusedFields = this.preferences.getBoolean("hideUnusedFields", false);

        if (getArguments().containsKey(ARG_MOVIE_ID)) {
            // Prepare Data Provider
            mMoviesDataProvider = new MoviesDataProvider(getActivity());

            // Set (and compile) regular expressions
            regExpMultivaluedCleaner = Pattern.compile("\\s*\\([^\\)]*\\)+");
            regExpMultivaluedSeparator = "\\s*[" + Pattern.quote(mSettingMultivaluedSeparator) + "]\\s*";

            // Benchmark start
            long startTime;
            long endTime;

            if (S.INFO) {
                startTime = System.currentTimeMillis();
            }

            // Fetch movie
            Uri uri = Uri.withAppendedPath(S.CONTENT_URI, "_id/" + getArguments().getString(ARG_MOVIE_ID, "0"));
            Cursor cursor = mMoviesDataProvider.fetchMovie(uri);
            cursor.moveToFirst();
            mMovie = new Movie(cursor, getView(), this, getActivity());
            cursor.close();

            // Fetch extras
            Cursor cursorExtras = mMoviesDataProvider.fetchMovieExtras(Uri.withAppendedPath(uri, "extras"));
            cursorExtras.moveToFirst();
            mExtras = new Extras(cursorExtras);
            cursorExtras.close();

            // Close database
            mMoviesDataProvider.closeDatabase();

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
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_movie_detail, container, false);

//        rootView.findViewById(R.id.detail_image_teaser).set;

        return rootView;
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
                // If link points to IMDb, open IMDb app if available
                String url = ((TextView) view).getText().toString();
                Pattern regExpImdb = Pattern.compile("imdb\\.[\\w]+/(Title\\?|title/tt)(\\d{6,7})/?");
                Matcher m = regExpImdb.matcher(url);

                if (m.find()) {
                    // prepare uri
                    Uri uri = Uri.parse("imdb:///title/tt" + String.format(Locale.US, "%07d", Integer.parseInt(m
                            .group(2))) + "/");

                    // open intent
                    try {
                        Intent i = new Intent(Intent.ACTION_VIEW);
                        i.setData(uri);
                        startActivity(i);
                    } catch (ActivityNotFoundException e) {
                        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                        startActivity(browserIntent);
                    }
                } else {
                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                    startActivity(browserIntent);
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
            builder.setItems(availableValues, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int item) {
                    filterClick(new Filter(field, operator, availableValues[item]));
                }
            });
            builder.setNegativeButton(getString(R.string.dialog_negative), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int whichButton) {
                }
            });
            AlertDialog alertDialog = builder.create();
            alertDialog.show();
        }
    }

    /**
     * Split multivalued field into respective values
     */
    private String[] separateMultivaluedField(View v) {
        ArrayList<String> availableValues = new ArrayList<String>();
        TextView multiField = (TextView) v;
        String multiValue = multiField.getText().toString();

        Matcher m = regExpMultivaluedCleaner.matcher(multiValue);
        multiValue = m.replaceAll("");

        for (String value : multiValue.split(regExpMultivaluedSeparator)) {
            if (value.length() > 0)
                availableValues.add(value);
        }

        return availableValues.toArray(new String[availableValues.size()]);
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
            } else {
                // TODO: is this necessary?
//                getActivity().finish();
//                Intent listIntent = new Intent(getActivity(), MovieListActivity.class);
//                startActivity(listIntent);
            }
        }
    }

//    detailIntent.setData(data);

}
