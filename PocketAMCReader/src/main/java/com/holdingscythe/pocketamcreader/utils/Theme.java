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

import android.app.Activity;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.holdingscythe.pocketamcreader.R;
import com.holdingscythe.pocketamcreader.S;

public class Theme {
    private Activity activity;

    public Theme(Activity activity) {
        this.activity = activity;
    }

    public int getTheme() {
        int theme;

        // Get preferences
        if (SharedObjects.getInstance().preferences == null) {
            // Prepare Shared Objects
            SharedObjects.getInstance().preferences =
                    PreferenceManager.getDefaultSharedPreferences(this.activity.getApplicationContext());
        }

        SharedPreferences preferences = SharedObjects.getInstance().preferences;
        String settingTheme = preferences.getString("settingTheme",
                this.activity.getString(R.string.pref_setting_theme_default_value));

        switch (settingTheme) {
            case S.THEME_RED:
                theme = R.style.AppThemeRed;
                break;
            case S.THEME_BLUE:
            default:
                theme = R.style.AppThemeBlue;
                break;
        }

        return theme;
    }
}
