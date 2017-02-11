package com.example.user.testtask;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.StateSet;
import android.view.View;
import android.view.accessibility.AccessibilityEvent;
import android.widget.Checkable;
import android.widget.CheckedTextView;
import android.widget.RelativeLayout;

/**
 * Created by const on 28.01.2017.
 */

public class ListViewRowTimeZone extends RelativeLayout
        implements Checkable {

    boolean mChecked = false;

    private final static int[] STATE_CHECKED = new int[]{android.R.attr.state_checked};

    public ListViewRowTimeZone(Context context) {
        super(context, null);
    }

    public ListViewRowTimeZone(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs, 0);
    }

    @TargetApi(21)
    public ListViewRowTimeZone(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr, 0);
    }

    @TargetApi(21)
    public ListViewRowTimeZone(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    public void setChecked(boolean checked) {
        if (mChecked != checked) {
            mChecked = checked;
            refreshDrawableState();
        }
    }

    @Override
    public boolean isChecked() {
        return mChecked;
    }

    @Override
    public void toggle() {
        setChecked(!mChecked);
    }

    @Override
    protected int[] onCreateDrawableState(int extraSpace) {
        final int[] drawableState = super.onCreateDrawableState(extraSpace + 1);
        if (isChecked()) {
            mergeDrawableStates(drawableState, STATE_CHECKED);
        }
        return drawableState;
    }
}

