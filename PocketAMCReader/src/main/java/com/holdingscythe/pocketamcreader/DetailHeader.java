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

package com.holdingscythe.pocketamcreader;

import android.view.View;
import android.view.ViewGroup;

import com.google.android.material.appbar.AppBarLayout;

import androidx.annotation.NonNull;
import androidx.coordinatorlayout.widget.CoordinatorLayout;

/**
 * Keeps the header of the movie detail (picture, title block and the default scroll position of the app bar)
 * consistent.
 * <p>
 * The picture is as tall as the whole picture and is drawn behind the title block. An invisible spacer as tall
 * as the picture pushes the title block below it, so when the app bar is fully expanded the picture is complete
 * and the title block is below it. While the bar is partly collapsed the parallax scrolling moves the picture
 * under the title block, which overlays its bottom.
 * <p>
 * CollapsingToolbarLayout moves its children which are not drawn behind the status bar (fitsSystemWindows) down
 * by the height of the status bar, the picture is not moved. It does so when it has received the window insets,
 * which can be after the first layout (e.g. a page of a ViewPager2 is laid out when it is added, the insets
 * arrive later). The spacer is therefore adjusted after every layout of the app bar, so the title block always
 * starts where the picture ends.
 * <p>
 * The color tag of the movie is a thin line at the top of the title block (only for movies which have a color
 * tag). It covers the bottom edge of the picture instead of adding its height to the header, so the header, the
 * default position and everything under it are the same for movies with and without a color tag.
 * <p>
 * The default position of the app bar shows the hero part of the picture and the toolbar, and the title block
 * under them. A movie without picture (the placeholder is as tall as these two) is not scrolled at all, a movie
 * with a picture is scrolled so its title block ends at the same place. The position does not depend on the
 * window insets.
 * <p>
 * The app bar reports its offset when its layout is finished; everything is done then. Moving the app bar while
 * the toolbar is still being laid out is overwritten by the layout of the app bar.
 */
class DetailHeader implements AppBarLayout.OnOffsetChangedListener {

    private final AppBarLayout mAppBarLayout;
    private final View mHeader;
    private final View mSpacer;
    private final View mColorTag;
    private final int mPictureHeight;
    private final int mHeroHeight;
    private final int mActionBarHeight;
    private final int mColorTagHeight;

    private boolean mRestPositionSet = false;

    /**
     * @param pictureHeight   height of the whole picture
     * @param heroHeight      height of the part of the picture which is displayed when the movie is opened
     * @param actionBarHeight height of the toolbar
     * @param colorTag        the line of the color tag (it is visible only when the movie has a color tag)
     * @param colorTagHeight  height of the line when it is visible
     */
    DetailHeader(@NonNull AppBarLayout appBarLayout, @NonNull View header, @NonNull View spacer,
                 @NonNull View colorTag, int pictureHeight, int heroHeight, int actionBarHeight,
                 int colorTagHeight) {
        mAppBarLayout = appBarLayout;
        mHeader = header;
        mSpacer = spacer;
        mColorTag = colorTag;
        mPictureHeight = pictureHeight;
        mHeroHeight = heroHeight;
        mActionBarHeight = actionBarHeight;
        mColorTagHeight = colorTagHeight;

        setHeight(mSpacer, Math.max(0, pictureHeight - visibleColorTagHeight()));

        // Called when the layout of the app bar is finished (also when it was laid out because of the window insets)
        mAppBarLayout.addOnOffsetChangedListener(this);
    }

    @Override
    public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
        // The title block starts where the picture ends (the color tag line, if there is one, covers its bottom
        // edge). The change lays out the app bar again, the rest is done then.
        int spacerHeight = Math.max(0, mPictureHeight - mHeader.getTop() - visibleColorTagHeight());
        if (mSpacer.getLayoutParams().height != spacerHeight) {
            setHeight(mSpacer, spacerHeight);
            return;
        }

        // Default position of the app bar, set once
        if (mRestPositionSet) {
            return;
        }

        CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams) appBarLayout.getLayoutParams();
        if (!(params.getBehavior() instanceof AppBarLayout.Behavior)) {
            return;
        }
        AppBarLayout.Behavior behavior = (AppBarLayout.Behavior) params.getBehavior();

        // The visible part of the app bar is the hero height, the toolbar and the title block. A picture which is
        // taller than the hero part and the toolbar is scrolled, the placeholder (as tall as these) is not.
        mRestPositionSet = true;
        behavior.setTopAndBottomOffset(Math.min(0, mHeroHeight + mActionBarHeight - mPictureHeight));
        appBarLayout.setLayoutParams(params);
    }

    private int visibleColorTagHeight() {
        return mColorTag.getVisibility() == View.VISIBLE ? mColorTagHeight : 0;
    }

    private static void setHeight(View view, int height) {
        ViewGroup.LayoutParams params = view.getLayoutParams();
        params.height = height;
        view.setLayoutParams(params);
    }
}
