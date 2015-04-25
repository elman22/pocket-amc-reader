package com.holdingscythe.pocketamcreader;

import android.app.AlertDialog;
import android.content.DialogInterface;
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

import com.holdingscythe.pocketamcreader.catalog.Movie;
import com.holdingscythe.pocketamcreader.catalog.Movies;
import com.holdingscythe.pocketamcreader.catalog.MoviesDataProvider;
import com.holdingscythe.pocketamcreader.filters.Filter;
import com.holdingscythe.pocketamcreader.filters.FilterField;
import com.holdingscythe.pocketamcreader.filters.FilterOperator;
import com.holdingscythe.pocketamcreader.filters.Filters;
import com.holdingscythe.pocketamcreader.utils.SharedObjects;

import java.util.ArrayList;
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
        mPreferences = SharedObjects.getInstance().preferences;
//        this.settingIMDb = this.preferences.getString("settingIMDb", "original");
//        this.settingDefaultTab = Integer.valueOf(this.preferences.getString("settingDefaultTab", "0"));
//        this.settingShowColorTags = this.preferences.getBoolean("settingShowColorTags", true);
        mSettingMultivaluedSeparator = mPreferences.getString("settingMultivalueSeparator", ",");
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
            mMovie = new Movie(cursor, getView(), this, getActivity());
            mMoviesDataProvider.closeDatabase();

            // Benchmark end
            if (S.INFO) {
                endTime = System.currentTimeMillis();
                Log.i(S.TAG, "Details display elapsed time: " + (endTime - startTime) + " ms.");
            }

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
            case R.id.Actors:
                chooseMultivaluedFieldValue(Movies.FILTER_ACTORS, Movies.FILTER_OPERATOR_CONTAINS, view);
                break;
        }
    }

    /** Prepare select to pick from available values in multivalued field */
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

    /** Split multivalued field into respective values */
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

    /** Add filter and return to list activity */
    private void filterClick(Filter filter) {
        String val = filter.getHumanValue();
        if (SharedObjects.getInstance().movieListFragment != null && val != null && !val.equals("")) {
            SharedObjects.getInstance().movieListFragment.addExternalFilter(filter);
            if (getActivity() instanceof MovieDetailActivity) {
                getActivity().finish();
            }
        }
    }

//    detailIntent.setData(data);

    private void loadMovie(String id) {

    }

    protected void showBasic() {

    }
}
