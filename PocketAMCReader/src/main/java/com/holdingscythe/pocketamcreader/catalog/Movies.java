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

package com.holdingscythe.pocketamcreader.catalog;

import android.provider.BaseColumns;

import com.holdingscythe.pocketamcreader.R;
import com.holdingscythe.pocketamcreader.filters.FilterField;
import com.holdingscythe.pocketamcreader.filters.FilterOperator;
import com.holdingscythe.pocketamcreader.filters.FilterType;
import com.holdingscythe.pocketamcreader.settings.SettingsListField;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public final class Movies implements BaseColumns {
    // Fields mappings
    public static final String NUMBER = "Number";
    public static final String CHECKED = "Checked";
    public static final String FORMATTED_TITLE = "FormattedTitle";
    public static final String MEDIA_LABEL = "MediaLabel";
    public static final String MEDIA_TYPE = "MediaType";
    public static final String SOURCE = "Source";
    public static final String DATE = "Date";
    public static final String BORROWER = "Borrower";
    public static final String RATING = "Rating";
    public static final String ORIGINAL_TITLE = "OriginalTitle";
    public static final String TRANSLATED_TITLE = "TranslatedTitle";
    public static final String DIRECTOR = "Director";
    public static final String PRODUCER = "Producer";
    public static final String COUNTRY = "Country";
    public static final String CATEGORY = "Category";
    public static final String LENGTH = "Length";
    public static final String YEAR = "Year";
    public static final String ACTORS = "Actors";
    public static final String URL = "URL";
    public static final String DESCRIPTION = "Description";
    public static final String COMMENTS = "Comments";
    public static final String VIDEO_FORMAT = "VideoFormat";
    public static final String VIDEO_BITRATE = "VideoBitrate";
    public static final String AUDIO_FORMAT = "AudioFormat";
    public static final String AUDIO_BITRATE = "AudioBitrate";
    public static final String RESOLUTION = "Resolution";
    public static final String FRAMERATE = "Framerate";
    public static final String LANGUAGES = "Languages";
    public static final String SUBTITLES = "Subtitles";
    public static final String SIZE = "Size";
    public static final String DISKS = "Disks";
    public static final String PICTURE = "Picture";
    public static final String COLOR_TAG = "ColorTag";
    public static final String DATE_WATCHED = "DateWatched";
    public static final String USER_RATING = "UserRating";
    public static final String WRITER = "Writer";
    public static final String COMPOSER = "Composer";
    public static final String CERTIFICATION = "Certification";
    public static final String FILE_PATH = "FilePath";

    // Fields mapping foreign key for custom fields and extras
    public static final String MOVIES_ID = "Movies_id";

    // Fields mapping custom fields
    public static final String C_TYPE = "CType";
    public static final String C_NAME = "CName";
    public static final String C_VALUE = "CValue";

    // Fields mappings extras
    public static final String E_CHECKED = "EChecked";
    public static final String E_TAG = "ETag";
    public static final String E_TITLE = "ETitle";
    public static final String E_CATEGORY = "ECategory";
    public static final String E_URL = "EURL";
    public static final String E_DESCRIPTION = "EDescription";
    public static final String E_COMMENTS = "EComments";
    public static final String E_CREATED_BY = "ECreatedBy";
    public static final String E_PICTURE = "EPicture";

    // Custom fields properties
    public static final String P_TAG = "Tag";
    public static final String P_NAME = "Name";
    public static final String P_TYPE = "Type";

    // Default projection for Movies List
    public static String[] MOVIES_PROJECTION = new String[]{BaseColumns._ID, Movies.NUMBER, Movies.FORMATTED_TITLE,
            Movies.CATEGORY, Movies.LENGTH, Movies.YEAR, Movies.PICTURE, Movies.RATING, Movies.BORROWER,
            Movies.MEDIA_LABEL, Movies.MEDIA_TYPE, Movies.SOURCE, Movies.DIRECTOR, Movies.PRODUCER, Movies.COUNTRY,
            Movies.VIDEO_FORMAT, Movies.AUDIO_FORMAT, Movies.RESOLUTION, Movies.CHECKED, Movies.ORIGINAL_TITLE,
            Movies.TRANSLATED_TITLE, Movies.COLOR_TAG, Movies.DATE};

    // Projection for Movie
    public static final String[] MOVIE_PROJECTION = new String[]{BaseColumns._ID, Movies.NUMBER, Movies.CHECKED,
            Movies.FORMATTED_TITLE, Movies.MEDIA_LABEL, Movies.MEDIA_TYPE, Movies.SOURCE, Movies.DATE, Movies.BORROWER,
            Movies.RATING, Movies.ORIGINAL_TITLE, Movies.TRANSLATED_TITLE, Movies.DIRECTOR, Movies.PRODUCER,
            Movies.COUNTRY, Movies.CATEGORY, Movies.YEAR, Movies.LENGTH, Movies.ACTORS, Movies.URL,
            Movies.DESCRIPTION, Movies.COMMENTS, Movies.VIDEO_FORMAT, Movies.VIDEO_BITRATE, Movies.AUDIO_FORMAT,
            Movies.AUDIO_BITRATE, Movies.RESOLUTION, Movies.FRAMERATE, Movies.LANGUAGES, Movies.SUBTITLES,
            Movies.SIZE, Movies.DISKS, Movies.PICTURE, Movies.COLOR_TAG, Movies.DATE_WATCHED, Movies.USER_RATING,
            Movies.WRITER, Movies.COMPOSER, Movies.CERTIFICATION, Movies.FILE_PATH};

    // Projection for Custom Fields
    public static final String[] MOVIE_CUSTOM_FIELDS_PROJECTION = new String[]{BaseColumns._ID, Movies.MOVIES_ID,
            Movies.C_TYPE, Movies.C_NAME, Movies.C_VALUE};

    // Projection for Movie Extras
    public static final String[] MOVIE_EXTRA_PROJECTION = new String[]{BaseColumns._ID, Movies.MOVIES_ID,
            Movies.E_CHECKED, Movies.E_TAG, Movies.E_TITLE, Movies.E_CATEGORY, Movies.E_URL, Movies.E_DESCRIPTION,
            Movies.E_COMMENTS, Movies.E_CREATED_BY, Movies.E_PICTURE};

    // Sorting definitions
    public static final String DEFAULT_SORT_ORDER = "FormattedTitle asc";
    public static final String SORT_ORDER_TITLE_ASC = "FormattedTitle asc";
    public static final String SORT_ORDER_TITLE_DESC = "FormattedTitle desc";
    public static final String SORT_ORDER_NUMBER_ASC = "Number asc, FormattedTitle asc";
    public static final String SORT_ORDER_NUMBER_DESC = "Number desc, FormattedTitle asc";
    public static final String SORT_ORDER_YEAR_ASC = "Year asc, FormattedTitle asc";
    public static final String SORT_ORDER_YEAR_DESC = "Year desc, FormattedTitle asc";
    public static final String SORT_ORDER_LENGTH_ASC = "Length asc, FormattedTitle asc";
    public static final String SORT_ORDER_LENGTH_DESC = "Length desc, FormattedTitle asc";
    public static final String SORT_ORDER_RATING_ASC = "Rating asc, FormattedTitle asc";
    public static final String SORT_ORDER_RATING_DESC = "Rating desc, FormattedTitle asc";
    public static final String SORT_ORDER_BORROWER_ASC = "Borrower asc, FormattedTitle asc";
    public static final String SORT_ORDER_BORROWER_DESC = "Borrower desc, FormattedTitle asc";
    public static final String SORT_ORDER_MEDIALABEL_ASC = "MediaLabel asc, FormattedTitle asc";
    public static final String SORT_ORDER_MEDIALABEL_DESC = "MediaLabel desc, FormattedTitle asc";
    public static final String SORT_ORDER_COLOR_TAG_ASC = "ColorTag asc, FormattedTitle asc";
    public static final String SORT_ORDER_COLOR_TAG_DESC = "ColorTag desc, FormattedTitle asc";
    public static final String SORT_ORDER_ORIGINAL_TITLE_ASC = "OriginalTitle asc, FormattedTitle asc";
    public static final String SORT_ORDER_ORIGINAL_TITLE_DESC = "OriginalTitle desc, FormattedTitle asc";
    public static final String SORT_ORDER_TRANSLATED_TITLE_ASC = "TranslatedTitle asc, FormattedTitle asc";
    public static final String SORT_ORDER_TRANSLATED_TITLE_DESC = "TranslatedTitle desc, FormattedTitle asc";
    public static final String SORT_ORDER_DATE_ASC = "Date asc, FormattedTitle asc";
    public static final String SORT_ORDER_DATE_DESC = "Date desc, FormattedTitle asc";
    public static final String SORT_ORDER_DATE_WATCHED_ASC = "DateWatched asc, FormattedTitle asc";
    public static final String SORT_ORDER_DATE_WATCHED_DESC = "DateWatched desc, FormattedTitle asc";
    public static final String SORT_ORDER_USER_RATING_ASC = "UserRating asc, FormattedTitle asc";
    public static final String SORT_ORDER_USER_RATING_DESC = "UserRating desc, FormattedTitle asc";
    public static final String SORT_ORDER_CERTIFICATION_ASC = "Certification asc, FormattedTitle asc";
    public static final String SORT_ORDER_CERTIFICATION_DESC = "Certification desc, FormattedTitle asc";

    // Map of sort fields - keep in sync with available ordering
    public static final Map<String, String> SettingsSortFieldsMap;

    static {
        Map<String, String> tmpMap = new HashMap<String, String>();
        tmpMap.put(Movies.DEFAULT_SORT_ORDER, Movies.FORMATTED_TITLE);
        tmpMap.put(Movies.SORT_ORDER_TITLE_ASC, Movies.FORMATTED_TITLE);
        tmpMap.put(Movies.SORT_ORDER_TITLE_DESC, Movies.FORMATTED_TITLE);
        tmpMap.put(Movies.SORT_ORDER_NUMBER_ASC, Movies.NUMBER);
        tmpMap.put(Movies.SORT_ORDER_NUMBER_DESC, Movies.NUMBER);
        tmpMap.put(Movies.SORT_ORDER_YEAR_ASC, Movies.YEAR);
        tmpMap.put(Movies.SORT_ORDER_YEAR_DESC, Movies.YEAR);
        tmpMap.put(Movies.SORT_ORDER_LENGTH_ASC, Movies.LENGTH);
        tmpMap.put(Movies.SORT_ORDER_LENGTH_DESC, Movies.LENGTH);
        tmpMap.put(Movies.SORT_ORDER_RATING_ASC, Movies.RATING);
        tmpMap.put(Movies.SORT_ORDER_RATING_DESC, Movies.RATING);
        tmpMap.put(Movies.SORT_ORDER_BORROWER_ASC, Movies.BORROWER);
        tmpMap.put(Movies.SORT_ORDER_BORROWER_DESC, Movies.BORROWER);
        tmpMap.put(Movies.SORT_ORDER_MEDIALABEL_ASC, Movies.MEDIA_LABEL);
        tmpMap.put(Movies.SORT_ORDER_MEDIALABEL_DESC, Movies.MEDIA_LABEL);
        tmpMap.put(Movies.SORT_ORDER_COLOR_TAG_ASC, Movies.COLOR_TAG);
        tmpMap.put(Movies.SORT_ORDER_COLOR_TAG_DESC, Movies.COLOR_TAG);
        tmpMap.put(Movies.SORT_ORDER_ORIGINAL_TITLE_ASC, Movies.ORIGINAL_TITLE);
        tmpMap.put(Movies.SORT_ORDER_ORIGINAL_TITLE_DESC, Movies.ORIGINAL_TITLE);
        tmpMap.put(Movies.SORT_ORDER_TRANSLATED_TITLE_ASC, Movies.TRANSLATED_TITLE);
        tmpMap.put(Movies.SORT_ORDER_TRANSLATED_TITLE_DESC, Movies.TRANSLATED_TITLE);
        tmpMap.put(Movies.SORT_ORDER_DATE_ASC, Movies.DATE);
        tmpMap.put(Movies.SORT_ORDER_DATE_DESC, Movies.DATE);
        tmpMap.put(Movies.SORT_ORDER_DATE_WATCHED_ASC, Movies.DATE_WATCHED);
        tmpMap.put(Movies.SORT_ORDER_DATE_WATCHED_DESC, Movies.DATE_WATCHED);
        tmpMap.put(Movies.SORT_ORDER_USER_RATING_ASC, Movies.USER_RATING);
        tmpMap.put(Movies.SORT_ORDER_USER_RATING_DESC, Movies.USER_RATING);
        tmpMap.put(Movies.SORT_ORDER_CERTIFICATION_ASC, Movies.CERTIFICATION);
        tmpMap.put(Movies.SORT_ORDER_CERTIFICATION_DESC, Movies.CERTIFICATION);
        SettingsSortFieldsMap = Collections.unmodifiableMap(tmpMap);
    }

    // Filter operators
    public static final FilterOperator FILTER_OPERATOR_EQUALS = new FilterOperator("=", R.string.filter_type_equals,
            "", "");
    public static final FilterOperator FILTER_OPERATOR_EQUALS_NOT = new FilterOperator("!=",
            R.string.filter_type_equals_not, "", "");
    public static final FilterOperator FILTER_OPERATOR_IS = new FilterOperator("=", R.string.filter_type_is, "", "");
    public static final FilterOperator FILTER_OPERATOR_LARGER = new FilterOperator(">", R.string.filter_type_larger,
            "", "");
    public static final FilterOperator FILTER_OPERATOR_SMALLER = new FilterOperator("<",
            R.string.filter_type_smaller, "", "");
    public static final FilterOperator FILTER_OPERATOR_CONTAINS = new FilterOperator("like",
            R.string.filter_type_contains, "%", "%");
    public static final FilterOperator FILTER_OPERATOR_CONTAINS_NOT =
            new FilterOperator("not like", R.string.filter_type_contains_not, "%", "%");
    public static final FilterOperator FILTER_OPERATOR_BEGINS_WITH = new FilterOperator("like",
            R.string.filter_type_begins_with, "", "%");
    public static final FilterOperator FILTER_OPERATOR_ENDS_WITH = new FilterOperator("like",
            R.string.filter_type_ends_with, "%", "");
    public static final FilterOperator FILTER_OPERATOR_IS_NULL = new FilterOperator("is null",
            R.string.filter_type_is_null, "", "");
    public static final FilterOperator FILTER_OPERATOR_IS_NULL_NOT =
            new FilterOperator("is not null", R.string.filter_type_is_null_not, "", "");

    // Filter types
    public static final FilterType FILTER_TYPE_TEXT = new FilterType(new FilterOperator[]{Movies.FILTER_OPERATOR_IS,
            Movies.FILTER_OPERATOR_CONTAINS, Movies.FILTER_OPERATOR_CONTAINS_NOT, Movies.FILTER_OPERATOR_BEGINS_WITH,
            Movies.FILTER_OPERATOR_ENDS_WITH, Movies.FILTER_OPERATOR_IS_NULL, Movies.FILTER_OPERATOR_IS_NULL_NOT});
    public static final FilterType FILTER_TYPE_BOOLEAN = new FilterType(new FilterOperator[]{
            Movies.FILTER_OPERATOR_EQUALS});
    public static final FilterType FILTER_TYPE_NUMBER = new FilterType(new FilterOperator[]{
            Movies.FILTER_OPERATOR_EQUALS, Movies.FILTER_OPERATOR_LARGER, Movies.FILTER_OPERATOR_SMALLER,
            Movies.FILTER_OPERATOR_EQUALS_NOT, Movies.FILTER_OPERATOR_IS_NULL, Movies.FILTER_OPERATOR_IS_NULL_NOT});
    public static final FilterType FILTER_TYPE_PICTURE = new FilterType(new FilterOperator[]{
            Movies.FILTER_OPERATOR_IS_NULL, Movies.FILTER_OPERATOR_IS_NULL_NOT});
    public static final FilterType FILTER_TYPE_DATE = new FilterType(new FilterOperator[]{Movies.FILTER_OPERATOR_EQUALS,
            Movies.FILTER_OPERATOR_LARGER, Movies.FILTER_OPERATOR_SMALLER, Movies.FILTER_OPERATOR_IS_NULL,
            Movies.FILTER_OPERATOR_IS_NULL_NOT});

    // Filter fields
    public static final FilterField FILTER_NUMBER =
            new FilterField(Movies.FILTER_TYPE_NUMBER, Movies.NUMBER, R.string.details_number_label);
    public static final FilterField FILTER_CHECKED =
            new FilterField(Movies.FILTER_TYPE_BOOLEAN, Movies.CHECKED, R.string.details_checked_label);
    public static final FilterField FILTER_FORMATTED_TITLE =
            new FilterField(Movies.FILTER_TYPE_TEXT, Movies.FORMATTED_TITLE, R.string.details_formattedtitle_label);
    public static final FilterField FILTER_ORIGINAL_TITLE =
            new FilterField(Movies.FILTER_TYPE_TEXT, Movies.ORIGINAL_TITLE, R.string.details_originaltitle_label);
    public static final FilterField FILTER_TRANSLATED_TITLE =
            new FilterField(Movies.FILTER_TYPE_TEXT, Movies.TRANSLATED_TITLE, R.string.details_translatedtitle_label);
    public static final FilterField FILTER_MEDIA_LABEL =
            new FilterField(Movies.FILTER_TYPE_TEXT, Movies.MEDIA_LABEL, R.string.details_medialabel_label);
    public static final FilterField FILTER_MEDIA_TYPE =
            new FilterField(Movies.FILTER_TYPE_TEXT, Movies.MEDIA_TYPE, R.string.details_mediatype_label);
    public static final FilterField FILTER_SOURCE =
            new FilterField(Movies.FILTER_TYPE_TEXT, Movies.SOURCE, R.string.details_source_label);
    public static final FilterField FILTER_DATE =
            new FilterField(Movies.FILTER_TYPE_DATE, Movies.DATE, R.string.details_date_label);
    public static final FilterField FILTER_BORROWER =
            new FilterField(Movies.FILTER_TYPE_TEXT, Movies.BORROWER, R.string.details_borrower_label);
    public static final FilterField FILTER_RATING =
            new FilterField(Movies.FILTER_TYPE_NUMBER, Movies.RATING, R.string.details_rating_label);
    public static final FilterField FILTER_DIRECTOR =
            new FilterField(Movies.FILTER_TYPE_TEXT, Movies.DIRECTOR, R.string.details_director_label);
    public static final FilterField FILTER_PRODUCER =
            new FilterField(Movies.FILTER_TYPE_TEXT, Movies.PRODUCER, R.string.details_producer_label);
    public static final FilterField FILTER_COUNTRY =
            new FilterField(Movies.FILTER_TYPE_TEXT, Movies.COUNTRY, R.string.details_country_label);
    public static final FilterField FILTER_CATEGORY =
            new FilterField(Movies.FILTER_TYPE_TEXT, Movies.CATEGORY, R.string.details_category_label);
    public static final FilterField FILTER_YEAR =
            new FilterField(Movies.FILTER_TYPE_NUMBER, Movies.YEAR, R.string.details_year_label);
    public static final FilterField FILTER_LENGTH =
            new FilterField(Movies.FILTER_TYPE_NUMBER, Movies.LENGTH, R.string.details_length_label);
    public static final FilterField FILTER_ACTORS =
            new FilterField(Movies.FILTER_TYPE_TEXT, Movies.ACTORS, R.string.details_actors_label);
    // public static final FilterField FILTER_URL = new FilterField(); // [URL] varchar(256)
    public static final FilterField FILTER_DESCRIPTION =
            new FilterField(Movies.FILTER_TYPE_TEXT, Movies.DESCRIPTION, R.string.details_description_label);
    public static final FilterField FILTER_COMMENTS =
            new FilterField(Movies.FILTER_TYPE_TEXT, Movies.COMMENTS, R.string.details_comments_label);
    public static final FilterField FILTER_VIDEO_FORMAT =
            new FilterField(Movies.FILTER_TYPE_TEXT, Movies.VIDEO_FORMAT, R.string.details_videoformat_label);
    public static final FilterField FILTER_VIDEO_BITRATE =
            new FilterField(Movies.FILTER_TYPE_NUMBER, Movies.VIDEO_BITRATE, R.string.details_videobitrate_label);
    public static final FilterField FILTER_AUDIO_FORMAT =
            new FilterField(Movies.FILTER_TYPE_TEXT, Movies.AUDIO_FORMAT, R.string.details_audioformat_label);
    public static final FilterField FILTER_AUDIO_BITRATE =
            new FilterField(Movies.FILTER_TYPE_NUMBER, Movies.AUDIO_BITRATE, R.string.details_audiobitrate_label);
    public static final FilterField FILTER_RESOLUTION =
            new FilterField(Movies.FILTER_TYPE_TEXT, Movies.RESOLUTION, R.string.details_resolution_label);
    public static final FilterField FILTER_FRAMERATE =
            new FilterField(Movies.FILTER_TYPE_NUMBER, Movies.FRAMERATE, R.string.details_framerate_label);
    public static final FilterField FILTER_LANGUAGES =
            new FilterField(Movies.FILTER_TYPE_TEXT, Movies.LANGUAGES, R.string.details_languages_label);
    public static final FilterField FILTER_SUBTITLES =
            new FilterField(Movies.FILTER_TYPE_TEXT, Movies.SUBTITLES, R.string.details_subtitles_label);
    public static final FilterField FILTER_SIZE =
            new FilterField(Movies.FILTER_TYPE_TEXT, Movies.SIZE, R.string.details_size_label);
    public static final FilterField FILTER_DISKS =
            new FilterField(Movies.FILTER_TYPE_NUMBER, Movies.DISKS, R.string.details_disks_label);
    public static final FilterField FILTER_PICTURE =
            new FilterField(Movies.FILTER_TYPE_PICTURE, Movies.PICTURE, R.string.details_picture_label);
    public static final FilterField FILTER_COLOR_TAG =
            new FilterField(Movies.FILTER_TYPE_NUMBER, Movies.COLOR_TAG, R.string.details_colortag_label);
    public static final FilterField FILTER_DATE_WATCHED =
            new FilterField(Movies.FILTER_TYPE_DATE, Movies.DATE_WATCHED, R.string.details_datewatched_label);
    public static final FilterField FILTER_USER_RATING =
            new FilterField(Movies.FILTER_TYPE_NUMBER, Movies.USER_RATING, R.string.details_userrating_label);
    public static final FilterField FILTER_WRITER =
            new FilterField(Movies.FILTER_TYPE_TEXT, Movies.WRITER, R.string.details_writer_label);
    public static final FilterField FILTER_COMPOSER =
            new FilterField(Movies.FILTER_TYPE_TEXT, Movies.COMPOSER, R.string.details_composer_label);
    public static final FilterField FILTER_CERTIFICATION =
            new FilterField(Movies.FILTER_TYPE_TEXT, Movies.CERTIFICATION, R.string.details_certification_label);

    // Available filter fields for add field method - keep sorted as availableListFields[] to have consistent GUI
    public static final FilterField availableFilterFields[] = {Movies.FILTER_CHECKED, Movies.FILTER_FORMATTED_TITLE,
            Movies.FILTER_RATING, Movies.FILTER_USER_RATING, Movies.FILTER_DIRECTOR, Movies.FILTER_ACTORS,
            Movies.FILTER_CATEGORY, Movies.FILTER_MEDIA_LABEL, Movies.FILTER_LANGUAGES, Movies.FILTER_SUBTITLES,
            Movies.FILTER_COUNTRY, Movies.FILTER_LENGTH, Movies.FILTER_YEAR, Movies.FILTER_CERTIFICATION,
            Movies.FILTER_COLOR_TAG, Movies.FILTER_DESCRIPTION, Movies.FILTER_COMMENTS, Movies.FILTER_PRODUCER,
            Movies.FILTER_WRITER, Movies.FILTER_COMPOSER, Movies.FILTER_BORROWER, Movies.FILTER_NUMBER,
            Movies.FILTER_DATE, Movies.FILTER_DATE_WATCHED, Movies.FILTER_ORIGINAL_TITLE,
            Movies.FILTER_TRANSLATED_TITLE, Movies.FILTER_SOURCE, Movies.FILTER_MEDIA_TYPE,
            Movies.FILTER_VIDEO_FORMAT, Movies.FILTER_VIDEO_BITRATE, Movies.FILTER_AUDIO_FORMAT,
            Movies.FILTER_AUDIO_BITRATE, Movies.FILTER_RESOLUTION, Movies.FILTER_FRAMERATE, Movies.FILTER_DISKS,
            Movies.FILTER_SIZE, Movies.FILTER_PICTURE};

    // Available fields for list display - keep sorted as availableFilterFields[] to have consistent GUI
    public static final String defaultListFieldsLine1 = Movies.FORMATTED_TITLE;
    public static final String defaultListFieldsLine2 = Movies.NUMBER + "," + Movies.YEAR + "," + Movies.LENGTH + "," +
            Movies.RATING;
    public static final String defaultListFieldsLine3 = Movies.CATEGORY;
    public static final SettingsListField[] availableListFields = {
            new SettingsListField(1, Movies.CHECKED, R.string.details_checked_label),
            new SettingsListField(2, Movies.FORMATTED_TITLE, R.string.details_formattedtitle_label),
            new SettingsListField(3, Movies.RATING, R.string.details_rating_label),
            new SettingsListField(4, Movies.USER_RATING, R.string.details_userrating_label),
            new SettingsListField(5, Movies.DIRECTOR, R.string.details_director_label),
            new SettingsListField(6, Movies.CATEGORY, R.string.details_category_label),
            new SettingsListField(7, Movies.MEDIA_LABEL, R.string.details_medialabel_label),
            new SettingsListField(8, Movies.COUNTRY, R.string.details_country_label),
            new SettingsListField(9, Movies.LENGTH, R.string.details_length_label),
            new SettingsListField(10, Movies.YEAR, R.string.details_year_label),
            new SettingsListField(11, Movies.CERTIFICATION, R.string.details_certification_label),
            new SettingsListField(12, Movies.COLOR_TAG, R.string.details_colortag_label),
            new SettingsListField(13, Movies.PRODUCER, R.string.details_producer_label),
            new SettingsListField(14, Movies.WRITER, R.string.details_writer_label),
            new SettingsListField(15, Movies.COMPOSER, R.string.details_composer_label),
            new SettingsListField(16, Movies.BORROWER, R.string.details_borrower_label),
            new SettingsListField(17, Movies.NUMBER, R.string.details_number_label),
            new SettingsListField(18, Movies.DATE, R.string.details_date_label),
            new SettingsListField(19, Movies.DATE_WATCHED, R.string.details_datewatched_label),
            new SettingsListField(20, Movies.ORIGINAL_TITLE, R.string.details_originaltitle_label),
            new SettingsListField(21, Movies.TRANSLATED_TITLE, R.string.details_translatedtitle_label),
            new SettingsListField(22, Movies.SOURCE, R.string.details_source_label),
            new SettingsListField(23, Movies.MEDIA_TYPE, R.string.details_mediatype_label),
            new SettingsListField(24, Movies.VIDEO_FORMAT, R.string.details_videoformat_label),
            new SettingsListField(25, Movies.AUDIO_FORMAT, R.string.details_audioformat_label),
            new SettingsListField(26, Movies.RESOLUTION, R.string.details_resolution_label),
    };

}
