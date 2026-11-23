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

import android.os.Build;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.ViewTreeObserver;
import android.widget.TextView;

import com.google.android.material.appbar.AppBarLayout;

import androidx.annotation.NonNull;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.widget.NestedScrollView;

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
 * The row under the title (year, length, rating...) is always at the same distance from the top of the screen in
 * the default position, whatever the number of lines of the title is. The title block is at the bottom of the
 * visible app bar, so for a title of more lines the default position scrolls the app bar further by the height of
 * the additional lines: the title grows to the top, over the picture.
 * <p>
 * The app bar reports its offset when its layout is finished; everything is done then. Moving the app bar while
 * the toolbar is still being laid out is overwritten by the layout of the app bar.
 */
class DetailHeader implements AppBarLayout.OnOffsetChangedListener {

    private final View mHeader;
    private final View mSpacer;
    private final View mColorTag;
    private final TextView mTitle;
    private final View mContent;
    private final AppBarLayout mAppBarLayout;
    private final int mPictureHeight;
    private final int mHeroHeight;
    private final int mActionBarHeight;
    private final int mColorTagHeight;

    private boolean mRestPositionSet = false;

    /**
     * @param pictureHeight   height of the whole picture
     * @param heroHeight      height of the part of the picture which is displayed when the movie is opened
     * @param actionBarHeight height of the toolbar
     * @param title           the title of the movie (it can have more lines)
     * @param content         the scrolling content under the app bar
     * @param colorTag        the line of the color tag (it is visible only when the movie has a color tag)
     * @param colorTagHeight  height of the line when it is visible
     */
    DetailHeader(@NonNull AppBarLayout appBarLayout, @NonNull View header, @NonNull View spacer,
                 @NonNull TextView title, @NonNull View content, @NonNull View colorTag, int pictureHeight, int heroHeight, int actionBarHeight,
                 int colorTagHeight) {
        mHeader = header;
        mSpacer = spacer;
        mColorTag = colorTag;
        mTitle = title;
        mContent = content;
        mAppBarLayout = appBarLayout;
        mPictureHeight = pictureHeight;
        mHeroHeight = heroHeight;
        mActionBarHeight = actionBarHeight;
        mColorTagHeight = colorTagHeight;

        setHeight(mSpacer, Math.max(0, pictureHeight - visibleColorTagHeight()));

        // Called when the layout of the app bar is finished (also when it was laid out because of the window insets)
        appBarLayout.addOnOffsetChangedListener(this);

        // The content must always start where the app bar ends (see keepContentUnderAppBar)
        final ViewTreeObserver.OnPreDrawListener preDraw = () -> {
            keepContentUnderAppBar();
            return true;
        };
        appBarLayout.addOnAttachStateChangeListener(new View.OnAttachStateChangeListener() {
            @Override
            public void onViewAttachedToWindow(@NonNull View v) {
                v.getViewTreeObserver().addOnPreDrawListener(preDraw);
            }

            @Override
            public void onViewDetachedFromWindow(@NonNull View v) {
                v.getViewTreeObserver().removeOnPreDrawListener(preDraw);
            }
        });
        if (appBarLayout.isAttachedToWindow()) {
            appBarLayout.getViewTreeObserver().addOnPreDrawListener(preDraw);
        }
    }

    /**
     * The scrolling content is placed under the app bar by the behavior of the coordinator layout, which follows
     * the app bar when it moves. The default position is set directly (not by scrolling) and a page of a pager is
     * laid out and prepared before it is displayed, so the content may stay where it was placed for another
     * position of the app bar: the boxes under the title block are then partly hidden under it. The content
     * starts where the app bar ends, when it does not, the coordinator layout is asked to place it again.
     */
    private void keepContentUnderAppBar() {
        // The content is scrolled only when the app bar is collapsed (the app bar takes the scrolling first).
        // Content scrolled under an app bar which is not collapsed is a state which should not exist, e.g. the
        // scroll position was restored for a page which was recreated: show the content from its start.
        int range = mAppBarLayout.getTotalScrollRange();
        if (range > 0 && mContent instanceof NestedScrollView && mContent.getScrollY() != 0
                && mAppBarLayout.getLayoutParams() instanceof CoordinatorLayout.LayoutParams params
                && params.getBehavior() instanceof AppBarLayout.Behavior behavior
                && behavior.getTopAndBottomOffset() > -range) {
            mContent.scrollTo(0, 0);
        }

        if (mContent.getTop() == mAppBarLayout.getBottom()) {
            return;
        }
        ViewParent parent = mAppBarLayout.getParent();
        if (parent instanceof CoordinatorLayout coordinatorLayout) {
            coordinatorLayout.dispatchDependentViewsChanged(mAppBarLayout);
        }
    }

    @Override
    public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
        keepContentUnderAppBar();

        // The title block starts where the picture ends (the color tag line, if there is one, covers its bottom
        // edge). The change lays out the app bar again, the rest is done then.
        int spacerHeight = Math.max(0, mPictureHeight - mHeader.getTop() - visibleColorTagHeight());
        if (mSpacer.getLayoutParams().height != spacerHeight) {
            setHeight(mSpacer, spacerHeight);
            return;
        }

        // Default position of the app bar, set once. The title must be laid out, the position depends on its lines.
        if (mRestPositionSet || mTitle.getLayout() == null) {
            return;
        }

        CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams) appBarLayout.getLayoutParams();
        if (!(params.getBehavior() instanceof AppBarLayout.Behavior behavior)) {
            return;
        }

        // The visible part of the app bar is the hero height, the toolbar and the title block. A picture which is
        // taller than the hero part and the toolbar is scrolled, the placeholder (as tall as these) is not.
        mRestPositionSet = true;
        behavior.setTopAndBottomOffset(getRestOffset());
        appBarLayout.setLayoutParams(params);
        keepContentUnderAppBar();
    }

    /**
     * Offset of the app bar in its default position (0 or negative). The visible part of the app bar has the same
     * height for any title: the app bar is scrolled further by the height of the lines of the title after the first
     * one.
     *
     * @return the offset, it is not known before the title is laid out (the title is taken as one line then)
     */
    int getRestOffset() {
        int offset = Math.min(0, mHeroHeight + mActionBarHeight - mPictureHeight) - additionalTitleHeight();
        int range = mAppBarLayout.getTotalScrollRange();
        return range > 0 ? Math.max(-range, offset) : offset;
    }

    /**
     * Height which the title has more than it would have with one line. It is the difference of the height of the
     * layout of the title and the height of the same text laid out as one line, with the same font, font padding and
     * line spacing, so it is exact to the pixel. Taking the heights of the lines from the layout is not: the first
     * and the last line of a layout contain the font padding, the lines between them do not.
     *
     * @return the height in pixels, 0 for a title of one line or when the title is not laid out yet
     */
    private int additionalTitleHeight() {
        Layout layout = mTitle.getLayout();
        if (layout == null || layout.getLineCount() < 2) {
            return 0;
        }
        CharSequence text = layout.getText();
        StaticLayout.Builder builder = StaticLayout.Builder
                .obtain(text, 0, text.length(), mTitle.getPaint(), layout.getWidth())
                .setIncludePad(mTitle.getIncludeFontPadding())
                .setLineSpacing(mTitle.getLineSpacingExtra(), mTitle.getLineSpacingMultiplier())
                // The maximum of lines is applied only together with an ellipsize, otherwise all lines are laid out
                .setEllipsize(TextUtils.TruncateAt.END)
                .setEllipsizedWidth(layout.getWidth())
                .setMaxLines(1);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            builder.setUseLineSpacingFromFallbacks(mTitle.isFallbackLineSpacing());
        }
        return Math.max(0, layout.getHeight() - builder.build().getHeight());
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
