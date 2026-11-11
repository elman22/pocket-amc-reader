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

package com.holdingscythe.pocketamcreader.views;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.text.Layout;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.holdingscythe.pocketamcreader.R;

import androidx.annotation.Nullable;

/**
 * Text which is collapsed to a number of lines, with a button (and a click on the text) to expand and collapse it.
 * <p>
 * The layout of the view has to contain a TextView with id {@code expandable_text} and an ImageButton with id
 * {@code expand_collapse}. The button is hidden when the whole text fits to the collapsed lines.
 * Attributes: maxCollapsedLines, animDuration (ms), animAlphaStart, expandDrawable, collapseDrawable and
 * expandToggleOnTextClick.
 */
public class ExpandableTextView extends LinearLayout {

    private static final int DEFAULT_MAX_COLLAPSED_LINES = 8;
    private static final int DEFAULT_ANIM_DURATION = 300;
    private static final float DEFAULT_ANIM_ALPHA_START = 0.7f;

    private TextView mTextView;
    private ImageButton mButton;

    private final int mMaxCollapsedLines;
    private final int mAnimDuration;
    private final float mAnimAlphaStart;
    private final Drawable mExpandDrawable;
    private final Drawable mCollapseDrawable;
    private final boolean mToggleOnTextClick;

    private boolean mCollapsed = true;
    private boolean mExpandable = false;
    private boolean mRelayout = false;
    private int mCollapsedTextHeight;
    private int mExpandedTextHeight;
    private ValueAnimator mAnimator;

    public ExpandableTextView(Context context) {
        this(context, null);
    }

    public ExpandableTextView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ExpandableTextView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setOrientation(VERTICAL);

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.ExpandableTextView, defStyleAttr, 0);
        try {
            mMaxCollapsedLines = a.getInt(R.styleable.ExpandableTextView_maxCollapsedLines,
                    DEFAULT_MAX_COLLAPSED_LINES);
            mAnimDuration = a.getInt(R.styleable.ExpandableTextView_animDuration, DEFAULT_ANIM_DURATION);
            mAnimAlphaStart = a.getFloat(R.styleable.ExpandableTextView_animAlphaStart, DEFAULT_ANIM_ALPHA_START);
            mExpandDrawable = a.getDrawable(R.styleable.ExpandableTextView_expandDrawable);
            mCollapseDrawable = a.getDrawable(R.styleable.ExpandableTextView_collapseDrawable);
            mToggleOnTextClick = a.getBoolean(R.styleable.ExpandableTextView_expandToggleOnTextClick, true);
        } finally {
            a.recycle();
        }
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        mTextView = findViewById(R.id.expandable_text);
        mButton = findViewById(R.id.expand_collapse);
        if (mTextView == null || mButton == null) {
            throw new IllegalStateException(
                    "ExpandableTextView needs views with ids expandable_text and expand_collapse");
        }

        mTextView.setMaxLines(mMaxCollapsedLines);
        mButton.setImageDrawable(mCollapsed ? mExpandDrawable : mCollapseDrawable);
        mButton.setOnClickListener(v -> toggle());
        if (mToggleOnTextClick) {
            mTextView.setOnClickListener(v -> toggle());
        }
    }

    /**
     * Set the text, it is measured again to find out whether it has to be collapsed. An empty text hides the view.
     */
    public void setText(@Nullable CharSequence text) {
        mRelayout = true;
        mTextView.setText(text);
        setVisibility(TextUtils.isEmpty(text) ? GONE : VISIBLE);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (!mRelayout || getVisibility() == GONE) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
            return;
        }
        mRelayout = false;

        // Measure the text without any limit to find out whether it needs to be collapsed
        mButton.setVisibility(GONE);
        mTextView.setMaxLines(Integer.MAX_VALUE);
        mTextView.setMaxHeight(Integer.MAX_VALUE);
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        Layout layout = mTextView.getLayout();
        mExpandable = layout != null && layout.getLineCount() > mMaxCollapsedLines;
        if (!mExpandable) {
            return;
        }

        mExpandedTextHeight = textHeight(layout, Integer.MAX_VALUE);
        mCollapsedTextHeight = textHeight(layout, mMaxCollapsedLines);

        mButton.setVisibility(VISIBLE);
        if (mCollapsed) {
            mTextView.setMaxLines(mMaxCollapsedLines);
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    private int textHeight(Layout layout, int lines) {
        int count = Math.min(lines, layout.getLineCount());
        return layout.getLineTop(count) + mTextView.getCompoundPaddingTop() + mTextView.getCompoundPaddingBottom();
    }

    private void toggle() {
        if (!mExpandable || (mAnimator != null && mAnimator.isRunning())) {
            return;
        }

        mCollapsed = !mCollapsed;
        mButton.setImageDrawable(mCollapsed ? mExpandDrawable : mCollapseDrawable);

        // The limit of lines must not cut the text while it grows, the height limit is animated instead
        int from = mTextView.getHeight();
        int to = mCollapsed ? mCollapsedTextHeight : mExpandedTextHeight;
        mTextView.setMaxLines(Integer.MAX_VALUE);
        mTextView.setMaxHeight(from);

        mAnimator = ValueAnimator.ofInt(from, to);
        mAnimator.setDuration(mAnimDuration);
        mAnimator.addUpdateListener(animation -> {
            mTextView.setMaxHeight((int) animation.getAnimatedValue());
            mTextView.setAlpha(mAnimAlphaStart + animation.getAnimatedFraction() * (1f - mAnimAlphaStart));
        });
        mAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                finishAnimation();
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                finishAnimation();
            }
        });
        mAnimator.start();
    }

    private void finishAnimation() {
        mTextView.setAlpha(1f);
        mTextView.setMaxHeight(Integer.MAX_VALUE);
        if (mCollapsed) {
            mTextView.setMaxLines(mMaxCollapsedLines);
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        if (mAnimator != null) {
            mAnimator.cancel();
        }
        super.onDetachedFromWindow();
    }
}
