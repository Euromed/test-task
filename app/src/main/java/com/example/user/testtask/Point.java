package com.example.user.testtask;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.widget.ImageView;

import us.fatehi.pointlocation6709.Angle;
import us.fatehi.pointlocation6709.Latitude;
import us.fatehi.pointlocation6709.Longitude;
import us.fatehi.pointlocation6709.format.PointLocationFormatType;
import us.fatehi.pointlocation6709.format.PointLocationFormatter;

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
        return getPointCursor().getString(0);
    }

    public double getLatitude () {
        return getPointCursor().getDouble(1);
    }

    public double getLongitude () {
        return getPointCursor().getDouble(2);
    }

    public String getLastVisited () {
        return getPointCursor().getString(3);
    }

    public int getDefaultImage () {
        Cursor pointCursor = getPointCursor();
        int defaultImage = 0;
        if (!pointCursor.isNull(4)) {
            defaultImage = pointCursor.getInt(4);
        }
        return defaultImage;
    }

    public int getCount() {
        return getImageCursor().getCount();
    }

    public void LoadImage(ImageView v, int img) {
        int defaultImage = getDefaultImage();
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
