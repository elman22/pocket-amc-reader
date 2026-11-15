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

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.holdingscythe.pocketamcreader.catalog.MoviesAdapter;
import com.holdingscythe.pocketamcreader.utils.SharedObjects;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

/**
 * This fragment hosts the pager which uses a FragmentStateAdapter to display the details of the movies as child
 * fragments (only the fragments near the visible one are kept).
 * Created by Elman on 2. 11. 2014.
 */
public class MovieDetailFragmentHost extends Fragment {

    public static class MovieDetailAdapter extends FragmentStateAdapter {
        private final MoviesAdapter mMoviesAdapter;

        MovieDetailAdapter(@NonNull Fragment host, @NonNull MoviesAdapter moviesAdapter) {
            super(host);
            mMoviesAdapter = moviesAdapter;
        }

        @Override
        public int getItemCount() {
            return mMoviesAdapter.getItemCount();
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            Bundle args = new Bundle();
            args.putString(MovieDetailFragment.ARG_MOVIE_ID, String.valueOf(mMoviesAdapter.getItemId(position)));
            return MovieDetailFragment.newInstance(args);
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_movie_detail_viewpager, container, false);
        ViewPager2 viewPager = root.findViewById(R.id.movieDetailViewPager);

        // The list is needed to follow the movie and to read the movies (it is gone e.g. after the process died)
        MoviesAdapter moviesAdapter = SharedObjects.getInstance().recyclerMovieAdapter;
        MovieListFragment listFragment = SharedObjects.getInstance().movieListFragment;
        View listView = listFragment == null ? null : listFragment.getView();
        final RecyclerView recyclerView = listView == null ? null : listView.findViewById(R.id.movie_list_recycler);
        if (moviesAdapter == null || recyclerView == null) {
            if (getActivity() != null)
                getActivity().finish();
            return root;
        }

        // Important: Must use this fragment (its child FragmentManager) or you will see side effects.
        viewPager.setAdapter(new MovieDetailAdapter(this, moviesAdapter));

        // Show the movie which is displayed in the pager also in the list. This is not done when the fragment of the
        // page is created: the neighbors are created in advance, but they are not displayed.
        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                recyclerView.setSelected(true);
                recyclerView.smoothScrollToPosition(position);
            }
        });

        // Set current item based on clicked item. The pager restores the item displayed before a screen rotation
        // after this (not clicked item again).
        if (getArguments() != null && getArguments().containsKey(MovieDetailFragment.ARG_MOVIE_ID)) {
            String argumentMovieId = getArguments().getString(MovieDetailFragment.ARG_MOVIE_ID);
            if (argumentMovieId != null && !argumentMovieId.isEmpty()) {
                viewPager.setCurrentItem(Integer.parseInt(argumentMovieId), false);
            }
        }

        return root;
    }

    /**
     * Called when the activity is resumed.
     */
    @Override
    public void onResume() {
        super.onResume();

        if (SharedObjects.getInstance().recyclerMovieAdapter == null && getActivity() != null)
            getActivity().finish();
    }
}
