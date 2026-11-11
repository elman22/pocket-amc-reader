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

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.database.Cursor;
import android.os.Bundle;
import android.text.method.DigitsKeyListener;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.holdingscythe.pocketamcreader.catalog.Movies;
import com.holdingscythe.pocketamcreader.catalog.MoviesAdapter;
import com.holdingscythe.pocketamcreader.catalog.MoviesDataProvider;
import com.holdingscythe.pocketamcreader.filters.Filter;
import com.holdingscythe.pocketamcreader.filters.FilterField;
import com.holdingscythe.pocketamcreader.filters.FilterOperator;
import com.holdingscythe.pocketamcreader.filters.Filters;
import com.holdingscythe.pocketamcreader.settings.SettingsActivity;
import com.holdingscythe.pocketamcreader.settings.SettingsConstants;
import com.holdingscythe.pocketamcreader.utils.SharedObjects;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SearchView;
import androidx.core.os.BundleCompat;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


/**
 * A list fragment representing a list of Movies. This fragment
 * also supports tablet devices by allowing list items to be given an
 * 'activated' state upon selection. This helps indicate which item is
 * currently being viewed in a {@link MovieDetailFragment}.
 * Activities containing this fragment MUST implement the {@link Callbacks}
 * interface.
 */
public class MovieListFragment extends androidx.fragment.app.Fragment implements View.OnClickListener {

    /**
     * Items of the sort menu: sort order and the field the list is sorted by. Resource ids are not constants, so
     * they can't be used in a switch.
     */
    private static final class SortOption {
        final String order;
        final String field;

        SortOption(String order, String field) {
            this.order = order;
            this.field = field;
        }
    }

    private static final Map<Integer, SortOption> SORT_OPTIONS = new HashMap<>();

    static {
        SORT_OPTIONS.put(R.id.menu_sort_formattedtitle_asc,
                new SortOption(Movies.SORT_ORDER_TITLE_ASC, Movies.FORMATTED_TITLE));
        SORT_OPTIONS.put(R.id.menu_sort_formattedtitle_desc,
                new SortOption(Movies.SORT_ORDER_TITLE_DESC, Movies.FORMATTED_TITLE));
        SORT_OPTIONS.put(R.id.menu_sort_number_asc, new SortOption(Movies.SORT_ORDER_NUMBER_ASC, Movies.NUMBER));
        SORT_OPTIONS.put(R.id.menu_sort_number_desc, new SortOption(Movies.SORT_ORDER_NUMBER_DESC, Movies.NUMBER));
        SORT_OPTIONS.put(R.id.menu_sort_rating_asc, new SortOption(Movies.SORT_ORDER_RATING_ASC, Movies.RATING));
        SORT_OPTIONS.put(R.id.menu_sort_rating_desc, new SortOption(Movies.SORT_ORDER_RATING_DESC, Movies.RATING));
        SORT_OPTIONS.put(R.id.menu_sort_medialabel_asc,
                new SortOption(Movies.SORT_ORDER_MEDIALABEL_ASC, Movies.MEDIA_LABEL));
        SORT_OPTIONS.put(R.id.menu_sort_medialabel_desc,
                new SortOption(Movies.SORT_ORDER_MEDIALABEL_DESC, Movies.MEDIA_LABEL));
        SORT_OPTIONS.put(R.id.menu_sort_length_asc, new SortOption(Movies.SORT_ORDER_LENGTH_ASC, Movies.LENGTH));
        SORT_OPTIONS.put(R.id.menu_sort_length_desc, new SortOption(Movies.SORT_ORDER_LENGTH_DESC, Movies.LENGTH));
        SORT_OPTIONS.put(R.id.menu_sort_year_asc, new SortOption(Movies.SORT_ORDER_YEAR_ASC, Movies.YEAR));
        SORT_OPTIONS.put(R.id.menu_sort_year_desc, new SortOption(Movies.SORT_ORDER_YEAR_DESC, Movies.YEAR));
        SORT_OPTIONS.put(R.id.menu_sort_date_asc, new SortOption(Movies.SORT_ORDER_DATE_ASC, Movies.DATE));
        SORT_OPTIONS.put(R.id.menu_sort_date_desc, new SortOption(Movies.SORT_ORDER_DATE_DESC, Movies.DATE));
        SORT_OPTIONS.put(R.id.menu_sort_borrower_asc, new SortOption(Movies.SORT_ORDER_BORROWER_ASC, Movies.BORROWER));
        SORT_OPTIONS.put(R.id.menu_sort_borrower_desc,
                new SortOption(Movies.SORT_ORDER_BORROWER_DESC, Movies.BORROWER));
        SORT_OPTIONS.put(R.id.menu_sort_color_tag_asc,
                new SortOption(Movies.SORT_ORDER_COLOR_TAG_ASC, Movies.COLOR_TAG));
        SORT_OPTIONS.put(R.id.menu_sort_color_tag_desc,
                new SortOption(Movies.SORT_ORDER_COLOR_TAG_DESC, Movies.COLOR_TAG));
        SORT_OPTIONS.put(R.id.menu_sort_originaltitle_asc,
                new SortOption(Movies.SORT_ORDER_ORIGINAL_TITLE_ASC, Movies.ORIGINAL_TITLE));
        SORT_OPTIONS.put(R.id.menu_sort_originaltitle_desc,
                new SortOption(Movies.SORT_ORDER_ORIGINAL_TITLE_DESC, Movies.ORIGINAL_TITLE));
        SORT_OPTIONS.put(R.id.menu_sort_translatedtitle_asc,
                new SortOption(Movies.SORT_ORDER_TRANSLATED_TITLE_ASC, Movies.TRANSLATED_TITLE));
        SORT_OPTIONS.put(R.id.menu_sort_translatedtitle_desc,
                new SortOption(Movies.SORT_ORDER_TRANSLATED_TITLE_DESC, Movies.TRANSLATED_TITLE));
        SORT_OPTIONS.put(R.id.menu_sort_datewatched_asc,
                new SortOption(Movies.SORT_ORDER_DATE_WATCHED_ASC, Movies.DATE_WATCHED));
        SORT_OPTIONS.put(R.id.menu_sort_datewatched_desc,
                new SortOption(Movies.SORT_ORDER_DATE_WATCHED_DESC, Movies.DATE_WATCHED));
        SORT_OPTIONS.put(R.id.menu_sort_userrating_asc,
                new SortOption(Movies.SORT_ORDER_USER_RATING_ASC, Movies.USER_RATING));
        SORT_OPTIONS.put(R.id.menu_sort_userrating_desc,
                new SortOption(Movies.SORT_ORDER_USER_RATING_DESC, Movies.USER_RATING));
        SORT_OPTIONS.put(R.id.menu_sort_certification_asc,
                new SortOption(Movies.SORT_ORDER_CERTIFICATION_ASC, Movies.CERTIFICATION));
        SORT_OPTIONS.put(R.id.menu_sort_certification_desc,
                new SortOption(Movies.SORT_ORDER_CERTIFICATION_DESC, Movies.CERTIFICATION));
    }

    /**
     * The serialization (saved instance state) Bundle key representing the activated item position. Only used on
     * tablets.
     */
    private static final String STATE_ACTIVATED_POSITION = "activated_position";

    // Saved current state of filters
    private static final String STATE_FILTERS = "active_filters";
    private static final String STATE_FILTER_QUERY = "active_filter_query";

    /**
     * The fragment's current callback object, which is notified of list item clicks.
     */
    private Callbacks mCallbacks = sDummyCallbacks;

    /**
     * The current activated item position. Only used on tablets.
     */
    private int mActivatedPosition = ListView.INVALID_POSITION;

    private MoviesDataProvider mMoviesDataProvider;
    private MoviesAdapter mMoviesAdapter;
    private Filters mFilters;
    private String mFilterQuery;
    private RecyclerView mRecyclerView;
    private RecyclerView.LayoutManager mGridLayoutManager, mLinearLayoutManager;
    private TextView mHeaderListCountView;
    private TextView mFilterHeaderLabelText;
    private TextView mFilterHeaderText;
    private MenuItem mSearchMenuItem;
    private Menu mMenu;

    // Date picker variables
    private int mDpYear;
    private int mDpMonth;
    private int mDpDay;

    // Recycler views
    private final int GRID = 0;
    private final int LIST = 1;
    private final int GRID_SPAN = 3;
    private final int GRID_SPAN_LANDSCAPE = 6;
    private int mViewType;
    private CursorAdapterObserver mAdapterObserver;

    protected class CursorAdapterObserver extends RecyclerView.AdapterDataObserver {
        CursorAdapterObserver() {
        }

        private boolean mUpdateScheduled = false;
        private final Runnable mUpdate = () -> {
            mUpdateScheduled = false;
            if (isAdded()) {
                updateHeaderInfo();
                resetDetailPager();
            }
        };

        /**
         * A new cursor is reported by several range events, update the screen once
         */
        private void scheduleUpdate() {
            if (!mUpdateScheduled && mRecyclerView != null) {
                mUpdateScheduled = true;
                mRecyclerView.post(mUpdate);
            }
        }

        @Override
        public void onChanged() {
            scheduleUpdate();
        }

        @Override
        public void onItemRangeChanged(int positionStart, int itemCount) {
            scheduleUpdate();
        }

        @Override
        public void onItemRangeInserted(int positionStart, int itemCount) {
            scheduleUpdate();
        }

        @Override
        public void onItemRangeRemoved(int positionStart, int itemCount) {
            scheduleUpdate();
        }
    }

    protected class FilterDatePickerListener implements DatePickerDialog.OnDateSetListener {
        private final FilterField mFilterField;
        private final FilterOperator mFilterOperator;
        private Boolean mIsFilterSet = false; // workaround because Android calls onDateSet twice

        FilterDatePickerListener(FilterField filterField, FilterOperator filterOperator) {
            mFilterField = filterField;
            mFilterOperator = filterOperator;
        }

        @Override
        public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
            if (!mIsFilterSet) {
                String filterValue = year + "-"
                        + String.format("%2s", (monthOfYear + 1)).replace(' ', '0') + "-"
                        + String.format("%2s", dayOfMonth).replace(' ', '0');
                mFilters.addFilter(new Filter(mFilterField, mFilterOperator, filterValue));
                mIsFilterSet = true;
                refreshList();
            }
        }
    }

    /**
     * A callback interface that all activities containing this fragment must
     * implement. This mechanism allows activities to be notified of item selections.
     */
    public interface Callbacks {
        /**
         * Callback for when an item has been selected.
         */
        void onItemSelected(String id);
    }

    /**
     * A dummy implementation of the {@link Callbacks} interface that does
     * nothing. Used only when this fragment is not attached to an activity.
     */
    private static final Callbacks sDummyCallbacks = id -> {
    };

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public MovieListFragment() {
        mFilterQuery = "";
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        // Prepare date picker
        final Calendar c = Calendar.getInstance();
        mDpYear = c.get(Calendar.YEAR);
        mDpMonth = c.get(Calendar.MONTH);
        mDpDay = c.get(Calendar.DAY_OF_MONTH);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_movie_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Restore the previously serialized activated item position.
        if (savedInstanceState != null && savedInstanceState.containsKey(STATE_ACTIVATED_POSITION)) {
            setActivatedPosition(savedInstanceState.getInt(STATE_ACTIVATED_POSITION));
        }
    }

    @SuppressLint("StringFormatInvalid")
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // Read preferences
        if (SharedObjects.getInstance().preferences == null) {
            // Prepare Shared Objects
            SharedObjects.getInstance().preferences = PreferenceManager.getDefaultSharedPreferences
                    (requireActivity().getApplicationContext());
        }

        // Setup filters from bundle if available
        if (savedInstanceState != null && savedInstanceState.containsKey(STATE_FILTERS)) {
            try {
                mFilters = BundleCompat.getSerializable(savedInstanceState, STATE_FILTERS, Filters.class);
            } catch (Exception ex) {
                Log.w(S.TAG, "Error de-serializing mFilters: " + ex);
                mFilters = new Filters();
            }
        } else {
            mFilters = new Filters();
        }
        assert mFilters != null;
        mFilters.setContext(getActivity());

        // Setup filter query from bundle
        if (savedInstanceState != null && savedInstanceState.containsKey(STATE_FILTER_QUERY)) {
            mFilterQuery = savedInstanceState.getString(STATE_FILTER_QUERY);
        }

        // Setup database and filters
        mMoviesDataProvider = new MoviesDataProvider(getActivity());
        mMoviesDataProvider.setFilters(mFilters);

        // Prepare recycler view
        mRecyclerView = requireView().findViewById(R.id.movie_list_recycler);

        // Define layout managers
        mLinearLayoutManager = new LinearLayoutManager(requireActivity().getApplicationContext());
        int gridSpan = (requireActivity().getResources().getConfiguration().orientation == Configuration
                .ORIENTATION_PORTRAIT ? GRID_SPAN : GRID_SPAN_LANDSCAPE);
        mGridLayoutManager = new GridLayoutManager(requireActivity().getApplicationContext(), gridSpan);

        // Setting view preference or list as default view
        mViewType = SharedObjects.getInstance().preferences.getInt(SettingsConstants.KEY_PREF_MOVIE_LIST_VIEW_ID, LIST);

        // Add counter header
        mHeaderListCountView = requireActivity().findViewById(R.id.nowShowing);
        LinearLayout headerView = requireActivity().findViewById(R.id.showing_info_layout);
        headerView.setOnClickListener(this);

        // Add filter header
        mFilterHeaderLabelText = requireActivity().findViewById(R.id.filter_label);
        mFilterHeaderText = requireActivity().findViewById(R.id.filter_detail);
        RelativeLayout headerViewFilter = requireActivity().findViewById(R.id.filter_info_layout);
        headerViewFilter.setOnClickListener(this);

        // Prepare adapter to list
        prepareMoviesAdapter();

        mRecyclerView.setNestedScrollingEnabled(true);

        // Prepare recycler view
        if (mViewType == GRID) {
            mRecyclerView.setLayoutManager(mGridLayoutManager);
        } else {
            mRecyclerView.setLayoutManager(mLinearLayoutManager);
        }
        mRecyclerView.setAdapter(mMoviesAdapter);

        // Update list header views info
        updateHeaderInfo();

        // Store references
        SharedObjects.getInstance().movieListFragment = this;

        // Show welcome screen if zero movies are displayed
        if (mMoviesAdapter.getItemCount() == 0 && mFilters.getCount() == 0) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle(getString(R.string.welcome_title));
            builder.setMessage(String.format(getString(R.string.welcome_message), getString(R.string.menu_settings),
                    getString(R.string.pref_setting_catalog), getString(R.string.pref_setting_encoding)));
            builder.setNeutralButton(getString(R.string.welcome_dialog_dismiss), (dialog, id) -> {
                dialog.cancel();
                startActivity(new Intent(getActivity(), SettingsActivity.class));
            });
            AlertDialog alertDialog = builder.create();
            alertDialog.show();
        }

        resetDetailPager();
    }

    @Override
    public void onAttach(@NonNull Activity activity) {
        super.onAttach(activity);

        // Activities containing this fragment must implement its callbacks.
        if (!(activity instanceof Callbacks)) {
            throw new IllegalStateException("Activity must implement fragment's callbacks.");
        }

        mCallbacks = (Callbacks) activity;
    }

    @Override
    public void onDetach() {
        super.onDetach();

        // Reset the active callbacks interface to the dummy implementation.
        mCallbacks = sDummyCallbacks;
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mActivatedPosition != ListView.INVALID_POSITION) {
            // Serialize and persist the activated item position.
            outState.putInt(STATE_ACTIVATED_POSITION, mActivatedPosition);
        }

        // Save filter status if some exist
        if (mFilters != null && mFilters.getCount() > 0) {
            try {
                outState.putSerializable(STATE_FILTERS, mFilters);
            } catch (Exception ex) {
                Log.w(S.TAG, "Error serializing mFilters: " + ex);
            }
        }

        // Save filter query
        if (!mFilterQuery.isEmpty()) {
            outState.putString(STATE_FILTER_QUERY, mFilterQuery);
        }
    }

    /**
     * Create menu for main activity
     */
    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, MenuInflater inflater) {
        // Store reference for future use
        mMenu = menu;

        // Inflate menu
        inflater.inflate(R.menu.main_menu, menu);

        // Set correct movie order
        int settingMovieListOrderId =
                SharedObjects.getInstance().preferences.getInt(SettingsConstants.KEY_PREF_MOVIE_LIST_ORDER_ID, 0);
        try {
            menu.findItem(settingMovieListOrderId).setChecked(true);
        } catch (NullPointerException e) {
            if (S.INFO)
                Log.i(S.TAG, "Preferred list order not found.");
        }

        // Set correct icon for layout view
        changeMenuLayoutIcon();

        // Add search widget
        mSearchMenuItem = menu.findItem(R.id.menu_search);
        SearchView searchView = (SearchView) mSearchMenuItem.getActionView();

        // If filter query was set via bundle, apply it
        if (!mFilterQuery.isEmpty() && mMoviesAdapter != null) {
            mSearchMenuItem.expandActionView();
            Objects.requireNonNull(searchView).setQuery(mFilterQuery, false);
            mMoviesAdapter.getFilter().filter(mFilterQuery);
        }

        Objects.requireNonNull(searchView).setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                mMoviesAdapter.getFilter().filter(newText);
                mFilterQuery = newText;
                return true;
            }
        });
    }

    /**
     * Catch menu clicked actions
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Settings and About items are handled in MovieListActivity
        int itemId = item.getItemId();

        if (itemId == R.id.menu_add_filter) {
            addFilter();
            return true;
        }

        if (itemId == R.id.menu_switch_view) {
            if (mViewType == GRID) {
                // Change to list view
                mViewType = LIST;
                prepareMoviesAdapter();
                mRecyclerView.setLayoutManager(mLinearLayoutManager);
            } else {
                // Change to grid view
                mViewType = GRID;
                prepareMoviesAdapter();
                mRecyclerView.setLayoutManager(mGridLayoutManager);
            }
            mRecyclerView.setAdapter(mMoviesAdapter);

            // Set filter query from previous view
            if (!mFilterQuery.isEmpty())
                mMoviesAdapter.getFilter().filter(mFilterQuery);

            // Save preferences
            SharedPreferences.Editor editor = SharedObjects.getInstance().preferences.edit();
            editor.putInt(SettingsConstants.KEY_PREF_MOVIE_LIST_VIEW_ID, mViewType);
            editor.apply();

            // Change menu icon
            changeMenuLayoutIcon();

            return true;
        }

        SortOption sortOption = SORT_OPTIONS.get(itemId);
        if (sortOption != null) {
            setSortOrder(itemId, sortOption.order, sortOption.field);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Click on filter list header
     */
    @Override
    public void onClick(View v) {
        int viewId = v.getId();
        if (viewId == R.id.filter_info_layout || viewId == R.id.showing_info_layout) {
            removeFilters();
        }
    }

    /**
     * Called when the activity is resumed.
     */
    @Override
    public void onResume() {
        super.onResume();

        // Restart if activity is not available
        if (!isAdded())
            SharedObjects.getInstance().restartAppRequested = true;

        // Restart if adapter is not available
        if (mMoviesAdapter == null && mRecyclerView != null)
            SharedObjects.getInstance().restartAppRequested = true;

        // Restart if preferences are not available
        if (SharedObjects.getInstance().preferences == null)
            SharedObjects.getInstance().restartAppRequested = true;

        // Restart
        if (SharedObjects.getInstance().restartAppRequested) {
            if (S.INFO)
                Log.i(S.TAG, "App restarting...");

            SharedObjects.getInstance().restartAppRequested = false;
            SharedObjects.getInstance().moviesListActivityRefreshRequested = false;

            Intent importIntent = new Intent(getActivity(), ImportActivity.class);
            if (getActivity() != null)
                getActivity().finish();
            startActivity(importIntent);

        } else if (SharedObjects.getInstance().moviesListActivityRefreshRequested) {
            // Refresh list
            if (S.INFO)
                Log.i(S.TAG, "List refreshing...");

            SharedObjects.getInstance().moviesListActivityRefreshRequested = false;
            refreshList();
        }
    }

    /**
     * Called when the activity is destroyed.
     */
    @Override
    public void onDestroy() {
        // Don't keep the destroyed fragment and its adapter (with closed cursor) for other screens
        SharedObjects shared = SharedObjects.getInstance();
        if (shared.movieListFragment == this)
            shared.movieListFragment = null;
        if (shared.recyclerMovieAdapter == mMoviesAdapter)
            shared.recyclerMovieAdapter = null;

        if (mMoviesAdapter != null && mMoviesAdapter.getCursor() != null) {
            mMoviesAdapter.getCursor().close();
        }

        try {
            mRecyclerView.setAdapter(null);
        } catch (Exception e) {
            if (S.ERROR)
                Log.e(S.TAG, "Content view not yet created");
        }

        if (mMoviesDataProvider != null)
            mMoviesDataProvider.closeDatabase();

        super.onDestroy();
    }

    /**
     * Turns on activate-on-click mode. When this mode is on, list items will be
     * given the 'activated' state when touched.
     */
    public void  setActivateOnItemClick(boolean activateOnItemClick) {
        // When setting CHOICE_MODE_SINGLE, ListView will automatically
        // give items the 'activated' state when touched.
        // TODO update when implementing TwoPane
//        mListView.setChoiceMode(activateOnItemClick
//                ? ListView.CHOICE_MODE_SINGLE
//                : ListView.CHOICE_MODE_NONE);
    }

    private void setActivatedPosition(int position) {
        // TODO update when implementing TwoPane
//        if (position == ListView.INVALID_POSITION) {
//            mListView.setItemChecked(mActivatedPosition, false);
//        } else {
//            mListView.setItemChecked(position, true);
//        }

        mActivatedPosition = position;
    }

    /**
     * Updates list header about number of movies displayed
     */
    private void updateHeaderInfo() {
        mHeaderListCountView.setText(String.format(getResources().getQuantityString(R.plurals.list_showing,
                mMoviesAdapter.getItemCount()), mMoviesAdapter.getItemCount()));

        mFilterHeaderLabelText.setText(getResources().getQuantityString(R.plurals.filter_label, mFilters.getCount()));
        mFilterHeaderText.setText(mFilters.getFiltersHumanInfo());
    }

    /**
     * Set default order to preferences and sort list
     */
    private void setSortOrder(int menuId, String order, String field) {
        if (mMoviesDataProvider != null && mRecyclerView != null) {
            SharedPreferences.Editor editor = SharedObjects.getInstance().preferences.edit();
            editor.putInt(SettingsConstants.KEY_PREF_MOVIE_LIST_ORDER_ID, menuId);
            editor.putString(SettingsConstants.KEY_PREF_MOVIE_LIST_ORDER, order);
            editor.putString(SettingsConstants.KEY_PREF_MOVIE_LIST_ORDER_FIELD, field);
            editor.apply();
            mMenu.findItem(menuId).setChecked(true);
            refreshList();
        }
    }

    /**
     * Refresh list view
     */
    private void refreshList() {
        if (mMoviesDataProvider != null && mRecyclerView != null) {
            if (SharedObjects.getInstance().preferences == null) {
                SharedObjects.getInstance().preferences = PreferenceManager.getDefaultSharedPreferences
                        (requireActivity().getApplicationContext());
            }

            // Close search field
            if (mSearchMenuItem != null)
                mSearchMenuItem.collapseActionView();

            // Swap cursor
            mMoviesAdapter.loadConfiguration(requireActivity().getBaseContext());
            Cursor oldCursor = mMoviesAdapter.swapCursor(mMoviesDataProvider.fetchMovies(S.CONTENT_URI));
            if (oldCursor != null)
                oldCursor.close();

            // Scroll to top
            mRecyclerView.scrollToPosition(0);
        }
    }

    /**
     * Display first movie in details pager to if two pane display
     */
    private void resetDetailPager() {
        if (SharedObjects.getInstance().twoPane && mMoviesAdapter.getItemCount() > 0) {
            mMoviesAdapter.getCursor().moveToFirst();
            mCallbacks.onItemSelected("");
        }
    }

    /**
     * Function to add new filter from wizard - 1st step
     */
    private void addFilter() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(getString(R.string.filter_add_first_step));
        builder.setItems(mFilters.getAvailableFilters(), (dialog, item) -> {
            // Shortcut for boolean type
            if (Movies.availableFilterFields[item].type == Movies.FILTER_TYPE_BOOLEAN) {
                addFilterThirdStep(Movies.availableFilterFields[item], Movies.FILTER_OPERATOR_EQUALS);
            } else {
                addFilterSecondStep(Movies.availableFilterFields[item]);
            }
        });
        builder.setNegativeButton(getString(R.string.dialog_negative), (dialog, whichButton) -> {
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    /**
     * Function to add new filter from wizard - 2nd step
     */
    private void addFilterSecondStep(final FilterField field) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(mFilters.getFilterFieldHumanName(field.resId));
        builder.setItems(mFilters.getAvailableOperators(field),
                (dialog, item) -> addFilterThirdStep(field, field.type.operators[item]));
        builder.setNegativeButton(getString(R.string.dialog_negative), (dialog, whichButton) -> {
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    /**
     * Function to add new filter from wizard - 3rd step
     */
    private void addFilterThirdStep(final FilterField field, final FilterOperator operator) {
        // This step is not needed when operator equals "is (not) null"
        if (operator == Movies.FILTER_OPERATOR_IS_NULL || operator == Movies.FILTER_OPERATOR_IS_NULL_NOT) {
            mFilters.addFilter(new Filter(field, operator));
            refreshList();
            return;
        }

        // Date picker is handled different way
        if (field.type == Movies.FILTER_TYPE_DATE) {
            new DatePickerDialog(requireActivity(), new FilterDatePickerListener(field, operator),
                    mDpYear, mDpMonth, mDpDay).show();
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(mFilters.getFilterFieldHumanName(field.resId) + " " + getString(operator.resId));

        if (field.type == Movies.FILTER_TYPE_BOOLEAN) {
            // If filter type boolean show list yes/no
            final String[] booleanValues = new String[]{getString(R.string.details_boolean_true),
                    getString(R.string.details_boolean_false)};
            builder.setItems(booleanValues, (dialog, item) -> {
                mFilters.addFilter(new Filter(field, operator, booleanValues[item],
                        getString(R.string.details_boolean_true)));
                refreshList();
            });
        } else {
            // Prepare view with text input
            View filterView = requireActivity().getLayoutInflater().inflate(R.layout.filter_input, null);
            final EditText filterValueView = filterView.findViewById(R.id.filterValue);
            if (field.type == Movies.FILTER_TYPE_NUMBER) {
                DigitsKeyListener digitsKeyListener = DigitsKeyListener.getInstance(false, true);
                filterValueView.setKeyListener(digitsKeyListener);
            }
            builder.setView(filterView);

            // Prepare positive button
            builder.setPositiveButton(getString(R.string.dialog_positive), (dialog, button) -> {
                String filterValue = filterValueView.getText().toString();
                mFilters.addFilter(new Filter(field, operator, filterValue));
                refreshList();
            });
        }

        // Negative button is for both options
        builder.setNegativeButton(getString(R.string.dialog_negative), (dialog, whichButton) -> {
        });

        AlertDialog alertDialog = builder.create();

        if (field.type == Movies.FILTER_TYPE_TEXT || field.type == Movies.FILTER_TYPE_NUMBER) {
            // The keyboard is shown with the dialog and hidden when it is dismissed
            Window window = alertDialog.getWindow();
            if (window != null) {
                window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
            }
        }
        alertDialog.show();
    }

    /**
     * Add new filter from external fragments
     */
    public void addExternalFilter(Filter filter) {
        mFilters.addFilter(filter);
        refreshList();
    }

    /**
     * Function to remove one or all filters
     * If there is only one filter - clear it, if there is more, user chooses
     */
    private void removeFilters() {
        if (mFilters.getCount() == 1) {
            // if there is only one filter, remove and refresh
            mFilters.removeAllFilters();
            refreshList();
        } else if (mFilters.getCount() > 1) {
            // if there are more filters, let user decide
            // prepare array of all filters
            ArrayList<String> filtersList = new ArrayList<>();
            filtersList.add(getString(R.string.filter_remove_all));
            filtersList.addAll(mFilters.getFiltersHumanInfoList());

            // build alert dialog
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle(getString(R.string.filter_remove));
            builder.setItems(filtersList.toArray(new String[0]), (dialog, item) -> {
                if (item == 0) {
                    mFilters.removeAllFilters();
                } else {
                    mFilters.removeFilter(item - 1);
                }
                refreshList();
            });
            builder.setNegativeButton(getString(R.string.dialog_negative), (dialog, whichButton) -> {
            });
            AlertDialog alertDialog = builder.create();
            alertDialog.show();
        }
    }

    /**
     * Create or update movies adapter
     */
    private void prepareMoviesAdapter() {
        MoviesAdapter previousAdapter = mMoviesAdapter;
        CursorAdapterObserver previousObserver = mAdapterObserver;

        mMoviesAdapter = new MoviesAdapter(
                requireActivity().getBaseContext(),
                mMoviesDataProvider.query(S.CONTENT_URI, SharedObjects.getInstance().moviesProjection, null, null,
                        null),
                mViewType,
                (v, position) -> {
                    // Open movie detail when clicked on item
                    if (S.INFO)
                        Log.i(S.TAG, "List clicked position: " + position);

                    mCallbacks.onItemSelected(String.valueOf(position));
                }
        );

        // Set filter query provider
        mMoviesAdapter.setFilterQueryProvider(mMoviesDataProvider);

        // Data change observer
        mAdapterObserver = new CursorAdapterObserver();
        mMoviesAdapter.registerAdapterDataObserver(mAdapterObserver);

        // Store references
        SharedObjects.getInstance().recyclerMovieAdapter = mMoviesAdapter;

        // The previous adapter (view type changed) is not used anymore, don't leave its cursor open. The caller sets
        // the new adapter to the RecyclerView right away, before the previous one could bind a closed cursor.
        if (previousAdapter != null) {
            if (previousObserver != null)
                previousAdapter.unregisterAdapterDataObserver(previousObserver);
            if (previousAdapter.getCursor() != null)
                previousAdapter.getCursor().close();
        }
    }

    /**
     * Change icon in menu according to used layout
     */
    public void changeMenuLayoutIcon() {
        if (mMenu != null) {
            MenuItem item = mMenu.findItem(R.id.menu_switch_view);
            if (item != null) {
                if (mViewType == LIST) {
                    item.setIcon(R.drawable.ic_grid_on_white_48dp);
                } else {
                    item.setIcon(R.drawable.ic_list_white_48dp);
                }
            }
        }
    }

}
