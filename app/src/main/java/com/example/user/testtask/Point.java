package com.example.user.testtask;

import android.content.ContentValues;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.v7.widget.RecyclerView;
import android.widget.ImageView;

import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

/**
 * Created by const on 13.09.2016.
 */
public class Point {
    public final int MSG_ERROR      = 1;
    public final int MSG_WARNING    = 2;
    public final int MSG_INFO       = 3;

    public final int ITEM_WHOLE_POINT   = 0;
    public final int ITEM_NAME = 1;
    public final int ITEM_LATITUDE      = 2;
    public final int ITEM_LONGITUDE     = 3;
    public final int ITEM_LAST_VISITED  = 4;

    public static final String BUNDLE_POINT_STATE = "com.example.user.testtask.point.state";

    public interface EventsListener {
        void notifyError(int msgType, int msgItem, String msg);
        void notifyImageInserted(int pos);
        void notifyImageChanged(int pos);
        void notifyImageRemoved(int pos);
        void notifyDataSetChanged();
    }

    private static class ImageRow {
        int id = -1;
        String url = null;
    }

    private static class State implements Parcelable {
        int pointId = -1;
        String name = null;
        double latitude = Double.NaN;
        double longitude = Double.NaN;
        Calendar lastVisited = Calendar.getInstance();
        int defaultImage = -1;
        int defaultImageId = -1;
        ArrayList<ImageRow> images = new ArrayList<>();

        String oldName = null;
        double oldLatitude = Double.NaN;
        double oldLongitude = Double.NaN;
        Calendar oldLastVisited = Calendar.getInstance();
        int oldDefaultImage = -1;
        int oldDefaultImageId = -1;
        ArrayList<ImageRow> deletedImages = new ArrayList<>();

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel out, int flags) {
            out.writeInt(pointId);
            out.writeString(name);
            out.writeDouble(latitude);
            out.writeDouble(longitude);
            out.writeSerializable(lastVisited);
            out.writeInt(defaultImage);
            out.writeInt(defaultImageId);
            int count = images.size();
            out.writeInt(count);
            for (ImageRow im : images) {
                out.writeInt(im.id);
                out.writeString(im.url);
            }

            out.writeString(oldName);
            out.writeDouble(oldLatitude);
            out.writeDouble(oldLongitude);
            out.writeSerializable(oldLastVisited);
            out.writeInt(oldDefaultImage);
            out.writeInt(oldDefaultImageId);
            count = deletedImages.size();
            out.writeInt(count);
            for (ImageRow im : deletedImages) {
                out.writeInt(im.id);
                out.writeString(im.url);
            }
        }

        public static final Creator<State> CREATOR = new Creator<State>() {
            @Override
            public State createFromParcel(Parcel in) {
                State rv = new State();
                rv.pointId = in.readInt();
                rv.name = in.readString();
                rv.latitude = in.readDouble();
                rv.longitude = in.readDouble();
                rv.lastVisited = (Calendar)in.readSerializable();
                rv.defaultImage = in.readInt();
                rv.defaultImageId = in.readInt();
                int count = in.readInt();
                rv.images = new ArrayList<>(count);
                for (int i = 0; i < count; ++i) {
                    ImageRow im = new ImageRow();
                    im.id = in.readInt();
                    im.url = in.readString();
                    rv.images.add(im);
                }

                rv.oldName = in.readString();
                rv.oldLatitude = in.readDouble();
                rv.oldLongitude = in.readDouble();
                rv.oldLastVisited = (Calendar)in.readSerializable();
                rv.oldDefaultImage = in.readInt();
                rv.oldDefaultImageId = in.readInt();
                count = in.readInt();
                rv.deletedImages = new ArrayList<>(count);
                for (int i = 0; i < count; ++i) {
                    ImageRow im = new ImageRow();
                    im.id = in.readInt();
                    im.url = in.readString();
                    rv.deletedImages.add(im);
                }
                return rv;
            }

            @Override
            public State[] newArray(int size) {
                return new State[size];
            }
        };
    }

    private State mState = new State();

    private PointsDatabaseHelper mDatabaseHelper;
    private SQLiteDatabase mDB = null;
    private final WeakReference<EventsListener> mEventsListener;
    private final Resources mResources;

    public Point(PointsDatabaseHelper dbh, EventsListener listener, Resources resources) {
        this(dbh, listener, resources, -1);
    }

    public Point(PointsDatabaseHelper dbh, EventsListener listener, Resources resources, int pointId) {
        mDatabaseHelper = dbh;
        mEventsListener = new WeakReference<EventsListener>(listener);
        mResources = resources;
        mState.pointId = pointId;
    }

    public void refresh(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            mState = savedInstanceState.getParcelable(BUNDLE_POINT_STATE);
            return;
        }
        if (mState.pointId == -1) {
            return;
        }
        AsyncTask<Void, Void, Boolean> task = new AsyncTask<Void, Void, Boolean>() {
            @Override
            protected Boolean doInBackground(Void... voids) {
                SQLiteDatabase db = getDatabase();
                if (db == null) {
                    return false;
                }
                Cursor cursor = db.query(PointsDatabaseHelper.tblPoints,
                        new String[] {PointsDatabaseHelper.fldName, PointsDatabaseHelper.fldLatitude,
                                PointsDatabaseHelper.fldLongitude, PointsDatabaseHelper.fldLastVisited,
                                PointsDatabaseHelper.fldDefaultImage},
                        "point_id=?",
                        new String[] {Integer.toString(mState.pointId)},
                        null, null, null);
                if (cursor.getCount() != 1) {
                    mState.pointId = -1;
                    return false;
                }
                cursor.moveToFirst();
                mState.name = cursor.getString(0);
                mState.latitude = cursor.getDouble(1);
                mState.longitude = cursor.getDouble(2);
                SimpleDateFormat sf = new SimpleDateFormat(mResources.getString(R.string.sql_time_format));
                try {
                    mState.lastVisited.setTime(sf.parse(cursor.getString(3)));
                }
                catch (Exception e) {
                }
                mState.defaultImage = -1;
                mState.defaultImageId = cursor.isNull(4) ? -1 : cursor.getInt(4);
                ArrayList<ImageRow> images = mState.images;
                images.clear();
                cursor = db.query(PointsDatabaseHelper.tblImages,
                        new String[] {PointsDatabaseHelper.fldImageId, PointsDatabaseHelper.fldImage},
                        "point = ?",
                        new String[] {Integer.toString(mState.pointId)},
                        null, null, PointsDatabaseHelper.fldImageId);
                cursor.moveToFirst();
                while (!cursor.isAfterLast()) {
                    ImageRow imageRow = new ImageRow();
                    int imageId = cursor.getInt(0);
                    imageRow.id = imageId;
                    imageRow.url = cursor.getString(1);
                    images.add(imageRow);
                    if (imageId == mState.defaultImageId) {
                        mState.defaultImage = images.size() - 1;
                    }
                    cursor.moveToNext();
                }
                return true;
            }

            @Override
            protected void onPostExecute(Boolean hasUpdated) {
                super.onPostExecute(hasUpdated);
                if (hasUpdated) {
                    fillOldValues();
                    notifyUpdateAll();
                }
            }
        };
    }

    void saveState(Bundle bundle) {
        bundle.putParcelable(BUNDLE_POINT_STATE, mState);
    }

    public boolean validate() {
        return validateName() && validateLatitude() &&
               validateLongitude() && validateLastVisited();
    }

    public void save() {
        if (!validate()) {
            return;
        }

        UpdateScript updateScript = getUpdates();
        if (updateScript.size() == 0) {
            return;
        }

        makeUpdates(updateScript);
    }

    private static class UpdateStep {
        final static int OP_INSERT = 1;
        final static int OP_UPDATE = 2;
        final static int OP_INSERT_IMAGE = 3;
        final static int OP_DELETE_IMAGE = 5;
        int operation = 0;
        ImageRow image = null;
        boolean isDefaultImage = false;
    }

    private static class UpdateScript extends ArrayList<UpdateStep> {
    }

    UpdateScript getUpdates() {
        UpdateScript rv = new UpdateScript();

        if (mState.pointId == -1) {
            UpdateStep u = new UpdateStep();
            u.operation = UpdateStep.OP_INSERT;
            rv.add(u);
        }

        for (ImageRow image : mState.deletedImages) {
            UpdateStep u = new UpdateStep();
            u.operation = UpdateStep.OP_DELETE_IMAGE;
            u.image = image;
            rv.add(u);
        }

        for (int i = 0; i < mState.images.size(); ++i) {
            ImageRow image = mState.images.get(i);
            if (image.id == -1) {
                UpdateStep u = new UpdateStep();
                u.operation = UpdateStep.OP_INSERT_IMAGE;
                u.isDefaultImage = i == mState.defaultImage;
                rv.add(u);
            }
        }

        if (mState.pointId != -1 && (
            !mState.name.equals(mState.oldName) ||
            mState.latitude != mState.oldLatitude ||
            mState.longitude != mState.oldLongitude ||
            !mState.lastVisited.equals(mState.oldLastVisited)) ||
            mState.defaultImage != mState.oldDefaultImage ||
            mState.defaultImageId != mState.oldDefaultImageId) {
            UpdateStep u = new UpdateStep();
            u.operation =  UpdateStep.OP_UPDATE;
            rv.add(u);
        }
        return rv;
    }

    void makeUpdates(final UpdateScript updateScript) {
        AsyncTask<Void, Void, Void> task = new AsyncTask<Void, Void, Void>() {
            UpdateScript mUpdateScript = updateScript;
            @Override
            protected Void doInBackground(Void... voids) {
                SQLiteDatabase db = getDatabase();
                if (db == null) {
                    return null;
                }
                db.beginTransaction();
                try {
                    for (UpdateStep u : mUpdateScript) {
                        switch (u.operation) {
                            case UpdateStep.OP_INSERT:
                                makeInsert(db, u);
                                break;
                            case UpdateStep.OP_UPDATE:
                                makeUpdate(db, u);
                                break;
                            case UpdateStep.OP_INSERT_IMAGE:
                                makeInsertImage(db, u);
                                break;
                            case UpdateStep.OP_DELETE_IMAGE:
                                makeDeleteImage(db, u);
                                break;
                        }
                    }
                    db.setTransactionSuccessful();
                }
                finally {
                    db.endTransaction();
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                fillOldValues();
            }
        };
        task.execute();
    }

    private void fillOldValues () {
        mState.oldName = mState.name;
        mState.oldLatitude = mState.latitude;
        mState.oldLongitude = mState.longitude;
        mState.oldLastVisited = (Calendar)mState.lastVisited.clone();
        mState.oldDefaultImage = mState.defaultImage;
        mState.oldDefaultImageId = mState.defaultImageId;
        mState.deletedImages.clear();
    }

    ContentValues createPointValues() {
        ContentValues v = new ContentValues(5);
        v.put(PointsDatabaseHelper.fldName, mState.name);
        v.put(PointsDatabaseHelper.fldLatitude, mState.latitude);
        v.put(PointsDatabaseHelper.fldLongitude, mState.longitude);
        SimpleDateFormat sf = new SimpleDateFormat(mResources.getString(R.string.sql_time_format));
        v.put(PointsDatabaseHelper.fldLastVisited, sf.format(mState.lastVisited));
        final int defaultImageId = mState.defaultImageId;
        if (defaultImageId != -1) {
            v.put(PointsDatabaseHelper.fldDefaultImage, defaultImageId);
        }
        else {
            v.putNull(PointsDatabaseHelper.fldDefaultImage);
        }
        return v;
    }

    void makeInsert(SQLiteDatabase db, UpdateStep u) {
        ContentValues v = createPointValues();
        mState.pointId = (int)db.insertOrThrow(PointsDatabaseHelper.tblPoints, null, v);
    }

    void makeUpdate(SQLiteDatabase db, UpdateStep u) {
        ContentValues v = createPointValues();
        db.update(PointsDatabaseHelper.tblPoints,
                v,
                "id = ?",
                new String[] {Integer.toString(mState.pointId)});
    }

    ContentValues createImageValues(ImageRow image) {
        ContentValues v = new ContentValues(5);
        v.put(PointsDatabaseHelper.fldPoint, mState.pointId);
        v.put(PointsDatabaseHelper.fldImage, image.url);
        return v;
    }

    void makeInsertImage(SQLiteDatabase db, UpdateStep u) {
        ContentValues v = createImageValues(u.image);
        int imageId = (int)db.insertOrThrow(PointsDatabaseHelper.tblImages, null, v);
        u.image.id = imageId;
        if (u.isDefaultImage) {
            mState.defaultImageId = imageId;
        }
    }

    void makeDeleteImage(SQLiteDatabase db, UpdateStep u) {
        db.delete(PointsDatabaseHelper.tblImages,
                "image_id = ?",
                new String[] {Integer.toString(u.image.id)});
    }

    public String getName() {
        return mState.name;
    }

    public void setName(String name) {
        mState.name = name.trim();
        if (validateName()) {
            checkNameDuplicate(name, mState.pointId);
        }
    }
    
    public boolean validateName(){
        if (mState.name == null || mState.name.length() == 0) {
            reportError(MSG_ERROR, ITEM_NAME, R.string.error_point_name_not_set);
            return false;
        }
        return true;
    }

    public double getLatitude() {
        return mState.latitude;
    }

    public void setLatitude(double latitude) {
        mState.latitude = latitude;
        if (validateLatitude()) {
            checkPointDuplicate(latitude, mState.longitude, mState.pointId);
        }
    }
    
    public boolean validateLatitude() {
        if (Double.isNaN(mState.latitude)) {
            reportError(MSG_ERROR, ITEM_LATITUDE, R.string.error_latitude_format);
            return false;
        }
        if (mState.latitude < -90 || mState.latitude > 90) {
            reportError(MSG_ERROR, ITEM_LATITUDE, R.string.error_latitude_out_of_range);
            return false;
        }
        return true;
    }

    public double getLongitude() {
        return mState.longitude;
    }

    public void setLongitude(double longitude) {
        mState.longitude = longitude;
        if (validateLongitude()) {
            checkPointDuplicate(mState.latitude, longitude, mState.pointId);
        }
    }

    public boolean validateLongitude() {
        if (Double.isNaN(mState.longitude)) {
            reportError(MSG_ERROR, ITEM_LONGITUDE, R.string.error_longitude_format);
            return false;
        }
        if (mState.longitude < -90 || mState.longitude > 90) {
            reportError(MSG_ERROR, ITEM_LONGITUDE, R.string.error_longitude_out_of_range);
            return false;
        }
        return true;
    }

    public Calendar getLastVisited() {
        return mState.lastVisited;
    }

    public void setLastVisited(Calendar lastVisited) {
        mState.lastVisited = lastVisited;
    }

    public boolean validateLastVisited () {
        if (!mState.lastVisited.isLenient()) {
            reportError(MSG_ERROR, ITEM_LAST_VISITED, R.string.error_last_visited_illegal);
            return false;
        }
        if (mState.lastVisited.getTimeInMillis() == 0) {
            reportError(MSG_ERROR, ITEM_LAST_VISITED, R.string.error_last_visited_format);
            return false;
        }
        return true;
    }

    public String getImageUrl(int pos) {
        return mState.images.get(pos).url;
    }

    public int getDefaultImage() {
        return mState.defaultImage;
    }

    public void setDefaultImage(int defaultImage) {
        mState.defaultImage = defaultImage;
        mState.defaultImageId = mState.images.get(defaultImage).id;
    }

    public int getImagesCount() {
        return mState.images.size();
    }

    public void addImage(String url) {
        if (url == null || url.trim().length() == 0) {
            return;
        }
        for (ImageRow imageRow : mState.images) {
            if (imageRow.url.equalsIgnoreCase(url)) {
                return;
            }
        }
        ImageRow imageRow = new ImageRow();
        imageRow.url = url;
        mState.images.add(imageRow);
        notifyAddImage(mState.images.size() - 1);
    }

    public void deleteImage(int pos) {
        mState.deletedImages.add(mState.images.get(pos));
        mState.images.remove(pos);
        if (mState.defaultImage == pos) {
            mState.defaultImage = -1;
            mState.defaultImageId = -1;
        }
        else if (mState.defaultImage > pos){
            --mState.defaultImage;
        }
        notifyDeleteImage(pos);
    }

    public boolean getDefaultImage(int pos) {
        return mState.defaultImage == pos;
    }

    public void setDefaultImage(int pos, boolean setDefault) {
        int oldDefaultImage = mState.defaultImage;
        if (setDefault) {
            mState.defaultImage = pos;
            mState.defaultImageId = mState.images.get(pos).id;
        }
        else if (mState.defaultImage == pos){
            mState.defaultImage = -1;
            mState.defaultImageId = -1;
        }
        if (oldDefaultImage == mState.defaultImage) {
            return;
        }
        if (oldDefaultImage != -1) {
            notifyUpdateImage(oldDefaultImage + 1);
        }
        if (mState.defaultImage != -1) {
            notifyUpdateImage(mState.defaultImage + 1);
        }
    }

    public void toggleDefaultImage(int pos) {
        int oldDefaultImage = mState.defaultImage;
        if (pos == oldDefaultImage) {
            mState.defaultImage = -1;
            mState.defaultImageId = -1;
        }
        else {
            mState.defaultImage = pos;
            mState.defaultImageId = mState.images.get(pos).id;
        }
        if (oldDefaultImage != -1) {
            notifyUpdateImage(oldDefaultImage + 1);
        }
        if (mState.defaultImage != -1) {
            notifyUpdateImage(mState.defaultImage + 1);
        }
    }

    private EventsListener getEventsListener() {
        if (mEventsListener == null) {
            return null;
        }
        return mEventsListener.get();
    }
    private void reportError(int msgType, int itemType, int msgRes) {
        EventsListener listener = getEventsListener();
        if (listener == null) {
            return;
        }
        String msg = mResources.getString(msgRes);
        listener.notifyError(msgType, itemType, msg);
    }

    private SQLiteDatabase getDatabase() {
        if (mDB == null) {
            mDB = mDatabaseHelper.getWritableDatabase();
        }
        return mDB;
    }

    private void checkNameDuplicate (final String pointName, final int pointId) {
        AsyncTask<Void, Void, Boolean> task = new AsyncTask<Void, Void, Boolean>() {
            @Override
            protected Boolean doInBackground(Void... voids) {
                SQLiteDatabase db = getDatabase();
                Cursor cursor = db.query(PointsDatabaseHelper.tblPoints,
                        new String[] {PointsDatabaseHelper.fldName},
                        "id <> ?",
                        new String[] {Integer.toString(pointId)},
                        null, null, null);
                if (cursor.getCount() == 0) {
                    return false;
                }
                cursor.moveToFirst();
                while (cursor.isAfterLast()) {
                    String name = cursor.getString(0);
                    if (pointName.equalsIgnoreCase(name)) {
                        return true;
                    }
                }
                return false;
            }

            @Override
            protected void onPostExecute(Boolean hasDuplicate) {
                super.onPostExecute(hasDuplicate);
                if (hasDuplicate) {
                    reportError(MSG_WARNING, ITEM_NAME, R.string.warning_point_name_duplicate);
                }
            }
        };
        task.execute();
    }

    private void checkPointDuplicate (final double latitude, final double longitude, final int pointId) {
        AsyncTask<Void, Void, Boolean> task = new AsyncTask<Void, Void, Boolean>() {
            @Override
            protected Boolean doInBackground(Void... voids) {
                SQLiteDatabase db = getDatabase();
                Cursor cursor = db.query(PointsDatabaseHelper.tblPoints,
                        new String[] {PointsDatabaseHelper.fldPointId},
                        "id <> ? and abs(latitude - ?) < 1e-5 and abs(longitude - ?) < 1e-5",
                        new String[] {Integer.toString(pointId), Double.toString(latitude), Double.toString(longitude)},
                        null, null, "1");
                return cursor.getCount() != 0;
            }

            @Override
            protected void onPostExecute(Boolean hasDuplicate) {
                super.onPostExecute(hasDuplicate);
                if (hasDuplicate) {
                    reportError(MSG_WARNING, ITEM_LATITUDE, R.string.warning_same_point_exists);
                }
            }
        };
        task.execute();
    }

    private void notifyAddImage(int pos) {
        EventsListener listener = getEventsListener();
        if (listener == null) {
            return;
        }
        listener.notifyImageInserted(pos);
    }

    private void notifyDeleteImage(int pos) {
        EventsListener listener = getEventsListener();
        if (listener == null) {
            return;
        }
        listener.notifyImageRemoved(pos);
    }

    private void notifyUpdateImage(int pos) {
        EventsListener listener = getEventsListener();
        if (listener == null) {
            return;
        }
        listener.notifyImageChanged(pos);
    }

    private void notifyUpdateAll() {
        EventsListener listener = getEventsListener();
        if (listener == null) {
            return;
        }
        listener.notifyDataSetChanged();
    }

    public void LoadImage(ImageView v, int pos) {
        BitmapWorkerTask.loadBitmap(mState.images.get(pos).url, v);
    }

}
