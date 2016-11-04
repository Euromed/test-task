package com.example.user.testtask;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by const on 22.07.2016.
 */
public class PointsDatabaseHelper extends SQLiteOpenHelper {
    private static final String DB_NAME = "points";
    private static final int DB_VERSION = 1;

    public PointsDatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        upgradeDatabase(db, 0, DB_VERSION);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        upgradeDatabase(db, oldVersion, newVersion);
    }

    public static final String tblPoints = "points";
    public static final String fldPointId = "point_id";
    public static final String fldLatitude = "latitude";
    public static final String fldLongitude = "longitude";
    public static final String fldName = "name";
    public static final String fldDefaultImage = "default_image";
    public static final String fldLastVisited = "last_visited";

    public static final String tblImages = "images";
    public static final String fldImageId = "image_id";
    public static final String fldPoint = "point";
    public static final String fldImage = "image";

    void upgradeDatabase(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 1) {
            db.execSQL("create table points (" +
                    "point_id integer primary key autoincrement," +
                    "latitude real," +
                    "longitude real," +
                    "name text," +
                    "last_visited date, unique(latitude, longitude));");
            db.execSQL("create table images (" +
                    "image_id integer primary key autoincrement," +
                    "point integer," +
                    "image text," +
                    "foreign key(point) references points(point_id) on delete cascade on update cascade);");
            db.execSQL("alter table points " +
                    "add column default_image integer references images(image_id) on delete set null on update cascade;");
        }
        if (newVersion > 1) {
            throw new UnsupportedOperationException("Not yet implemented for first version");
        }
    }
}
