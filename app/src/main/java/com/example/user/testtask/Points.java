package com.example.user.testtask;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.widget.ImageView;

/**
 * Created by User on 15.07.2016.
 */
public class Points {
//    private final String[] Captions = {"Point 1", "Point 2", "Point 3"};
//    private final int[][] Images = {{R.drawable.chrysanthemum},
//            {R.drawable.desert, R.drawable.hydrangeas, R.drawable.jellyfish},
//            {R.drawable.koala, R.drawable.lighthouse, R.drawable.penguins, R.drawable.tulips}};

    private SQLiteDatabase db;
    private Cursor pointsCursor = null;
    private Cursor imageCursor = null;
    private int currentImage = -1;

    public Points(SQLiteDatabase dbSource) {
        db = dbSource;
    }

    public String getCaption (int pos) {
        getPointsCursor().moveToPosition(pos);
        return getPointsCursor().getString(1);
    }

    public int getPointId (int pos) {
        getPointsCursor().moveToPosition(pos);
        return getPointsCursor().getInt(0);
    }

    public int getCount() {
        return getPointsCursor().getCount();
    }

    public int getImagesCount(int pos) {
        return getImageCursor(pos).getCount();
    }

    public void LoadImage(ImageView v, int pos, int img) {
        Cursor pointsCursor = getPointsCursor();
        pointsCursor.moveToPosition(pos);
        int defaultImage = 0;
        if (!pointsCursor.isNull(2)) {
            defaultImage = pointsCursor.getInt(2);
        }
        Cursor imageCursor = getImageCursor(pos);
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

    Cursor getPointsCursor() {
        if (pointsCursor == null) {
            pointsCursor = db.query(PointsDatabaseHelper.tblPoints,
                    new String[] {PointsDatabaseHelper.fldPointId, PointsDatabaseHelper.fldName, PointsDatabaseHelper.fldDefaultImage},
                    null, null, null, null, null);
        }
        return pointsCursor;
    }

    Cursor getImageCursor(int pos) {
        if (imageCursor == null || currentImage != pos) {
            getPointsCursor().moveToPosition(pos);
            imageCursor = db.query(PointsDatabaseHelper.tblImages,
                    new String[] {PointsDatabaseHelper.fldImageId, PointsDatabaseHelper.fldImage},
                    "point = ?",
                    new String[] {Integer.toString(getPointsCursor().getInt(0))},
                    null, null, PointsDatabaseHelper.fldImageId);
        }
        return imageCursor;
    }
}
