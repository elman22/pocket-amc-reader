package com.holdingscythe.pocketamcreader.utils;

import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;

import com.holdingscythe.pocketamcreader.MovieListFragment;
import com.holdingscythe.pocketamcreader.catalog.Movies;
import com.holdingscythe.pocketamcreader.catalog.MoviesAdapter;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;

public class SharedObjects {
    // Singleton instance
    private static SharedObjects instance = null;

    // Static reference to opened database
    public SQLiteDatabase db;
    public SQLiteStatement dbInsertStatement;
    public SQLiteStatement dbInsertCustomStatement;
    public SQLiteStatement dbInsertExtraStatement;
    public Integer dbReferences = 0;

    // Global variables
    public String[] moviesProjection = Movies.MOVIES_PROJECTION;
    public SharedPreferences preferences;
    public SimpleDateFormat dateAddedFormat = new SimpleDateFormat("yyyy-MM-dd", new Locale("US"));
    public DateFormat dateFormat = DateFormat.getDateInstance();

    // References between intents
    public boolean twoPane = false;
    public MoviesAdapter listMovieAdapter;
    public MovieListFragment movieListFragment;
    public boolean moviesListActivityRefreshRequested = false;
    public boolean restartAppRequested = false;

    protected SharedObjects() {
        // Exists only to defeat instantiation.
    }

    public static SharedObjects getInstance() {
        if (instance == null) {
            instance = new SharedObjects();
        }
        return instance;
    }

}
