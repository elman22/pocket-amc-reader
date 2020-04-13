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

package com.holdingscythe.pocketamcreader;

import android.content.ContentResolver;
import android.net.Uri;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Convenience definitions
 */
public final class S {
    public static final int LOGLEVEL = BuildConfig.LOGLEVEL;

    public static final boolean STRICT = LOGLEVEL == 1;
    public static final boolean VERBOSE = LOGLEVEL <= 2;
    public static final boolean DEBUG = LOGLEVEL <= 3;
    public static final boolean INFO = LOGLEVEL <= 4;
    public static final boolean WARN = LOGLEVEL <= 5;
    public static final boolean ERROR = LOGLEVEL <= 6;

    public static final String AUTHORITY = "com.holdingscythe.provider.MovieCatalog";
    public static final String TAG = "PocketAMCReader";
    public static final String WAKE_LOCK_TAG = TAG + ":WakeLock";
    public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/movies");
    public static final String DEFAULT_FONT = "fonts/RobotoCondensed-Regular.ttf";

    // MIME types used for searching movies or looking up a single movie
    public static final String MOVIES_MIME_TYPE =
            ContentResolver.CURSOR_DIR_BASE_TYPE + "/vnd.com.holdingscythe.movies";
    public static final String MOVIE_MIME_TYPE =
            ContentResolver.CURSOR_ITEM_BASE_TYPE + "/vnd.com.holdingscythe.movie";

    // Import
    static final String CATALOG_TMP_FILENAME = "/amc_import_tmp.xml";
    static final int IMPORT_CONVERSION_START = -1;
    static final int IMPORT_LOADING_START = -2;
    static final int IMPORT_INDEXING_START = -3;
    static final int IMPORT_ERROR_CONVERSION = -10;
    static final int IMPORT_ERROR_LOADING = -11;
    public static final String CATALOG_TRUE = "True";
    public static final String CATALOG_FALSE = "False";

    // Intent prefixes
    public static final String INTENT_SCHEME_HTTP = "http://";
    public static final String INTENT_SCHEME_HTTPS = "https://";
    public static final String INTENT_SCHEME_FILE = "file://";
    public static final String INTENT_SCHEME_CONTENT = "content://";
    public static final String INTENT_SCHEME_PORTION = "://";
    public static final String MIME_VIDEO = "video/*";

    // Themes
    public static final String THEME_BLUE = "theme_blue";
    public static final String THEME_RED = "theme_red";
    public static final String THEME_INDIGO = "theme_indigo";
    public static final String THEME_GREEN = "theme_green";
    public static final String THEME_ORANGE = "theme_orange";
    public static final String THEME_GREY = "theme_grey";
    public static final String THEME_BROWN = "theme_brown";
    public static final String THEME_PINK = "theme_pink";
    public static final String THEME_PURPLE = "theme_purple";
    public static final String THEME_BLACK = "theme_black";

    // SettingsActivity requesting import
    public static final Map<String, Integer> SETTINGS_REQUESTING_IMPORT;

    static {
        Map<String, Integer> tmpMap = new HashMap<>();
        tmpMap.put("settingCatalogLocation", 1);
        tmpMap.put("settingCatalogEncoding", 1);
        tmpMap.put("settingRemoveBadChars", 1);
        SETTINGS_REQUESTING_IMPORT = Collections.unmodifiableMap(tmpMap);
    }

    // SettingsActivity requesting restart
    public static final Map<String, Integer> SETTINGS_REQUESTING_RESTART;

    static {
        Map<String, Integer> tmpMap = new HashMap<>();
        tmpMap.put("settingShowThumbs", 1);
        tmpMap.put("settingShowGridTitle", 1);
        tmpMap.put("settingTheme", 1);
        SETTINGS_REQUESTING_RESTART = Collections.unmodifiableMap(tmpMap);
    }

    // SettingsActivity requesting list refresh
    public static final Map<String, Integer> SETTINGS_REQUESTING_REFRESH;

    static {
        Map<String, Integer> tmpMap = new HashMap<>();
        tmpMap.put("settingMoviesListLine1", 1);
        tmpMap.put("settingMoviesListLine2", 1);
        tmpMap.put("settingMoviesListLine3", 1);
        tmpMap.put("settingListForceSortField", 1);
        tmpMap.put("settingMoviesListSeparator", 1);
        SETTINGS_REQUESTING_REFRESH = Collections.unmodifiableMap(tmpMap);
    }

    // Color tags
    public static final Map<String, Integer> COLOR_TAGS;

    static {
        Map<String, Integer> tmpMap = new HashMap<>();
        tmpMap.put("0", android.R.color.transparent);
        tmpMap.put("1", R.color.color_tag_1);
        tmpMap.put("2", R.color.color_tag_2);
        tmpMap.put("3", R.color.color_tag_3);
        tmpMap.put("4", R.color.color_tag_4);
        tmpMap.put("5", R.color.color_tag_5);
        tmpMap.put("6", R.color.color_tag_6);
        tmpMap.put("7", R.color.color_tag_7);
        tmpMap.put("8", R.color.color_tag_8);
        tmpMap.put("9", R.color.color_tag_9);
        tmpMap.put("10", R.color.color_tag_10);
        tmpMap.put("11", R.color.color_tag_11);
        tmpMap.put("12", R.color.color_tag_12);
        COLOR_TAGS = Collections.unmodifiableMap(tmpMap);
    }

}
