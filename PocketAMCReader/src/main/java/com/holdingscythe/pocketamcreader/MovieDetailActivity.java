package com.holdingscythe.pocketamcreader;

import android.app.ActionBar;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.NavUtils;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.MenuItem;

import com.holdingscythe.pocketamcreader.catalog.Movie;
import com.holdingscythe.pocketamcreader.catalog.MoviesDataProvider;

// TODO: cleanup

/**
 * An activity representing a single Movie detail screen. This
 * activity is only used on handset devices. On tablet-size devices,
 * item details are presented side-by-side with a list of items
 * in a {@link MovieListActivity}.
 * <p/>
 * This activity is mostly just a 'shell' activity containing nothing
 * more than a {@link MovieDetailFragment}.
 */
public class MovieDetailActivity extends FragmentActivity {
//    private final Handler handler = new Handler();
//    private PagerSlidingTabStrip mTabs;
//    private Drawable mOldBackground = null;
//    private int mCurrentColor = 0xFFcaec60;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // savedInstanceState is non-null when there is fragment state
        // saved from previous configurations of this activity
        // (e.g. when rotating the screen from portrait to landscape).
        // In this case, the fragment will automatically be re-added
        // to its container so we don't need to manually add it.
        // For more information, see the Fragments API guide at:
        //
        // http://developer.android.com/guide/components/fragments.html
        //
        setContentView(R.layout.activity_movie_detail);

        if (savedInstanceState == null) {
            // Create the detail fragment and add it to the activity
            // using a fragment transaction.
            Bundle arguments = new Bundle();
            arguments.putString(MovieDetailFragment.ARG_MOVIE_ID,
                    getIntent().getStringExtra(MovieDetailFragment.ARG_MOVIE_ID));
            MovieDetailFragmentHost fragment = new MovieDetailFragmentHost();
            fragment.setArguments(arguments);
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.movie_detail_container, fragment)
                    .commit();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            // This ID represents the Home or Up button. In the case of this
            // activity, the Up button is shown. Use NavUtils to allow users
            // to navigate up one level in the application structure. For
            // more details, see the Navigation pattern on Android Design:
            //
            // http://developer.android.com/design/patterns/navigation.html#up-vs-back
            //
            NavUtils.navigateUpTo(this, new Intent(this, MovieListActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

//    private Movie LoadMovie() {
//        if (mMoviesDataProvider == null) {
//            mMoviesDataProvider = new MoviesDataProvider(this);
//        }
//
//        // Benchmark start
//        long startTime;
//        long endTime;
//
//        if (S.INFO) {
//            startTime = System.currentTimeMillis();
//        }
//
//        // Fetch movie
//        Uri uri = Uri.withAppendedPath(S.CONTENT_URI, "_id/" + getIntent().getStringExtra
//                (MovieDetailActivity.ARG_MOVIE_ID));
//        Cursor cursor = mMoviesDataProvider.fetchMovie(uri);
//        cursor.moveToFirst();
//        Movie movie = new Movie(cursor);
//
//        // Benchmark end
//        if (S.INFO) {
//            endTime = System.currentTimeMillis();
//            Log.i(S.TAG, "Details display elapsed time: " + (endTime - startTime) + " ms.");
//        }
//
//        return movie;
//    }

//    private void changeTabsColor(int newColor) {
//        mTabs.setIndicatorColor(newColor);
//
//        // change ActionBar color just if an ActionBar is available
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
//            Drawable colorDrawable = new ColorDrawable(newColor);
//            Drawable bottomDrawable = getResources().getDrawable(R.drawable.actionbar_bottom);
//            LayerDrawable ld = new LayerDrawable(new Drawable[]{colorDrawable, bottomDrawable});
//
//            if (mOldBackground == null) {
//                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1) {
//                    ld.setCallback(drawableCallback);
//                } else {
//                    getActionBar().setBackgroundDrawable(ld);
//                }
//            } else {
//                // TODO Do I need transition drawable?
//                TransitionDrawable td = new TransitionDrawable(new Drawable[]{mOldBackground, ld});
//                // workaround for broken ActionBarContainer drawable handling on pre-API 17 builds
//                // https://github.com/android/platform_frameworks_base/commit/a7cc06d82e45918c37429a59b14545c6a57db4e4
//                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1) {
//                    td.setCallback(drawableCallback);
//                } else {
//                    getActionBar().setBackgroundDrawable(td);
//                }
//                td.startTransition(200);
//            }
//
//            mOldBackground = ld;
//
//            // http://stackoverflow.com/questions/11002691/actionbar-setbackgrounddrawable-nulling-background-from-thread-handler
//            getActionBar().setDisplayShowTitleEnabled(false);
//            getActionBar().setDisplayShowTitleEnabled(true);
//        }
//
//        mCurrentColor = newColor;
//    }
//
//    private Drawable.Callback drawableCallback = new Drawable.Callback() {
//        @Override
//        public void invalidateDrawable(Drawable who) {
//            getActionBar().setBackgroundDrawable(who);
//        }
//
//        @Override
//        public void scheduleDrawable(Drawable who, Runnable what, long when) {
//            handler.postAtTime(what, when);
//        }
//
//        @Override
//        public void unscheduleDrawable(Drawable who, Runnable what) {
//            handler.removeCallbacks(what);
//        }
//    };
}
