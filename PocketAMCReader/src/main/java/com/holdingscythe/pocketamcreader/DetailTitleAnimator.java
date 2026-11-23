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

import android.graphics.Color;
import android.graphics.Paint;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.TextView;

import com.google.android.material.appbar.AppBarLayout;

import androidx.annotation.NonNull;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.graphics.ColorUtils;

import java.util.function.IntSupplier;

/**
 * Flows the title of the movie from the title block into the toolbar while the app bar collapses.
 * <p>
 * The title block keeps the real title (it gives the layout its size, and it is what accessibility services
 * read), but its text is made transparent. The same text is drawn by an overlay view, which is a child of the
 * collapsing toolbar placed above the toolbar. The overlay has the same font as the title in the title block
 * and keeps it in the toolbar, only the size, position and color change.
 * <p>
 * The title is shown in all its lines in the title block. While it flows, the number of lines goes down to one
 * and the title is shortened with an ellipsis if it does not fit the toolbar.
 * <p>
 * The overlay is laid out in the top left corner of the collapsing toolbar and moved with translations. The
 * positions are taken from the current geometry (in the window) on every update, so they follow the adjustments
 * of {@link DetailHeader} and the window insets, which can arrive after the first layout. The update is done right
 * before every draw, when the layout and the offsets of the app bar are final.
 * <p>
 * The progress of the collapse is counted from the default position of the app bar (0 = default position,
 * 1 = collapsed), not from the fully expanded app bar. Until {@link #TITLE_ANIM_START_FRACTION} the overlay
 * stays on the title block and scrolls with it. Then it moves to the toolbar and arrives there at the latest when
 * the app bar is collapsed.
 */
class DetailTitleAnimator implements ViewTreeObserver.OnPreDrawListener {

    /**
     * Fraction of the collapse from the default position of the app bar (0 = default position, 1 = collapsed)
     * at which the title starts to flow into the toolbar. Higher values start the animation later. 0 starts it
     * immediately. When the title would reach the toolbar sooner than this, the animation starts earlier, so it
     * always has some distance to run.
     */
    static final float TITLE_ANIM_START_FRACTION = 0.8f;

    /** Smallest fraction of the collapse which the animation takes. */
    private static final float MIN_ANIM_SPAN = 0.1f;

    private final AppBarLayout mAppBarLayout;
    private final View mToolbar;
    private final TextView mTitle;
    private final TextView mOverlay;
    private final IntSupplier mRestOffset;
    private final int mCollapsedColor;
    private final float mExpandedTextSize;
    private final float mCollapsedTextSize;
    private final int mExpandedColor;
    private final float mCollapsedHeight;
    private final int[] mLocation = new int[2];


    /**
     * @param restOffset        offset of the app bar in its default position (0 or negative), it can change until
     *                          the title is laid out
     * @param toolbar           pinned toolbar, the title stops at its start edge, centered vertically
     * @param title             title in the title block (it is made transparent)
     * @param overlay           view which draws the animated title
     * @param collapsedColor    color of the title in the collapsed toolbar
     * @param collapsedTextSize size of the text in the collapsed toolbar, in pixels
     */
    DetailTitleAnimator(@NonNull AppBarLayout appBarLayout, @NonNull IntSupplier restOffset,
                        @NonNull View toolbar, @NonNull TextView title, @NonNull TextView overlay,
                        int collapsedColor, float collapsedTextSize) {
        mAppBarLayout = appBarLayout;
        mRestOffset = restOffset;
        mToolbar = toolbar;
        mTitle = title;
        mOverlay = overlay;
        mCollapsedColor = collapsedColor;
        mCollapsedTextSize = collapsedTextSize;
        mExpandedTextSize = title.getTextSize();
        mExpandedColor = title.getCurrentTextColor();

        // The overlay looks like the title, it only does not have the background and the padding
        overlay.setText(title.getText());
        overlay.setTypeface(title.getTypeface());
        overlay.setTextSize(TypedValue.COMPLEX_UNIT_PX, mExpandedTextSize);
        overlay.setTextColor(mExpandedColor);
        overlay.setPadding(0, 0, 0, 0);

        // Height of one line with the collapsed size (as the text view lays it out, with the font padding). It is
        // measured with the paint, not taken from the layout of the overlay: the layout is behind the current size
        // while the size changes, which moved the title above the center.
        Paint paint = new Paint(overlay.getPaint());
        paint.setTextSize(collapsedTextSize);
        Paint.FontMetricsInt metrics = paint.getFontMetricsInt();
        mCollapsedHeight = metrics.bottom - metrics.top;

        // The title in the title block only keeps the space and the content description
        title.setTextColor(Color.TRANSPARENT);
        title.setShadowLayer(0, 0, 0, Color.TRANSPARENT);

        // The title is placed right before every draw, when the layout and the offsets of the app bar are final.
        // Placing it when the offset changes was not reliable: the collapsing toolbar moves the pinned toolbar in
        // its own listener, which can be called after this one (the view of a page of a pager is not attached to
        // the window yet when the animator is created), so the toolbar was found where it was before the change
        // and the title stopped above the center.
        appBarLayout.addOnAttachStateChangeListener(new View.OnAttachStateChangeListener() {
            @Override
            public void onViewAttachedToWindow(@NonNull View v) {
                v.getViewTreeObserver().addOnPreDrawListener(DetailTitleAnimator.this);
            }

            @Override
            public void onViewDetachedFromWindow(@NonNull View v) {
                v.getViewTreeObserver().removeOnPreDrawListener(DetailTitleAnimator.this);
            }
        });
        if (appBarLayout.isAttachedToWindow()) {
            appBarLayout.getViewTreeObserver().addOnPreDrawListener(this);
        }
    }

    @Override
    public boolean onPreDraw() {
        update();
        return true;
    }

    /**
     * Offset of the app bar, read from its behavior (it is the current one at any time).
     */
    private int currentOffset() {
        if (mAppBarLayout.getLayoutParams() instanceof CoordinatorLayout.LayoutParams params
                && params.getBehavior() instanceof AppBarLayout.Behavior behavior) {
            return behavior.getTopAndBottomOffset();
        }
        return mAppBarLayout.getTop();
    }

    private void update() {
        int range = mAppBarLayout.getTotalScrollRange();
        if (range <= 0 || mTitle.getWidth() == 0 || mToolbar.getHeight() == 0) {
            return;
        }

        // Scroll position of the app bar and the part of it which is collapsed from the default position
        float scroll = Math.min(range, Math.max(0, -currentOffset()));
        float restScroll = Math.min(range, Math.max(0, -mRestOffset.getAsInt()));
        float span = range - restScroll;

        // Resting place of the text of the title in the window
        locate(mTitle);
        float expandedLeft = mLocation[0] + mTitle.getPaddingLeft();
        float expandedTop = mLocation[1] + mTitle.getPaddingTop();
        float expandedWidth = mTitle.getWidth() - mTitle.getPaddingLeft() - mTitle.getPaddingRight();

        // Place of the text in the toolbar (the toolbar is pinned, its place in the window does not change)
        locate(mToolbar);
        float collapsedLeft = mLocation[0] + mToolbar.getPaddingStart();
        float collapsedTop = mLocation[1] + (mToolbar.getHeight() - mCollapsedHeight) / 2f;
        float collapsedWidth = mToolbar.getWidth() - mToolbar.getPaddingStart() - mToolbar.getPaddingEnd();

        float fraction;
        if (span <= 0) {
            fraction = scroll >= range ? 1f : 0f;
        } else {
            float progress = Math.min(1f, Math.max(0f, (scroll - restScroll) / span));

            // The title block scrolls with the app bar. Scroll position at which it would reach the place in the
            // toolbar, as progress from the default position. The animation is finished at the latest when the
            // app bar is collapsed, also when the title block would not get to the toolbar by scrolling.
            float reachProgress = (scroll + expandedTop - collapsedTop - restScroll) / span;
            float start = Math.min(TITLE_ANIM_START_FRACTION, Math.max(0f, reachProgress - MIN_ANIM_SPAN));
            float end = Math.max(start + 0.001f, Math.min(1f, reachProgress));

            fraction = Math.min(1f, Math.max(0f, (progress - start) / (end - start)));
            fraction = fraction * fraction * (3f - 2f * fraction);
        }

        float textSize = lerp(mExpandedTextSize, mCollapsedTextSize, fraction);
        if (mOverlay.getTextSize() != textSize) {
            mOverlay.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize);
        }
        int maxWidth = Math.max(0, Math.round(lerp(expandedWidth, collapsedWidth, fraction)));
        if (mOverlay.getMaxWidth() != maxWidth) {
            mOverlay.setMaxWidth(maxWidth);
        }
        // All lines in the title block, one line (with an ellipsis) in the toolbar
        int maxLines = Math.max(1, Math.round(lerp(Math.max(1, mTitle.getLineCount()), 1, fraction)));
        if (mOverlay.getMaxLines() != maxLines) {
            mOverlay.setMaxLines(maxLines);
        }
        mOverlay.setTextColor(ColorUtils.blendARGB(mExpandedColor, mCollapsedColor, fraction));

        // The overlay is moved from where it is now (translation included) to the place for the fraction
        locate(mOverlay);
        mOverlay.setTranslationX(mOverlay.getTranslationX() + lerp(expandedLeft, collapsedLeft, fraction) - mLocation[0]);
        mOverlay.setTranslationY(mOverlay.getTranslationY() + lerp(expandedTop, collapsedTop, fraction) - mLocation[1]);
    }

    private void locate(View view) {
        view.getLocationInWindow(mLocation);
    }

    private static float lerp(float from, float to, float fraction) {
        return from + (to - from) * fraction;
    }
}
