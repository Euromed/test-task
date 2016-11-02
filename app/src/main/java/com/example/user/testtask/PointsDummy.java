package com.example.user.testtask;

import android.database.sqlite.SQLiteDatabase;
import android.widget.ImageView;

/**
 * Created by const on 01.11.2016.
 */

public class PointsDummy extends Points {
    final int mCount;

    public PointsDummy(int count) {
        super(null);
        mCount = count;
    }

    @Override
    public String getCaption(int pos) {
        return "";
    }

    @Override
    public int getPointId(int pos) {
        return -1;
    }

    @Override
    public int getCount() {
        return mCount;
    }

    @Override
    public int getImagesCount(int pos) {
        return 0;
    }

    @Override
    public void LoadImage(ImageView v, int pos, int img) {
    }
}
