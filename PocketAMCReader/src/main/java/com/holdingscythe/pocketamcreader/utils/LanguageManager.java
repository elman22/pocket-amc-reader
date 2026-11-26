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

import java.util.Locale;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.os.LocaleListCompat;

/**
 * Applies and reads the application language.
 * <p>
 * The language is handled by {@link AppCompatDelegate#setApplicationLocales(LocaleListCompat)}. On Android 13+ it
 * delegates to the platform per-app language support (so the choice stays in sync with the system settings), on older
 * versions AppCompat stores the choice itself (see {@code AppLocalesMetadataHolderService} in the manifest). Running
 * activities are recreated automatically.
 * <p>
 * {@link #SYSTEM} means the system language. When the system language is not translated, Android falls back to the
 * default resources, which are English.
 */
public final class LanguageManager {

    /**
     * Value meaning "follow the system language".
     */
    public static final String SYSTEM = "system";

    private LanguageManager() {
    }

    /**
     * Applies the language to the whole application.
     *
     * @param value {@link #SYSTEM} or a BCP 47 language tag, e.g. {@code "cs"}
     */
    public static void apply(@NonNull String value) {
        AppCompatDelegate.setApplicationLocales(toLocales(value));
    }

    /**
     * Returns the language the application currently uses.
     *
     * @return BCP 47 language tag of the explicitly selected language, or {@link #SYSTEM}
     */
    @NonNull
    public static String getCurrent() {
        LocaleListCompat locales = AppCompatDelegate.getApplicationLocales();
        if (locales.isEmpty())
            return SYSTEM;

        Locale first = locales.get(0);
        return first == null ? SYSTEM : first.getLanguage();
    }

    /**
     * Converts the preference value to a locale list.
     *
     * @param value {@link #SYSTEM} or a BCP 47 language tag
     * @return empty list for the system language (removes the override), otherwise the requested language
     */
    @NonNull
    static LocaleListCompat toLocales(@NonNull String value) {
        return SYSTEM.equals(value) || value.isEmpty()
                ? LocaleListCompat.getEmptyLocaleList()
                : LocaleListCompat.forLanguageTags(value);
    }
}
