package com.holdingscythe.pocketamcreader;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.holdingscythe.pocketamcreader.catalog.MoviesAdapter;
import com.holdingscythe.pocketamcreader.utils.SharedObjects;

// todo: cleanup

/**
 * This fragment hosts the viewpager that will use a FragmentPagerAdapter to display child fragments.
 * Created by Elman on 2. 11. 2014.
 */
public class MovieDetailFragmentHost extends Fragment {

    /**
     * The fragment argument representing the list ID that this fragment represents.
     */
//    public static final String ARG_LIST_ID = "list_id";
//    public static final String TAG = MovieDetailFragmentHost.class.getName();
//    public static MovieDetailFragmentHost newInstance() {
//        return new MovieDetailFragmentHost();
//    }

    public static class MovieDetailAdapter extends FragmentPagerAdapter {
        private MoviesAdapter mMoviesAdapter;
        private ListView mListView;

        public MovieDetailAdapter(FragmentManager fm) {
            super(fm);
            mMoviesAdapter = SharedObjects.getInstance().listMovieAdapter;
            mListView = SharedObjects.getInstance().moviesListView;
        }

        @Override
        public int getCount() {
            return mMoviesAdapter.getCount();
        }

        @Override
        public Fragment getItem(int position) {
            mMoviesAdapter.getCursor().move(position);
            mListView.setSelection(position);
            mListView.setSelected(true);
            mListView.smoothScrollToPosition(position);

            Bundle args = new Bundle();
            args.putString(MovieDetailFragment.ARG_MOVIE_ID, String.valueOf(mMoviesAdapter.getItemId(position)));
            return MovieDetailFragment.newInstance(args);
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_movie_detail_viewpager, container, false);

        ViewPager viewPager = (ViewPager) root.findViewById(R.id.movieDetailViewPager);
        // Important: Must use the child FragmentManager or you will see side effects.
        viewPager.setAdapter(new MovieDetailAdapter(getChildFragmentManager()));

        // Set current item based on clicked item
        if (getArguments().containsKey(MovieDetailFragment.ARG_MOVIE_ID)) {
            viewPager.setCurrentItem(SharedObjects.getInstance().listMovieAdapter.getCursor().getPosition());
        }

        return root;
    }

    /**
     * Called when the activity is resumed.
     */
    @Override
    public void onResume() {
        super.onResume();
        if (SharedObjects.getInstance().listMovieAdapter == null)
            getActivity().finish();
    }

}
