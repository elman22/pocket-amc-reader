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

import android.app.Activity;
import android.content.SharedPreferences;
import android.util.Log;

import com.holdingscythe.pocketamcreader.R;
import com.holdingscythe.pocketamcreader.S;
import com.holdingscythe.pocketamcreader.settings.SettingsConstants;

import androidx.preference.PreferenceManager;

public class Theme {
    private Activity activity;

    public Theme(Activity activity) {
        this.activity = activity;
    }

    public int getTheme() {
        // Get preferences
        if (SharedObjects.getInstance().preferences == null) {
            // Prepare Shared Objects
            SharedObjects.getInstance().preferences =
                    PreferenceManager.getDefaultSharedPreferences(this.activity.getApplicationContext());
        }

        SharedPreferences preferences = SharedObjects.getInstance().preferences;
        String settingTheme = preferences.getString(SettingsConstants.KEY_PREF_THEME,
                this.activity.getString(R.string.pref_setting_theme_default_value));

        assert settingTheme != null;
        int theme = switch (settingTheme) {
            case S.THEME_RED -> R.style.AppThemeRed;
            case S.THEME_INDIGO -> R.style.AppThemeIndigo;
            case S.THEME_GREEN -> R.style.AppThemeGreen;
            case S.THEME_ORANGE -> R.style.AppThemeOrange;
            case S.THEME_GREY -> R.style.AppThemeGrey;
            case S.THEME_BROWN -> R.style.AppThemeBrown;
            case S.THEME_PINK -> R.style.AppThemePink;
            case S.THEME_PURPLE -> R.style.AppThemePurple;
            case S.THEME_BLACK -> R.style.AppThemeBlack;
            default -> R.style.AppThemeBlue; // S.THEME_BLUE
        };

        if (S.DEBUG)
            Log.d(S.TAG, "Setting theme " + settingTheme + " for activity " + this.activity.getLocalClassName());

        return theme;
    }
}
