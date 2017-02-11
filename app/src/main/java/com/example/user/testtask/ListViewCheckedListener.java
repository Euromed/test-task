package com.example.user.testtask;

import android.annotation.TargetApi;
import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

/**
 * Created by const on 31.01.2017.
 */

public class ListViewCheckedListener extends ListView {
    public interface OnItemCheckedListener {
        void onItemChecked(AdapterView<?> parent, View view, int position, long id);
    }

    private OnItemCheckedListener mOnItemCheckedListener = null;

    public ListViewCheckedListener(Context context) {
        super(context);
    }

    public ListViewCheckedListener(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ListViewCheckedListener(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @TargetApi(21)
    public ListViewCheckedListener(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public void setOnItemCheckedListener(OnItemCheckedListener listener) {
        mOnItemCheckedListener = listener;
    }

    @Override
    public boolean performItemClick(View view, int position, long id) {
        int oldPosition = getCheckedItemPosition();
        boolean handled = super.performItemClick(view, position, id);
        if (!handled) {
            return (false);
        }
        if (oldPosition != getCheckedItemPosition() && mOnItemCheckedListener != null) {
            mOnItemCheckedListener.onItemChecked(this, view, position, id);
        }
        return (true);
    }
}
