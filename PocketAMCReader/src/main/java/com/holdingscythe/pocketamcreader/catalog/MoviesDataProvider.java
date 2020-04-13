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

package com.holdingscythe.pocketamcreader.catalog;

import android.app.SearchManager;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.provider.BaseColumns;
import android.text.TextUtils;
import android.util.Log;
import android.widget.FilterQueryProvider;

import com.holdingscythe.pocketamcreader.S;
import com.holdingscythe.pocketamcreader.filters.Filters;
import com.holdingscythe.pocketamcreader.settings.SettingsConstants;
import com.holdingscythe.pocketamcreader.utils.SharedObjects;
import com.holdingscythe.pocketamcreader.utils.Utils;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import androidx.annotation.NonNull;

public class MoviesDataProvider extends ContentProvider implements FilterQueryProvider {
    private static final String DATABASE_NAME = "PocketAMCReader.db";
    private static final int DATABASE_VERSION = 9;

    private static final String TABLE_NAME = "Movies";
    private static final String TABLE_NAME_CUSTOM = "CustomFields";
    private static final String TABLE_NAME_EXTRAS = "Extras";
    private static final String INSERT = "INSERT INTO " +
            TABLE_NAME +
            " (" + Movies.NUMBER + ", " + Movies.CHECKED + ", " + Movies.FORMATTED_TITLE + ", " +
            "" + Movies.MEDIA_LABEL + ", " + Movies.MEDIA_TYPE + ", " + Movies.SOURCE + ", " + Movies.DATE + "," +
            "" + Movies.BORROWER + ", " + Movies.RATING + ", " + Movies.ORIGINAL_TITLE + "," +
            "" + Movies.TRANSLATED_TITLE + ", " + Movies.DIRECTOR + ", " + Movies.PRODUCER + ", " +
            "" + Movies.COUNTRY + ", " + Movies.CATEGORY + ", " + Movies.YEAR + ", " + Movies.LENGTH + ", " +
            "" + Movies.ACTORS + ", " + Movies.URL + ", " + Movies.DESCRIPTION + "," + " " + Movies.COMMENTS + ", " +
            "" + Movies.VIDEO_FORMAT + ", " + Movies.VIDEO_BITRATE + ", " + Movies.AUDIO_FORMAT + ", " +
            "" + Movies.AUDIO_BITRATE + ", " + Movies.RESOLUTION + ", " + Movies.FRAMERATE + ", " +
            "" + Movies.LANGUAGES + "," + Movies.SUBTITLES + ", " + Movies.SIZE + ", " + Movies.DISKS + ", " +
            "" + Movies.PICTURE + ", " + Movies.COLOR_TAG + ", " + Movies.DATE_WATCHED + ", " +
            "" + Movies.USER_RATING + ", " + Movies.WRITER + ", " + Movies.COMPOSER + ", " +
            "" + Movies.CERTIFICATION + "," + Movies.FILE_PATH + ") " +
            "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
    private static final String INSERT_CUSTOM = "INSERT INTO " + TABLE_NAME_CUSTOM +
            " (" + Movies.MOVIES_ID + ", " + Movies.C_TYPE + ", " + Movies.C_NAME + ", " + Movies.C_VALUE + ")" +
            " VALUES (?,?,?,?)";
    private static final String INSERT_EXTRA = "INSERT INTO " + TABLE_NAME_EXTRAS +
            " (" + Movies.MOVIES_ID + ", " + Movies.E_CHECKED + ", " + Movies.E_TAG + ", " + Movies.E_TITLE + ", " +
            "" + Movies.E_CATEGORY + ", " + Movies.E_URL + ", " + Movies.E_DESCRIPTION + ", " +
            "" + Movies.E_COMMENTS + "," + Movies.E_CREATED_BY + ", " + Movies.E_PICTURE + ")" +
            " VALUES (?,?,?,?,?,?,?,?,?,?)";

    private SQLiteDatabase db;
    private SQLiteStatement insertStatement;
    private SQLiteStatement insertCustomStatement;
    private SQLiteStatement insertExtraStatement;
    private MoviesOpenHelper openHelper;
    private SharedPreferences preferences;
    private Filters filters;

    // UriMatcher stuff
    private static final int GET_MOVIES = 0;
    private static final int SEARCH_MOVIES = 1;
    private static final int GET_MOVIE = 2;
    private static final int GET_MOVIE_EXTRAS = 3;
    private static final int GET_MOVIE_CUSTOM_FIELDS = 4;
    private static final UriMatcher sURIMatcher = buildUriMatcher();

    private static class MoviesOpenHelper extends SQLiteOpenHelper {

        MoviesOpenHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL("CREATE TABLE [" + TABLE_NAME + "] ( " +
                    "[_id] integer PRIMARY KEY NOT NULL, " +
                    "[" + Movies.NUMBER + "] integer NOT NULL, " +
                    "[" + Movies.CHECKED + "] boolean, " +
                    "[" + Movies.FORMATTED_TITLE + "] varchar(512) COLLATE NOCASE, " +
                    "[" + Movies.MEDIA_LABEL + "] varchar(128) COLLATE NOCASE, " +
                    "[" + Movies.MEDIA_TYPE + "] varchar(64) COLLATE NOCASE, " +
                    "[" + Movies.SOURCE + "] varchar(64) COLLATE NOCASE, " +
                    "[" + Movies.DATE + "] date, " +
                    "[" + Movies.BORROWER + "] varchar(128) COLLATE NOCASE, " +
                    "[" + Movies.RATING + "] float, " +
                    "[" + Movies.ORIGINAL_TITLE + "] varchar(256) COLLATE NOCASE, " +
                    "[" + Movies.TRANSLATED_TITLE + "] varchar(256) COLLATE NOCASE, " +
                    "[" + Movies.DIRECTOR + "] varchar(256) COLLATE NOCASE, " +
                    "[" + Movies.PRODUCER + "] varchar(512) COLLATE NOCASE, " +
                    "[" + Movies.COUNTRY + "] varchar(256) COLLATE NOCASE, " +
                    "[" + Movies.CATEGORY + "] varchar(256) COLLATE NOCASE, " +
                    "[" + Movies.YEAR + "] integer, " +
                    "[" + Movies.LENGTH + "] integer, " +
                    "[" + Movies.ACTORS + "] text COLLATE NOCASE, " +
                    "[" + Movies.URL + "] varchar(256), " +
                    "[" + Movies.DESCRIPTION + "] text, " +
                    "[" + Movies.COMMENTS + "] text, " +
                    "[" + Movies.VIDEO_FORMAT + "] varchar(64) COLLATE NOCASE, " +
                    "[" + Movies.VIDEO_BITRATE + "] integer, " +
                    "[" + Movies.AUDIO_FORMAT + "] varchar(64) COLLATE NOCASE, " +
                    "[" + Movies.AUDIO_BITRATE + "] integer, " +
                    "[" + Movies.RESOLUTION + "] varchar(32) COLLATE NOCASE, " +
                    "[" + Movies.FRAMERATE + "] float, " +
                    "[" + Movies.LANGUAGES + "] varchar(256) COLLATE NOCASE, " +
                    "[" + Movies.SUBTITLES + "] varchar(256) COLLATE NOCASE, " +
                    "[" + Movies.SIZE + "] varchar(16), " +
                    "[" + Movies.DISKS + "] integer, " +
                    "[" + Movies.PICTURE + "] varchar(512), " +
                    "[" + Movies.COLOR_TAG + "] integer, " +
                    "[" + Movies.DATE_WATCHED + "] date, " +
                    "[" + Movies.USER_RATING + "] float, " +
                    "[" + Movies.WRITER + "] varchar(256) COLLATE NOCASE, " +
                    "[" + Movies.COMPOSER + "] varchar(256) COLLATE NOCASE, " +
                    "[" + Movies.CERTIFICATION + "] varchar(32) COLLATE NOCASE, " +
                    "[" + Movies.FILE_PATH + "] varchar(256) COLLATE NOCASE " +
                    ")");

            db.execSQL("CREATE TABLE [" + TABLE_NAME_CUSTOM + "] ( " +
                    "[_id] integer PRIMARY KEY NOT NULL, " +
                    "[" + Movies.MOVIES_ID + "] integer NOT NULL, " +
                    "[" + Movies.C_TYPE + "] varchar(16) COLLATE NOCASE, " +
                    "[" + Movies.C_NAME + "] varchar(256) COLLATE NOCASE, " +
                    "[" + Movies.C_VALUE + "] text " +
                    ")");

            db.execSQL("CREATE TABLE [" + TABLE_NAME_EXTRAS + "] ( " +
                    "[_id] integer PRIMARY KEY NOT NULL, " +
                    "[" + Movies.MOVIES_ID + "] integer NOT NULL, " +
                    "[" + Movies.E_CHECKED + "] boolean, " +
                    "[" + Movies.E_TAG + "] varchar(32) COLLATE NOCASE, " +
                    "[" + Movies.E_TITLE + "] varchar(128) COLLATE NOCASE, " +
                    "[" + Movies.E_CATEGORY + "] varchar(64) COLLATE NOCASE, " +
                    "[" + Movies.E_URL + "] varchar(256), " +
                    "[" + Movies.E_DESCRIPTION + "] text, " +
                    "[" + Movies.E_COMMENTS + "] text, " +
                    "[" + Movies.E_CREATED_BY + "] varchar(64) COLLATE NOCASE, " +
                    "[" + Movies.E_PICTURE + "] varchar(256) " +
                    ")");

            createIndexes(db);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            if (S.INFO)
                Log.i(S.TAG, "Upgrading database from version " + oldVersion + " to version " + newVersion +
                        ". This will drop tables and recreate.");

            dropIndexes(db);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME_EXTRAS);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME_CUSTOM);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
            onCreate(db);

            /* Update imported file size to zero so import is forced */
            if (S.INFO)
                Log.i(S.TAG, "Setting last catalog import size to 0 to force catalog refresh.");

            SharedPreferences.Editor editor = SharedObjects.getInstance().preferences.edit();
            editor.putLong(SettingsConstants.KEY_PREF_LAST_IMPORTED_SIZE, 0);
            editor.apply();
        }

        void createIndexes(SQLiteDatabase db) {
            db.execSQL("CREATE INDEX [IDX_MOVIES_NUMBER] ON [" + TABLE_NAME + "] ([" + Movies.NUMBER + "])");
            db.execSQL("CREATE INDEX [IDX_MOVIES_CHECKED] ON [" + TABLE_NAME + "] ([" + Movies.CHECKED + "])");
            db.execSQL("CREATE INDEX [IDX_MOVIES_FORMATTEDTITLE] ON [" + TABLE_NAME + "] ([" + Movies.FORMATTED_TITLE
                    + "])");
            db.execSQL("CREATE INDEX [IDX_MOVIES_RATING] ON [" + TABLE_NAME + "] ([" + Movies.RATING + "])");
            db.execSQL("CREATE INDEX [IDX_MOVIES_MEDIALABEL] ON [" + TABLE_NAME + "] ([" + Movies.MEDIA_LABEL + "])");
            db.execSQL("CREATE INDEX [IDX_MOVIES_YEAR] ON [" + TABLE_NAME + "] ([" + Movies.YEAR + "])");
            db.execSQL("CREATE INDEX [IDX_MOVIES_LENGTH] ON [" + TABLE_NAME + "] ([" + Movies.LENGTH + "])");
            db.execSQL("CREATE INDEX [IDX_MOVIES_DATE] ON [" + TABLE_NAME + "] ([" + Movies.DATE + "])");
            db.execSQL("CREATE INDEX [IDX_MOVIES_BORROWER] ON [" + TABLE_NAME + "] ([" + Movies.BORROWER + "])");
            db.execSQL("CREATE INDEX [IDX_MOVIES_COLORTAG] ON [" + TABLE_NAME + "] ([" + Movies.COLOR_TAG + "])");
            db.execSQL("CREATE INDEX [IDX_MOVIES_ORIGINALTITLE] ON [" + TABLE_NAME + "] ([" + Movies.ORIGINAL_TITLE +
                    "])");
            db.execSQL("CREATE INDEX [IDX_MOVIES_TRANSLATEDTITLE] ON [" + TABLE_NAME + "] ([" + Movies
                    .TRANSLATED_TITLE + "])");
            db.execSQL("CREATE INDEX [IDX_MOVIES_USERRATING] ON [" + TABLE_NAME + "] ([" + Movies.USER_RATING + "])");
            db.execSQL("CREATE INDEX [IDX_MOVIES_DATEWATCHED] ON [" + TABLE_NAME + "] ([" + Movies.DATE_WATCHED + "])");
            db.execSQL("CREATE INDEX [IDX_CUSTOM_MOVIES_ID] ON [" + TABLE_NAME_CUSTOM + "] ([" + Movies.MOVIES_ID +
                    "])");
            db.execSQL("CREATE INDEX [IDX_EXTRAS_MOVIES_ID] ON [" + TABLE_NAME_EXTRAS + "] ([" + Movies.MOVIES_ID +
                    "])");
        }

        void dropIndexes(SQLiteDatabase db) {
            db.execSQL("DROP INDEX IF EXISTS [IDX_MOVIES_NUMBER]");
            db.execSQL("DROP INDEX IF EXISTS [IDX_MOVIES_CHECKED]");
            db.execSQL("DROP INDEX IF EXISTS [IDX_MOVIES_FORMATTEDTITLE]");
            db.execSQL("DROP INDEX IF EXISTS [IDX_MOVIES_RATING]");
            db.execSQL("DROP INDEX IF EXISTS [IDX_MOVIES_MEDIALABEL]");
            db.execSQL("DROP INDEX IF EXISTS [IDX_MOVIES_YEAR]");
            db.execSQL("DROP INDEX IF EXISTS [IDX_MOVIES_LENGTH]");
            db.execSQL("DROP INDEX IF EXISTS [IDX_MOVIES_DATE]");
            db.execSQL("DROP INDEX IF EXISTS [IDX_MOVIES_BORROWER]");
            db.execSQL("DROP INDEX IF EXISTS [IDX_MOVIES_COLORTAG]");
            db.execSQL("DROP INDEX IF EXISTS [IDX_MOVIES_ORIGINALTITLE]");
            db.execSQL("DROP INDEX IF EXISTS [IDX_MOVIES_TRANSLATEDTITLE]");
            db.execSQL("DROP INDEX IF EXISTS [IDX_MOVIES_USERRATING]");
            db.execSQL("DROP INDEX IF EXISTS [IDX_MOVIES_DATEWATCHED]");
            db.execSQL("DROP INDEX IF EXISTS [IDX_CUSTOM_MOVIES_ID]");
            db.execSQL("DROP INDEX IF EXISTS [IDX_EXTRAS_MOVIES_ID]");
        }
    }

    // Empty constructor for manifest provider registration
    public MoviesDataProvider() {
    }

    public MoviesDataProvider(Context context) {

        if (SharedObjects.getInstance().db != null) {
            this.db = SharedObjects.getInstance().db;
            this.insertStatement = SharedObjects.getInstance().dbInsertStatement;
            this.insertCustomStatement = SharedObjects.getInstance().dbInsertCustomStatement;
            this.insertExtraStatement = SharedObjects.getInstance().dbInsertExtraStatement;
        } else {
            // Cache objects as this class gets initiated on every page
            this.openHelper = new MoviesOpenHelper(context);

            this.db = openHelper.getWritableDatabase();
            SharedObjects.getInstance().db = this.db;

            this.insertStatement = this.db.compileStatement(INSERT);
            SharedObjects.getInstance().dbInsertStatement = this.insertStatement;

            this.insertCustomStatement = this.db.compileStatement(INSERT_CUSTOM);
            SharedObjects.getInstance().dbInsertCustomStatement = this.insertCustomStatement;

            this.insertExtraStatement = this.db.compileStatement(INSERT_EXTRA);
            SharedObjects.getInstance().dbInsertExtraStatement = this.insertExtraStatement;
        }

        SharedObjects.getInstance().dbReferences++;
        if (S.DEBUG)
            Log.d(S.TAG, "DB reference added. Total: " + SharedObjects.getInstance().dbReferences);

        this.preferences = PreferenceManager.getDefaultSharedPreferences(context);
    }

    /**
     * Builds up a UriMatcher.
     */
    private static UriMatcher buildUriMatcher() {
        UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);

        matcher.addURI(S.AUTHORITY, "movies", GET_MOVIES);
        matcher.addURI(S.AUTHORITY, "movies/_id/#", GET_MOVIE);
        matcher.addURI(S.AUTHORITY, "movies/_id/#/extras", GET_MOVIE_EXTRAS);
        matcher.addURI(S.AUTHORITY, "movies/_id/#/custom", GET_MOVIE_CUSTOM_FIELDS);
        matcher.addURI(S.AUTHORITY, "movies/*", SEARCH_MOVIES);

        return matcher;
    }

    /**
     * Handles all the movies searches and suggestion queries from the Search Manager. When requesting a specific
     * movie, the uri alone is required. When searching all of the movie catalog for matches,
     * the selectionArgs argument must carry the search query as the first element. All other arguments are ignored.
     */
    @Override
    public Cursor query(@NonNull Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        // Use the UriMatcher to see what kind of query we have and format the DB query accordingly
        if (S.DEBUG)
            Log.d(S.TAG, uri.toString());

        Cursor cursor;
        String filterFields = null;
        String[] filterValues = null;

        switch (sURIMatcher.match(uri)) {
            case GET_MOVIES:
                if (S.DEBUG)
                    Log.d(S.TAG, "URI match: movies");

                // Get sort order from preferences
                sortOrder = this.preferences.getString(SettingsConstants.KEY_PREF_MOVIE_LIST_ORDER,
                        Movies.DEFAULT_SORT_ORDER);

                // Prepare filters
                if (this.filters != null) {
                    filterFields = this.filters.getFilterFields();
                    filterValues = this.filters.getFilterValues();
                }

                // Update projection to match only required fields
                updateProjection(sortOrder);
                projection = SharedObjects.getInstance().moviesProjection;

                // Execute query
                cursor = this.db.query(TABLE_NAME, projection, filterFields, filterValues, null, null, sortOrder);
                return cursor;

            case GET_MOVIE:
                if (S.DEBUG)
                    Log.d(S.TAG, "URI match: movie");
                cursor = this.db.query(TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
                return cursor;

            case GET_MOVIE_CUSTOM_FIELDS:
                if (S.DEBUG)
                    Log.d(S.TAG, "URI match: movie custom fields");
                cursor = this.db.query(TABLE_NAME_CUSTOM, projection, selection, selectionArgs, null, null, sortOrder);
                return cursor;

            case GET_MOVIE_EXTRAS:
                if (S.DEBUG)
                    Log.d(S.TAG, "URI match: movie extras");
                cursor = this.db.query(TABLE_NAME_EXTRAS, projection, selection, selectionArgs, null, null, sortOrder);
                return cursor;

            case SEARCH_MOVIES:
                if (S.DEBUG)
                    Log.d(S.TAG, "URI match: search");

                // Get sort order from preferences
                sortOrder = this.preferences.getString(SettingsConstants.KEY_PREF_MOVIE_LIST_ORDER,
                        Movies.DEFAULT_SORT_ORDER);

                // Prepare filters
                if (this.filters != null) {
                    filterFields = this.filters.getFilterFields();
                    filterValues = this.filters.getFilterValues();
                }

                // Update projection to match only required fields
                updateProjection(sortOrder);
                projection = SharedObjects.getInstance().moviesProjection;

                // Fill filters and execute query
                String likeField = "FormattedTitle like ?";
                filterFields = filterFields == null ? likeField : likeField + " and " + filterFields;
                String[] likeArg = new String[]{"%" + uri.getLastPathSegment() + "%"};
                filterValues = filterValues == null ? likeArg : Utils.joinArrays(likeArg, filterValues);
                cursor = this.db.query(TABLE_NAME, projection, filterFields, filterValues, null, null, sortOrder);
                return cursor;

            default:
                throw new IllegalArgumentException("Unknown Uri: " + uri);
        }
    }

    /**
     * Update projection so only required fields are loaded from database
     */
    private void updateProjection(String sortOrder) {
        HashSet<String> projection = new HashSet<>();

        // Required fields
        projection.add(BaseColumns._ID);

        // Always visible fields
        projection.add(Movies.COLOR_TAG);
        projection.add(Movies.PICTURE);
        projection.add(Movies.NUMBER);

        // Always add sorting column
        projection.add(Movies.SettingsSortFieldsMap.get(sortOrder));

        // Shown fields
        String[] listFields1 = this.preferences.getString(SettingsConstants.KEY_PREF_MOVIES_LIST_LINE1,
                Movies.defaultListFieldsLine1).split(",");
        if (!TextUtils.isEmpty(listFields1[0])) {
            projection.addAll(Arrays.asList(listFields1));
        }
        String[] listFields2 = this.preferences.getString(SettingsConstants.KEY_PREF_MOVIES_LIST_LINE2,
                Movies.defaultListFieldsLine2).split(",");
        if (!TextUtils.isEmpty(listFields2[0])) {
            projection.addAll(Arrays.asList(listFields2));
        }
        String[] listFields3 = this.preferences.getString(SettingsConstants.KEY_PREF_MOVIES_LIST_LINE3,
                Movies.defaultListFieldsLine3).split(",");
        if (!TextUtils.isEmpty(listFields3[0])) {
            projection.addAll(Arrays.asList(listFields3));
        }

        // Fill MOVIES_PROJECTION
        SharedObjects.getInstance().moviesProjection = projection.toArray(new String[0]);

        if (S.DEBUG)
            Log.d(S.TAG, "Projection fields: " + SharedObjects.getInstance().moviesProjection.length
                    + " - " + Utils.arrayToString(SharedObjects.getInstance().moviesProjection, ", "));
    }

    public Cursor fetchMovies(Uri uri) {
        return query(uri, SharedObjects.getInstance().moviesProjection, null, null, null);
    }

    public Cursor fetchMovie(Uri uri) {
        String[] eqArg = new String[]{uri.getLastPathSegment()};
        return query(uri, Movies.MOVIE_PROJECTION, BaseColumns._ID + "=?", eqArg, null);
    }

    public Cursor fetchMovieCustomFields(Uri uri) {
        List<String> segments = uri.getPathSegments();
        String[] eqArg = new String[]{segments.get(segments.size() - 2)};
        return query(uri, Movies.MOVIE_CUSTOM_FIELDS_PROJECTION, Movies.MOVIES_ID + "=?", eqArg, BaseColumns._ID +
                " " + "asc");
    }

    public Cursor fetchMovieExtras(Uri uri) {
        List<String> segments = uri.getPathSegments();
        String[] eqArg = new String[]{segments.get(segments.size() - 2)};
        return query(uri, Movies.MOVIE_EXTRA_PROJECTION, Movies.MOVIES_ID + "=?", eqArg, BaseColumns._ID + " asc");
    }

    /**
     * Insert data into MOVIES table
     */
    public long insert(String Number, String Checked, String FormattedTitle, String MediaLabel, String MediaType,
                       String Source, String Date, String Borrower, String Rating, String OriginalTitle,
                       String TranslatedTitle, String Director, String Producer, String Country, String Category,
                       String Year, String Length, String Actors, String URL, String Description, String Comments,
                       String VideoFormat, String VideoBitrate, String AudioFormat, String AudioBitrate,
                       String Resolution, String Framerate, String Languages, String Subtitles, String Size,
                       String Disks, String Picture, String ColorTag, String DateWatched, String UserRating,
                       String Writer, String Composer, String Certification, String FilePath) {
        this.insertStatement.bindString(1, Number);
        bindHelper(this.insertStatement, 2, Checked);
        bindHelper(this.insertStatement, 3, FormattedTitle);
        bindHelper(this.insertStatement, 4, MediaLabel);
        bindHelper(this.insertStatement, 5, MediaType);
        bindHelper(this.insertStatement, 6, Source);
        bindHelper(this.insertStatement, 7, Date);
        bindHelper(this.insertStatement, 8, Borrower);
        bindHelper(this.insertStatement, 9, Rating);
        bindHelper(this.insertStatement, 10, OriginalTitle);
        bindHelper(this.insertStatement, 11, TranslatedTitle);
        bindHelper(this.insertStatement, 12, Director);
        bindHelper(this.insertStatement, 13, Producer);
        bindHelper(this.insertStatement, 14, Country);
        bindHelper(this.insertStatement, 15, Category);
        bindHelper(this.insertStatement, 16, Year);
        bindHelper(this.insertStatement, 17, Length);
        bindHelper(this.insertStatement, 18, Actors);
        bindHelper(this.insertStatement, 19, URL);
        bindHelper(this.insertStatement, 20, Description);
        bindHelper(this.insertStatement, 21, Comments);
        bindHelper(this.insertStatement, 22, VideoFormat);
        bindHelper(this.insertStatement, 23, VideoBitrate);
        bindHelper(this.insertStatement, 24, AudioFormat);
        bindHelper(this.insertStatement, 25, AudioBitrate);
        bindHelper(this.insertStatement, 26, Resolution);
        bindHelper(this.insertStatement, 27, Framerate);
        bindHelper(this.insertStatement, 28, Languages);
        bindHelper(this.insertStatement, 29, Subtitles);
        bindHelper(this.insertStatement, 30, Size);
        bindHelper(this.insertStatement, 31, Disks);
        bindHelper(this.insertStatement, 32, Picture);
        bindHelper(this.insertStatement, 33, ColorTag);
        bindHelper(this.insertStatement, 34, DateWatched);
        bindHelper(this.insertStatement, 35, UserRating);
        bindHelper(this.insertStatement, 36, Writer);
        bindHelper(this.insertStatement, 37, Composer);
        bindHelper(this.insertStatement, 38, Certification);
        bindHelper(this.insertStatement, 39, FilePath);

        return this.insertStatement.executeInsert();
    }

    /**
     * Insert data into CUSTOM
     */
    public void insertCustom(long Movies_id, String CType, String CName, String CValue) {
        this.insertCustomStatement.bindLong(1, Movies_id);
        bindHelper(this.insertCustomStatement, 2, CType);
        bindHelper(this.insertCustomStatement, 3, CName);
        bindHelper(this.insertCustomStatement, 4, CValue);

        this.insertCustomStatement.executeInsert();
    }

    /**
     * Insert data into EXTRAS
     */
    public void insertExtra(long Movies_id, String EChecked, String ETag, String ETitle,
                            String ECategory, String EURL, String EDescription, String EComments,
                            String ECreatedBy, String EPicture) {
        this.insertExtraStatement.bindLong(1, Movies_id);
        bindHelper(this.insertExtraStatement, 2, EChecked);
        bindHelper(this.insertExtraStatement, 3, ETag);
        bindHelper(this.insertExtraStatement, 4, ETitle);
        bindHelper(this.insertExtraStatement, 5, ECategory);
        bindHelper(this.insertExtraStatement, 6, EURL);
        bindHelper(this.insertExtraStatement, 7, EDescription);
        bindHelper(this.insertExtraStatement, 8, EComments);
        bindHelper(this.insertExtraStatement, 9, ECreatedBy);
        bindHelper(this.insertExtraStatement, 10, EPicture);

        this.insertExtraStatement.executeInsert();
    }

    /**
     * Helper for inserting data
     */
    private void bindHelper(SQLiteStatement st, int i, String s) {
        if (s == null || s.equals("")) {
            st.bindNull(i);
        } else {
            st.bindString(i, s);
        }
    }

    /**
     * Create indexes in database
     */
    public void createIndexes() {
        if (this.openHelper != null && this.db != null)
            this.openHelper.createIndexes(this.db);
    }

    /**
     * Drop indexes from database
     */
    public void dropIndexes() {
        if (this.openHelper != null && this.db != null)
            this.openHelper.dropIndexes(this.db);
    }

    /**
     * Empty database
     */
    public void deleteAll() {
        this.db.delete(TABLE_NAME_EXTRAS, null, null);
        this.db.delete(TABLE_NAME_CUSTOM, null, null);
        this.db.delete(TABLE_NAME, null, null);
    }

    /**
     * Close database
     */
    public void closeDatabase() {
        SharedObjects.getInstance().dbReferences--;
        if (S.DEBUG)
            Log.d(S.TAG, "DB reference removed. Total: " + SharedObjects.getInstance().dbReferences);

        if (SharedObjects.getInstance().dbReferences == 0) {
            SharedObjects.getInstance().dbInsertStatement.close();
            SharedObjects.getInstance().dbInsertStatement = null;
            SharedObjects.getInstance().dbInsertCustomStatement.close();
            SharedObjects.getInstance().dbInsertCustomStatement = null;
            SharedObjects.getInstance().dbInsertExtraStatement.close();
            SharedObjects.getInstance().dbInsertExtraStatement = null;
            SharedObjects.getInstance().db.close();
            SharedObjects.getInstance().db = null;
        }
    }

    /**
     * Set filters for query
     */
    public void setFilters(Filters f) {
        this.filters = f;
    }

    @Override
    public String getType(@NonNull Uri uri) {
        switch (sURIMatcher.match(uri)) {
            case GET_MOVIES:
                if (S.DEBUG)
                    Log.d(S.TAG, "getType: Movies");
                return S.MOVIES_MIME_TYPE;
            case GET_MOVIE:
                if (S.DEBUG)
                    Log.d(S.TAG, "getType: Movie");
                return S.MOVIE_MIME_TYPE;
            case SEARCH_MOVIES:
                if (S.DEBUG)
                    Log.d(S.TAG, "getType: Search");
                return SearchManager.USER_QUERY;
            default:
                throw new IllegalArgumentException("Unknown Uri " + uri);
        }
    }

    @Override
    public Cursor runQuery(CharSequence charSequence) {
        Uri uri = Uri.withAppendedPath(S.CONTENT_URI, (String) charSequence);
        return query(uri, SharedObjects.getInstance().moviesProjection,
                null, new String[]{(String) charSequence}, null);
    }

    // Other required implementations. Currently unsupported
    @Override
    public boolean onCreate() {
        return false;
    }

    @Override
    public Uri insert(@NonNull Uri uri, ContentValues values) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int update(@NonNull Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int delete(@NonNull Uri uri, String selection, String[] selectionArgs) {
        throw new UnsupportedOperationException();
    }

}
