package com.holdingscythe.pocketamcreader;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.DataSetObserver;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.text.method.DigitsKeyListener;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;

import com.holdingscythe.pocketamcreader.catalog.Movies;
import com.holdingscythe.pocketamcreader.catalog.MoviesAdapter;
import com.holdingscythe.pocketamcreader.catalog.MoviesDataProvider;
import com.holdingscythe.pocketamcreader.filters.Filter;
import com.holdingscythe.pocketamcreader.filters.FilterField;
import com.holdingscythe.pocketamcreader.filters.FilterOperator;
import com.holdingscythe.pocketamcreader.filters.Filters;
import com.holdingscythe.pocketamcreader.settings.SettingsActivity;
import com.holdingscythe.pocketamcreader.utils.SharedObjects;

import java.util.ArrayList;
import java.util.Calendar;

// TODO: cleanup

/**
 * A list fragment representing a list of Movies. This fragment
 * also supports tablet devices by allowing list items to be given an
 * 'activated' state upon selection. This helps indicate which item is
 * currently being viewed in a {@link MovieDetailFragment}.
 * Activities containing this fragment MUST implement the {@link Callbacks}
 * interface.
 */
public class MovieListFragment extends ListFragment implements View.OnClickListener {

    /**
     * The serialization (saved instance state) Bundle key representing the activated item position. Only used on
     * tablets.
     */
    private static final String STATE_ACTIVATED_POSITION = "activated_position";

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
    private TextView mHeaderListCountView;
    private TextView mFilterHeaderText;
    private CursorAdapterObserver mCursorAdapterObserver;
    private MenuItem mSearchMenuItem;
    private Menu mMenu;

    // Date picker variables
    private int mDpYear;
    private int mDpMonth;
    private int mDpDay;

    protected class CursorAdapterObserver extends DataSetObserver {
        CursorAdapterObserver() {
        }

        @Override
        public void onChanged() {
            updateHeaderInfo();
            updateHeaderFilterInfo();
            resetDetailPager();
        }
    }

    protected class FilterDatePickerListener implements DatePickerDialog.OnDateSetListener {
        private FilterField mFilterField;
        private FilterOperator mFilterOperator;
        private Boolean mIsFilterSet = false; // workaround because Android calls onDateSet twice

        FilterDatePickerListener(FilterField filterField, FilterOperator filterOperator) {
            mFilterField = filterField;
            mFilterOperator = filterOperator;
        }

        @Override
        public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
            if (!mIsFilterSet) {
                String filterValue = String.valueOf(year) + "-"
                        + String.format("%2s", String.valueOf(monthOfYear + 1)).replace(' ', '0') + "-"
                        + String.format("%2s", String.valueOf(dayOfMonth)).replace(' ', '0');
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
    private static Callbacks sDummyCallbacks = new Callbacks() {
        @Override
        public void onItemSelected(String id) {
        }
    };

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public MovieListFragment() {
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
    public void onViewCreated(View view, Bundle savedInstanceState) {
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

        // Setup database and filters
        mMoviesDataProvider = new MoviesDataProvider(getActivity());
        mFilters = new Filters(getActivity());
        mMoviesDataProvider.setFilters(mFilters);

        // Remove divider
        getListView().setDivider(null);
        getListView().setDividerHeight(0);

        // Add counter header
        View headerView = getActivity().getLayoutInflater().inflate(R.layout.list_header, null);
        mHeaderListCountView = (TextView) headerView.findViewById(R.id.nowShowing);
        headerView.setOnClickListener(this);
        getListView().addHeaderView(headerView, null, false);

        // Add filter header
        View headerViewFilter = getActivity().getLayoutInflater().inflate(R.layout.list_header_filter, null);
//            this.filterHeaderLabelText = (TextView) headerViewFilter.findViewById(R.id.filter_label);
        mFilterHeaderText = (TextView) headerViewFilter.findViewById(R.id.filter_detail);
//            this.filterHeaderActionText = (TextView) headerViewFilter.findViewById(R.id.filter_detail_action);
        headerViewFilter.setOnClickListener(this);
//            headerViewFilter.setOnTouchListener(this);
        getListView().addHeaderView(headerViewFilter, null, false);

        // Attach adapter to list
        mMoviesAdapter = new MoviesAdapter(
                getActivity(),
                R.layout.list_movie_item,
                mMoviesDataProvider.query(S.CONTENT_URI, SharedObjects.getInstance().moviesProjection, null, null,
                        null),
                SharedObjects.getInstance().moviesProjection,
                new int[]{}
        );

        // Set filter query provider
        mMoviesAdapter.setFilterQueryProvider(mMoviesDataProvider);

        // Data change observer
        mCursorAdapterObserver = new CursorAdapterObserver();
        mMoviesAdapter.registerDataSetObserver(mCursorAdapterObserver);

        // Set list view parameters
        getListView().setFastScrollEnabled(true);
        getListView().setChoiceMode(ListView.CHOICE_MODE_SINGLE);

        // Set list adapter
        setListAdapter(mMoviesAdapter);

        // Update list header views info
        updateHeaderInfo();
        updateHeaderFilterInfo();

        // Store references
        SharedObjects.getInstance().listMovieAdapter = mMoviesAdapter;
        SharedObjects.getInstance().movieListFragment = this;

        // Show welcome screen if zero movies are displayed
        if (mMoviesAdapter.getCount() == 0 && mFilters.getCount() == 0) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle(getString(R.string.welcome_title));
            builder.setMessage(String.format(getString(R.string.welcome_message), getString(R.string.menu_settings),
                    getString(R.string.pref_setting_catalog), getString(R.string.pref_setting_encoding)));
            builder.setNeutralButton(getString(R.string.welcome_dialog_dismiss), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int id) {
                    dialog.cancel();
                    startActivity(new Intent(getActivity(), SettingsActivity.class));
                }
            });
            AlertDialog alertDialog = builder.create();
            alertDialog.show();
        }

        resetDetailPager();
    }

    @Override
    public void onAttach(Activity activity) {
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
    public void onListItemClick(ListView listView, View view, int position, long id) {
        super.onListItemClick(listView, view, position, id);

        // Notify the active callbacks interface (the activity, if the
        // fragment is attached to one) that an item has been selected.
        mCallbacks.onItemSelected(String.valueOf(id));
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mActivatedPosition != ListView.INVALID_POSITION) {
            // Serialize and persist the activated item position.
            outState.putInt(STATE_ACTIVATED_POSITION, mActivatedPosition);
        }
    }

    /**
     * Create menu for main activity
     */
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // Store reference for future use
        mMenu = menu;

        // Inflate menu
        inflater.inflate(R.menu.main_menu, menu);

        // Set correct movie order
        int settingMovieListOrderId = SharedObjects.getInstance().preferences.getInt("settingMovieListOrderId", 0);
        try {
            menu.findItem(settingMovieListOrderId).setChecked(true);
        } catch (NullPointerException e) {
            if (S.INFO)
                Log.i(S.TAG, "Preferred list order not found.");
        }

        // Add search widget
        mSearchMenuItem = menu.findItem(R.id.menu_search);
        SearchView searchView = (SearchView) mSearchMenuItem.getActionView();

        // Do actions when text in search widget changes
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextChange(String newText) {
                mMoviesAdapter.getFilter().filter(newText);
                return true;
            }

            @Override
            public boolean onQueryTextSubmit(String query) {
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
        switch (item.getItemId()) {
            case R.id.menu_add_filter:
                addFilter();
                return true;
            case R.id.menu_sort_formattedtitle_asc:
                setSortOrder(R.id.menu_sort_formattedtitle_asc, Movies.SORT_ORDER_TITLE_ASC);
                return true;
            case R.id.menu_sort_formattedtitle_desc:
                setSortOrder(R.id.menu_sort_formattedtitle_desc, Movies.SORT_ORDER_TITLE_DESC);
                return true;
            case R.id.menu_sort_number_asc:
                setSortOrder(R.id.menu_sort_number_asc, Movies.SORT_ORDER_NUMBER_ASC);
                return true;
            case R.id.menu_sort_number_desc:
                setSortOrder(R.id.menu_sort_number_desc, Movies.SORT_ORDER_NUMBER_DESC);
                return true;
            case R.id.menu_sort_rating_asc:
                setSortOrder(R.id.menu_sort_rating_asc, Movies.SORT_ORDER_RATING_ASC);
                return true;
            case R.id.menu_sort_rating_desc:
                setSortOrder(R.id.menu_sort_rating_desc, Movies.SORT_ORDER_RATING_DESC);
                return true;
            case R.id.menu_sort_medialabel_asc:
                setSortOrder(R.id.menu_sort_medialabel_asc, Movies.SORT_ORDER_MEDIALABEL_ASC);
                return true;
            case R.id.menu_sort_medialabel_desc:
                setSortOrder(R.id.menu_sort_medialabel_desc, Movies.SORT_ORDER_MEDIALABEL_DESC);
                return true;
            case R.id.menu_sort_length_asc:
                setSortOrder(R.id.menu_sort_length_asc, Movies.SORT_ORDER_LENGTH_ASC);
                return true;
            case R.id.menu_sort_length_desc:
                setSortOrder(R.id.menu_sort_length_desc, Movies.SORT_ORDER_LENGTH_DESC);
                return true;
            case R.id.menu_sort_year_asc:
                setSortOrder(R.id.menu_sort_year_asc, Movies.SORT_ORDER_YEAR_ASC);
                return true;
            case R.id.menu_sort_year_desc:
                setSortOrder(R.id.menu_sort_year_desc, Movies.SORT_ORDER_YEAR_DESC);
                return true;
            case R.id.menu_sort_date_asc:
                setSortOrder(R.id.menu_sort_date_asc, Movies.SORT_ORDER_DATE_ASC);
                return true;
            case R.id.menu_sort_date_desc:
                setSortOrder(R.id.menu_sort_date_desc, Movies.SORT_ORDER_DATE_DESC);
                return true;
            case R.id.menu_sort_borrower_asc:
                setSortOrder(R.id.menu_sort_borrower_asc, Movies.SORT_ORDER_BORROWER_ASC);
                return true;
            case R.id.menu_sort_borrower_desc:
                setSortOrder(R.id.menu_sort_borrower_desc, Movies.SORT_ORDER_BORROWER_DESC);
                return true;
            case R.id.menu_sort_color_tag_asc:
                setSortOrder(R.id.menu_sort_color_tag_asc, Movies.SORT_ORDER_COLOR_TAG_ASC);
                return true;
            case R.id.menu_sort_color_tag_desc:
                setSortOrder(R.id.menu_sort_color_tag_desc, Movies.SORT_ORDER_COLOR_TAG_DESC);
                return true;
            case R.id.menu_sort_originaltitle_asc:
                setSortOrder(R.id.menu_sort_originaltitle_asc, Movies.SORT_ORDER_ORIGINAL_TITLE_ASC);
                return true;
            case R.id.menu_sort_originaltitle_desc:
                setSortOrder(R.id.menu_sort_originaltitle_desc, Movies.SORT_ORDER_ORIGINAL_TITLE_DESC);
                return true;
            case R.id.menu_sort_translatedtitle_asc:
                setSortOrder(R.id.menu_sort_translatedtitle_asc, Movies.SORT_ORDER_TRANSLATED_TITLE_ASC);
                return true;
            case R.id.menu_sort_translatedtitle_desc:
                setSortOrder(R.id.menu_sort_translatedtitle_desc, Movies.SORT_ORDER_TRANSLATED_TITLE_DESC);
                return true;
            case R.id.menu_sort_datewatched_asc:
                setSortOrder(R.id.menu_sort_datewatched_asc, Movies.SORT_ORDER_DATE_WATCHED_ASC);
                return true;
            case R.id.menu_sort_datewatched_desc:
                setSortOrder(R.id.menu_sort_datewatched_desc, Movies.SORT_ORDER_DATE_WATCHED_DESC);
                return true;
            case R.id.menu_sort_userrating_asc:
                setSortOrder(R.id.menu_sort_userrating_asc, Movies.SORT_ORDER_USER_RATING_ASC);
                return true;
            case R.id.menu_sort_userrating_desc:
                setSortOrder(R.id.menu_sort_userrating_desc, Movies.SORT_ORDER_USER_RATING_DESC);
                return true;
            case R.id.menu_sort_certification_asc:
                setSortOrder(R.id.menu_sort_certification_asc, Movies.SORT_ORDER_CERTIFICATION_ASC);
                return true;
            case R.id.menu_sort_certification_desc:
                setSortOrder(R.id.menu_sort_certification_desc, Movies.SORT_ORDER_CERTIFICATION_DESC);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * Click on filter list header
     */
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.filter_info_layout:
                // Click on filters info
                removeFilters(v);
                break;
//            case R.id.search_info_layout:
            // Click on search info
//                this.refreshList();
//                break;
//            case R.id.showing_info_layout:
            // Click on now showing info
//                this.refreshList();
//                break;
        }
    }

    /**
     * Called when the activity is resumed.
     */
    @Override
    public void onResume() {
        super.onResume();

        // Restart if adapter is not available
        if (mMoviesAdapter == null && getListView() != null)
            SharedObjects.getInstance().restartAppRequested = true;

        if (SharedObjects.getInstance().preferences == null)
            SharedObjects.getInstance().restartAppRequested = true;

        if (SharedObjects.getInstance().restartAppRequested) {
            if (S.INFO)
                Log.i(S.TAG, "App restarting...");

            SharedObjects.getInstance().restartAppRequested = false;
            SharedObjects.getInstance().moviesListActivityRefreshRequested = false;

            Intent importIntent = new Intent(getActivity(), ImportActivity.class);
            getActivity().finish();
            startActivity(importIntent);

        } else if (SharedObjects.getInstance().moviesListActivityRefreshRequested) {
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
        if (mMoviesAdapter != null && mMoviesAdapter.getCursor() != null) {
            mMoviesAdapter.stopImageLoader();
            mMoviesAdapter.getCursor().close();
        }

        try {
            getListView().setAdapter(null);
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
    public void setActivateOnItemClick(boolean activateOnItemClick) {
        // When setting CHOICE_MODE_SINGLE, ListView will automatically
        // give items the 'activated' state when touched.
        getListView().setChoiceMode(activateOnItemClick
                ? ListView.CHOICE_MODE_SINGLE
                : ListView.CHOICE_MODE_NONE);
    }

    private void setActivatedPosition(int position) {
        if (position == ListView.INVALID_POSITION) {
            getListView().setItemChecked(mActivatedPosition, false);
        } else {
            getListView().setItemChecked(position, true);
        }

        mActivatedPosition = position;
    }

    /**
     * Updates list header about number of movies displayed
     */
    private void updateHeaderInfo() {
        mMoviesAdapter.getCount();
        mHeaderListCountView.setText(String.format(getResources().getQuantityString(R.plurals.list_showing,
                mMoviesAdapter.getCount()), mMoviesAdapter.getCount()));
    }

    /**
     * Function to refresh main list header view
     */
    private void updateHeaderFilterInfo() {
//        this.filterHeaderLabelText.setText(getResources().getQuantityString(R.plurals.filter_label, this.filters.getCount()));
        mFilterHeaderText.setText(mFilters.getFiltersHumanInfo());
//        this.filterHeaderActionText.setText(this.filters.getCount() > 0 ? getString(R.string.filter_action) : null);
    }

    /**
     * Set default order to preferences and sort list
     */
    protected void setSortOrder(int menuId, String order) {
        if (mMoviesDataProvider != null && getListView() != null) {
            SharedPreferences.Editor editor = SharedObjects.getInstance().preferences.edit();
            editor.putInt("settingMovieListOrderId", menuId);
            editor.putString("settingMovieListOrder", order);
            editor.apply();
            mMenu.findItem(menuId).setChecked(true);
            refreshList();
        }
    }

    /**
     * Refresh list view
     */
    protected void refreshList() {
        if (mMoviesDataProvider != null && getListView() != null) {
            // Swap cursor
            mSearchMenuItem.collapseActionView();
            mMoviesAdapter.unregisterDataSetObserver(mCursorAdapterObserver);
            mMoviesAdapter.stopImageLoader();
            mMoviesAdapter.loadConfiguration(getActivity().getBaseContext());
            mMoviesAdapter.changeCursor(mMoviesDataProvider.fetchMovies(S.CONTENT_URI));
            mMoviesAdapter.registerDataSetObserver(mCursorAdapterObserver);
            mMoviesAdapter.notifyDataSetChanged();
        }
    }

    /**
     * Display first movie in details pager to if two pane display
     */
    private void resetDetailPager() {
        if (SharedObjects.getInstance().twoPane && mMoviesAdapter.getCount() > 0) {
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
        builder.setItems(mFilters.getAvailableFilters(), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                // Shortcut for boolean type
                if (Movies.availableFilterFields[item].type == Movies.FILTER_TYPE_BOOLEAN) {
                    addFilterThirdStep(Movies.availableFilterFields[item], Movies.FILTER_OPERATOR_EQUALS);
                } else {
                    addFilterSecondStep(Movies.availableFilterFields[item]);
                }
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

    /**
     * Function to add new filter from wizard - 2nd step
     */
    private void addFilterSecondStep(final FilterField field) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(mFilters.getFilterFieldHumanName(field.resId));
        builder.setItems(mFilters.getAvailableOperators(field), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                addFilterThirdStep(field, field.type.operators[item]);
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
            new DatePickerDialog(getActivity(), new FilterDatePickerListener(field, operator),
                    mDpYear, mDpMonth, mDpDay).show();
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(mFilters.getFilterFieldHumanName(field.resId) + " " + getString(operator.resId));

        if (field.type == Movies.FILTER_TYPE_BOOLEAN) {
            // If filter type boolean show list yes/no
            final String[] booleanValues = new String[]{getString(R.string.details_boolean_true),
                    getString(R.string.details_boolean_false)};
            builder.setItems(booleanValues, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int item) {
                    mFilters.addFilter(new Filter(field, operator, booleanValues[item], getString(R.string.details_boolean_true)));
                    refreshList();
                }
            });
        } else {
            // Prepare view with text input
            View filterView = getActivity().getLayoutInflater().inflate(R.layout.filter_input, null);
            final EditText filterValueView = (EditText) filterView.findViewById(R.id.filterValue);
            if (field.type == Movies.FILTER_TYPE_NUMBER) {
                DigitsKeyListener digitsKeyListener = new DigitsKeyListener(false, true);
                filterValueView.setKeyListener(digitsKeyListener);
            }
            builder.setView(filterView);

            // Prepare positive button
            builder.setPositiveButton(getString(R.string.dialog_positive), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int button) {
                    String filterValue = filterValueView.getText().toString();
                    mFilters.addFilter(new Filter(field, operator, filterValue));
                    refreshList();
                }
            });
        }

        // Negative button is for both options
        builder.setNegativeButton(getString(R.string.dialog_negative), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int whichButton) {
            }
        });

        AlertDialog alertDialog = builder.create();

        alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                if (field.type == Movies.FILTER_TYPE_TEXT || field.type == Movies.FILTER_TYPE_NUMBER) {
                    InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(getActivity().INPUT_METHOD_SERVICE);
                    imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
                }
            }
        });
        alertDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                if (field.type == Movies.FILTER_TYPE_TEXT || field.type == Movies.FILTER_TYPE_NUMBER) {
                    InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(getActivity().INPUT_METHOD_SERVICE);
                    imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
                }
            }
        });
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
    private void removeFilters(View v) {
        if (mFilters.getCount() == 0) {
            // if there is no filter, revert color and do nothing
//            v.setBackgroundResource(R.color.color_list_header_background);
        } else if (mFilters.getCount() == 1) {
            // if there is one filter, remove and refresh
            mFilters.removeAllFilters();
            refreshList();
//            v.setBackgroundResource(R.color.color_list_header_background);
        } else {
            // if there is more filter, let user decide
//            v.setBackgroundResource(R.color.color_list_header_background);

            // prepare array of all filters
            ArrayList<String> filtersList = new ArrayList<String>();
            filtersList.add(getString(R.string.filter_remove_all));
            filtersList.addAll(mFilters.getFiltersHumanInfoList());

            // build alert dialog
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle(getString(R.string.filter_remove));
            builder.setItems(filtersList.toArray(new String[filtersList.size()]), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int item) {
                    if (item == 0) {
                        mFilters.removeAllFilters();
                    } else {
                        mFilters.removeFilter(item - 1);
                    }
                    refreshList();
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

}
