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

package com.holdingscythe.pocketamcreader.utils;

import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.UnderlineSpan;
import android.webkit.MimeTypeMap;

import com.holdingscythe.pocketamcreader.S;

import java.util.ArrayList;
import java.util.Locale;

public class Utils {

    /**
     * Join string in array into one string with separator
     */
    public static String arrayToString(String[] str, String separator) {
        StringBuilder sb = new StringBuilder();
        if (str == null)
            return null;
        for (String s : str) {
            if (s != null && !s.isEmpty()) {
                if (sb.length() != 0)
                    sb.append(separator);
                sb.append(s);
            }
        }
        return sb.toString();
    }

    /**
     * Join string in array list into one string with separator
     */
    public static String arrayToString(ArrayList<String> arrayList, String separator) {
        String[] parsedArrayList = new String[arrayList.size()];
        return arrayToString(arrayList.toArray(parsedArrayList), separator);
    }

    /**
     * Join arrays into one array
     */
    public static String[] joinArrays(String[]... params) {
        // calculate size of target array
        int size = 0;
        for (String[] array : params) {
            size += array.length;
        }

        String[] result = new String[size];

        int j = 0;
        for (String[] array : params) {
            for (String s : array) {
                result[j++] = s;
            }
        }
        return result;
    }

    /**
     * Underline text
     */
    public static SpannableString markClickableText(String text) {
        if (text == null || text.isEmpty())
            return null;
        SpannableString strSpan = new SpannableString(text);
        strSpan.setSpan(new UnderlineSpan(), 0, strSpan.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        return strSpan;
    }

    /**
     * Get rating string
     */
    public static String getRatingString(String s, int n) {
        StringBuilder rating = new StringBuilder();
        for (int f = 0; f < n; f++) {
            rating.append(s);
        }
        return rating.toString();
    }

    /**
     * Safely convert long to int
     */
    public static int safeLongToInt(long l) {
        if (l < Integer.MIN_VALUE || l > Integer.MAX_VALUE) {
            throw new IllegalArgumentException
                    (l + " cannot be cast to int without changing its value.");
        }
        return (int) l;
    }

    /**
     * Lower case extension of the file name in a path or URL (both / and \\ separators), empty if there is none
     */
    public static String getFileExtension(String path) {
        // MimeTypeMap.getFileExtensionFromUrl() returns nothing for names with backslashes or spaces
        String name = path.substring(Math.max(path.lastIndexOf('/'), path.lastIndexOf('\\')) + 1);
        int dot = name.lastIndexOf('.');

        return dot < 0 ? "" : name.substring(dot + 1).toLowerCase(Locale.ROOT);
    }

    /**
     * Get MIME type from URL
     */
    public static String getMimeFromUrl(String url, String fallback) {
        String extension = getFileExtension(url);
        String mime = extension.isEmpty() ? null : MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);

        if (mime == null || mime.isEmpty()) {
            mime = fallback;
        }

        return mime;
    }

    /**
     * Return boolean whether custom color tag is set
     */
    public static boolean isCustomColorTagSet(String colorTag) {
        return colorTag != null &&
                !colorTag.isEmpty() &&
                !colorTag.equals(S.COLOR_TAG_DEFAULT) &&
                S.COLOR_TAGS.containsKey(colorTag);
    }

    /**
     * Return resource id for color based on color tag or default value
     */
    public static int getColorFromColorTag(String colorTag) {
        if (Utils.isCustomColorTagSet(colorTag)) {
            return S.COLOR_TAGS.get(colorTag);
        }
        return S.COLOR_TAGS.get(S.COLOR_TAG_DEFAULT);
    }

    /**
     * Java coalescing operator alternative
     */
    public static <T> T coalesce(T one, T two) {
        return one != null ? one : two;
    }

}
