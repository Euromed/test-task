package com.example.user.testtask;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.widget.ImageView;

/**
 * Created by const on 13.09.2016.
 */
public class Point {
    private final SQLiteDatabase db;
    private final int pointId;
    private Cursor pointCursor = null;
    private Cursor imageCursor = null;
    private int currentImage = -1;

    public Point(SQLiteDatabase dbSource, int point) {
        db = dbSource;
        pointId = point;
    }

    public String getCaption () {
        return getPointCursor().getString(1);
    }

    public String getLatitude () {
        return Double.toString(getPointCursor().getDouble(2));
    }

    public String getLongitude () {
        return Double.toString(getPointCursor().getDouble(3));
    }

    public String getLastVisited () {
        return Double.toString(getPointCursor().getDouble(4));
    }

    public int getDefaultImage () {
        return getPointCursor().getInt(5);
    }

    public int getCount() {
        return getImageCursor().getCount();
    }

    public void LoadImage(ImageView v, int img) {
        Cursor pointsCursor = getPointCursor();
        int defaultImage = 0;
        if (!pointsCursor.isNull(5)) {
            defaultImage = pointsCursor.getInt(5);
        }
        Cursor imageCursor = getImageCursor();
        imageCursor.moveToFirst();
        if (defaultImage != 0) {
            do {
                if (imageCursor.getInt(0) == defaultImage) {
                    defaultImage = imageCursor.getPosition();
                    break;
                }
            } while (imageCursor.moveToNext());
            if (imageCursor.isAfterLast()) {
                imageCursor.moveToFirst();
                defaultImage = 0;
            }
        }
        if (img > defaultImage) {
            imageCursor.moveToPosition(img);
        }
        if (img != 0 && img <= defaultImage) {
            imageCursor.moveToPosition(img - 1);
        }
        BitmapWorkerTask.loadBitmap(imageCursor.getString(1), v);
    }

    Cursor getPointCursor() {
        if (pointCursor == null) {
            pointCursor = db.query(PointsDatabaseHelper.tblPoints,
                    new String[] {PointsDatabaseHelper.fldDescription, PointsDatabaseHelper.fldLatitude,
                            PointsDatabaseHelper.fldLongitude, PointsDatabaseHelper.fldLastVisited,
                            PointsDatabaseHelper.fldDefaultIamge},
                    "point_id=?",
                    new String[] {Integer.toString(pointId)},
                    null, null, null);
        }
        pointCursor.moveToFirst();
        return pointCursor;
    }

    Cursor getImageCursor() {
        if (imageCursor == null) {
            imageCursor = db.query(PointsDatabaseHelper.tblImages,
                    new String[] {PointsDatabaseHelper.fldImageId, PointsDatabaseHelper.fldImage},
                    "point = ?",
                    new String[] {Integer.toString(pointId)},
                    null, null, PointsDatabaseHelper.fldImageId);
        }
        return imageCursor;
    }
}
