package com.holdingscythe.pocketamcreader;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.holdingscythe.pocketamcreader.catalog.Movie;
import com.holdingscythe.pocketamcreader.catalog.MoviesDataProvider;

/**
 * PocketAMCReader
 * Created by Elman on 21.10.2014.
 *
 * A fragment representing a single Movie detail screen.
 * This fragment is either contained in a {@link MovieListActivity}
 * in two-pane mode (on tablets) or a {@link MovieDetailActivity}
 * on handsets.
 */

public class MovieDetailBasicFragment extends Fragment {
    private Movie mMovie;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public MovieDetailBasicFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

//    @Override
//    public void onActivityCreated(Bundle savedInstanceState) {
//        super.onActivityCreated(savedInstanceState);
//
//        // prepare Data Provider
//        mMoviesDataProvider = new MoviesDataProvider(getActivity());
//
//        if (getArguments().containsKey(ARG_MOVIE_ID)) {
//            loadMovie(getArguments().getString(ARG_MOVIE_ID, "0"));
//        }
//    }
//
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_movie_detail, container, false);

//        rootView.findViewById(R.id.detail_image_teaser).set;

        return rootView;
    }

////    detailIntent.setData(data);
//
//    private void loadMovie(String id) {
////        // Benchmark start
////        long startTime;
////        long endTime;
////
////        if (S.INFO) {
////            startTime = System.currentTimeMillis();
////        }
////
////        // Fetch movie
////        Uri uri = Uri.withAppendedPath(S.CONTENT_URI, "_id/" + id);
////        mMovie = new Movie(this.mMoviesDataProvider.fetchMovie(uri));
////
//////        String catalogPicture = mCursor.getString(mCursor.getColumnIndex(Movies.PICTURE));
//////        ImageView mPictureThumb = (ImageView) this.vt.findCachedViewById(R.id.detail_Picture_thumb);
//////        ImageView mPictureTeaser = (ImageView) rootView.f findViewById()
////
////        boolean useStub = true;
////
////
////        // Benchmark end
////        if (S.INFO) {
////            endTime = System.currentTimeMillis();
////            Log.i(S.TAG, "Details display elapsed time: " + (endTime - startTime) + " ms.");
////        }
//    }
//
//    protected void showBasic() {
//
//    }

}
