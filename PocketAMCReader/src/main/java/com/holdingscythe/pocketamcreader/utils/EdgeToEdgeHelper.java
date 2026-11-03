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
import android.graphics.Color;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.ComponentActivity;
import androidx.activity.EdgeToEdge;
import androidx.activity.SystemBarStyle;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

/**
 * Edge-to-edge support (enforced from Android 15 for apps targeting SDK 35 and newer).
 * <p>
 * The app draws behind the system bars: the toolbar extends under the status bar, the rest of the content is
 * kept inside the area which is not covered by the navigation bar and display cutouts. All application themes are
 * dark, so system bar icons are light.
 */
public final class EdgeToEdgeHelper {

    private static final int NAVIGATION_BAR_SCRIM = Color.argb(0x80, 0x1b, 0x1b, 0x1b);
    private static final int INSET_TYPES = WindowInsetsCompat.Type.systemBars() | WindowInsetsCompat.Type.displayCutout();

    private EdgeToEdgeHelper() {
    }

    /**
     * Call before setContentView()
     */
    public static void enable(ComponentActivity activity) {
        EdgeToEdge.enable(activity,
                SystemBarStyle.dark(Color.TRANSPARENT),
                SystemBarStyle.dark(NAVIGATION_BAR_SCRIM));
    }

    /**
     * Keep activity content away from the navigation bar and display cutouts (left, right, bottom).
     * Call after setContentView().
     */
    public static void padContent(Activity activity) {
        View content = activity.findViewById(android.R.id.content);

        ViewCompat.setOnApplyWindowInsetsListener(content, (view, windowInsets) -> {
            Insets bars = windowInsets.getInsets(INSET_TYPES);
            view.setPadding(bars.left, 0, bars.right, bars.bottom);

            // not consumed, children (e.g. app bar with fitsSystemWindows) still receive them
            return windowInsets;
        });
    }

    /**
     * Make the toolbar taller by the height of the status bar, so its background is drawn behind it.
     */
    public static void padToolbar(Toolbar toolbar) {
        TypedValue value = new TypedValue();
        toolbar.getContext().getTheme().resolveAttribute(android.R.attr.actionBarSize, value, true);
        int actionBarHeight = TypedValue.complexToDimensionPixelSize(value.data,
                toolbar.getResources().getDisplayMetrics());

        ViewCompat.setOnApplyWindowInsetsListener(toolbar, (view, windowInsets) -> {
            int top = windowInsets.getInsets(INSET_TYPES).top;

            view.setPadding(view.getPaddingLeft(), top, view.getPaddingRight(), view.getPaddingBottom());

            ViewGroup.LayoutParams params = view.getLayoutParams();
            params.height = actionBarHeight + top;
            view.setLayoutParams(params);

            return windowInsets;
        });
    }
}
