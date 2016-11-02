package com.example.user.testtask;

import android.content.Context;
import android.os.Parcelable;
import android.support.v7.widget.CardView;
import android.util.AttributeSet;
import android.util.Log;

/**
 * Created by const on 01.11.2016.
 */

public class DynCardView extends CardView {
    public DynCardView(Context context) {
        super(context);
        //initialize(context, null, 0);
    }

    public DynCardView(Context context, AttributeSet attrs) {
        super(context, attrs);
        //initialize(context, attrs, 0);
    }

    public DynCardView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        //initialize(context, attrs, defStyleAttr);
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        Log.i("DCV", "Called OnRestoreInstanceState");
        super.onRestoreInstanceState(state);
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        Log.i("DCV", "Called onSaveInstanceState");
        return super.onSaveInstanceState();
    }
}
