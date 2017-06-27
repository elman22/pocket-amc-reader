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

package com.holdingscythe.pocketamcreader;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.holdingscythe.pocketamcreader.catalog.MoviesAdapter;
import com.holdingscythe.pocketamcreader.utils.SharedObjects;

/**
 * This fragment hosts the viewpager that will use a FragmentPagerAdapter to display child fragments.
 * Created by Elman on 2. 11. 2014.
 */
public class MovieDetailFragmentHost extends Fragment {

    public static class MovieDetailAdapter extends FragmentPagerAdapter {
        private MoviesAdapter mMoviesAdapter;
        private ListView mListView;

        public MovieDetailAdapter(FragmentManager fm) throws NullPointerException {
            super(fm);
            mMoviesAdapter = SharedObjects.getInstance().listMovieAdapter;
            mListView = (ListView) SharedObjects.getInstance().movieListFragment.getView().findViewById(R.id
                    .movie_list);

            if (mMoviesAdapter == null || mListView == null) {
                throw new NullPointerException();
            }

        }

        @Override
        public int getCount() {
            try {
                return mMoviesAdapter.getCount();
            } catch (NullPointerException e) {
                if (S.DEBUG)
                    Log.i(S.TAG, "MovieDetailAdapter is null");

                return 0;
            }
        }

        @Override
        public Fragment getItem(final int position) {
//            mListView.setSelection(position);
//            mListView.setSelected(true);
            mListView.post(new Runnable() {
                @Override
                public void run() {
                    mListView.smoothScrollToPosition(position);
                }
            });

            Bundle args = new Bundle();
            args.putString(MovieDetailFragment.ARG_MOVIE_ID, String.valueOf(mMoviesAdapter.getItemId(position)));
            return MovieDetailFragment.newInstance(args);
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_movie_detail_viewpager, container, false);

        ViewPager viewPager = (ViewPager) root.findViewById(R.id.movieDetailViewPager);
        try {
            // Important: Must use the child FragmentManager or you will see side effects.
            viewPager.setAdapter(new MovieDetailAdapter(getChildFragmentManager()));

            // Set current item based on clicked item
            if (getArguments().containsKey(MovieDetailFragment.ARG_MOVIE_ID)) {
                viewPager.setCurrentItem(SharedObjects.getInstance().listMovieAdapter.getCursor().getPosition());
            }

        } catch (NullPointerException e) {
            getActivity().finish();
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
