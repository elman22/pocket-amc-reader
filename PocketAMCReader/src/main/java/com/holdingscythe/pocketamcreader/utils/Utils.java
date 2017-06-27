package com.holdingscythe.pocketamcreader.utils;

import android.app.Activity;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.UnderlineSpan;

import java.util.ArrayList;

public class Utils {

    /**
     * Join string in array into one string with separator
     */
    public static String arrayToString(String[] str, String separator) {
        StringBuilder sb = new StringBuilder();
        if (str == null)
            return null;
        for (int f = 0; f < str.length; f++) {
            if (str[f] != null && str[f].length() > 0) {
                if (sb.length() != 0)
                    sb.append(separator);
                sb.append(str[f]);
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
        if (text == null || text.equals(""))
            return null;
        SpannableString strSpan = new SpannableString(text);
        strSpan.setSpan(new UnderlineSpan(), 0, strSpan.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        return strSpan;
    }

    /**
     * Get rating string
     */
    public static String getRatingString(String s, int n) {
        String rating = "";
        for (int f = 0; f < n; f++) {
            rating += s;
        }
        return rating;
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
     * Set the theme of the activity, according to the configuration
     */
    // TODO: validate usage for themes
    public static void onActivityCreateSetTheme(Activity activity, int theme) {
//        switch (theme) {
//            case Movies.THEME_NORMAL:
//                break;
//            case Movies.THEME_LARGE:
//                activity.setTheme(R.style.Theme_Large);
//                break;
//            case Movies.THEME_X_LARGE:
//                activity.setTheme(R.style.Theme_X_Large);
//                break;
//            case Movies.THEME_XX_LARGE:
//                activity.setTheme(R.style.Theme_XX_Large);
//                break;
//            default:
//                break;
//        }
    }

}
