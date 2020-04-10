/*
    This file is part of Pocket AMC Reader.
    Copyright © 2010-2020 Elman <holdingscythe@zoznam.sk>

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
    public int deviceWidthPixels = 0;
    public int actionBarHeight = 0;
    public int heroImageHeight = 0;

    // References between intents
    public boolean twoPane = false;
    public MoviesAdapter recyclerMovieAdapter;
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
