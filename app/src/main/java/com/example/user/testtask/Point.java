package com.example.user.testtask;

import android.content.ContentValues;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteCursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.provider.ContactsContract;
import android.support.v7.widget.RecyclerView;
import android.widget.ImageView;

import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.logging.SimpleFormatter;

import us.fatehi.pointlocation6709.Angle;
import us.fatehi.pointlocation6709.Latitude;
import us.fatehi.pointlocation6709.Longitude;
import us.fatehi.pointlocation6709.format.PointLocationFormatType;
import us.fatehi.pointlocation6709.format.PointLocationFormatter;

/**
 * Created by const on 13.09.2016.
 */
public class Point {
    public final int MSG_ERROR      = 1;
    public final int MSG_WARNING    = 2;
    public final int MSG_INFO       = 3;

    public final int ITEM_WHOLE_POINT   = 0;
    public final int ITEM_CAPTION       = 1;
    public final int ITEM_LATITUDE      = 2;
    public final int ITEM_LONGITUDE     = 3;
    public final int ITEM_LAST_VISITED  = 4;

    public interface ErrorsListener {
        void notifyError(int msgType, int msgItem, String msg);
    }

    private static class ImageRow {
        //boolean dirty = false;
        int id = -1;
        String url = null;
        //Date timeStamp = null;
        //String cacheId = null;
    }

    private static class State implements Cloneable, Parcelable {
        //private boolean mDirty = false;
        private int mPointId = -1;
        private String mCaption = null;
        private double mLatitude = Double.NaN;
        private double mLongitude = Double.NaN;
        private Calendar mLastVisited = null;
        private int mDefaultImage = -1;
        private int mDefaultImageId = -1;
        private ArrayList<ImageRow> mImages = new ArrayList<ImageRow>();

        private String mOldCaption = null;
        private double mOldLatitude = Double.NaN;
        private double mOldLongitude = Double.NaN;
        private Calendar mOldLastVisited = null;
        private int mOldDefaultImage = -1;
        private int mOldDefaultImageId = -1;
        private ArrayList<ImageRow> mDeletedImages = null;

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel parcel, int i) {

        }

        public static final Creator<State> CREATOR = new Creator<State>() {
            @Override
            public State createFromParcel(Parcel parcel) {
                State rv = new State();
                return null;
            }

            @Override
            public State[] newArray(int size) {
                return new State[size];
            }
        };
    };

    private State state = new State();

    private PointsDatabaseHelper mDatabaseHelper;
    private SQLiteDatabase mDB = null;
    private WeakReference<RecyclerView.Adapter> mAdapterReference = null;
    private final WeakReference<ErrorsListener> mErrorsListener;
    private final Resources mResources;
    //private Cursor mPointCursor = null;
    //private Cursor mImageCursor = null;
    //private int currentImage = -1;

    public Point(PointsDatabaseHelper dbh, ErrorsListener listener, Resources resources) {
        this(dbh, listener, resources, -1);
    }

    public Point(PointsDatabaseHelper dbh, ErrorsListener listener, Resources resources, int pointId) {
        mDatabaseHelper = dbh;
        mErrorsListener = new WeakReference<ErrorsListener>(listener);
        mResources = resources;
        mPointId = pointId;
    }

    public void setAdapter (RecyclerView.Adapter adapter) {
        mAdapterReference = new WeakReference<RecyclerView.Adapter>(adapter);
    }

    public void setReport (RecyclerView.Adapter adapter) {
        mAdapterReference = new WeakReference<RecyclerView.Adapter>(adapter);
    }

    public void refresh(Bundle savedInstanceState) {

    }

    public boolean validate() {
        return validateCaption() && validateLatitude() &&
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

        if (mPointId == -1) {
            UpdateStep u = new UpdateStep();
            u.operation = UpdateStep.OP_INSERT;
            rv.add(u);
        }

        for (ImageRow image : mDeletedImages) {
            UpdateStep u = new UpdateStep();
            u.operation = UpdateStep.OP_DELETE_IMAGE;
            u.image = image;
            rv.add(u);
        }

        for (int i = 0; i < mImages.size(); ++i) {
            ImageRow image = mImages.get(i);
            if (image.id == -1) {
                UpdateStep u = new UpdateStep();
                u.operation = UpdateStep.OP_INSERT_IMAGE;
                u.isDefaultImage = i == mDefaultImage;
                rv.add(u);
            }
        }

        if (mPointId != -1 && (
                !mCaption.equals(mOldCaption) ||
                mLatitude != mOldLatitude ||
                mLongitude != mOldLongitude ||
                !mLastVisited.equals(mOldLastVisited)) ||
            mDefaultImage != mOldDefaultImage ||
            mOldDefaultImageId != mDefaultImageId) {
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
        mOldCaption = mCaption;
        mOldLatitude = mLatitude;
        mOldLongitude = mLongitude;
        mOldLastVisited = (Calendar)mLastVisited.clone();
        mOldDefaultImage = mOldDefaultImage;
        mOldDefaultImageId = mOldDefaultImageId;
        mDeletedImages = new ArrayList<ImageRow>();
    }

    ContentValues createPointValues() {
        ContentValues v = new ContentValues(5);
        v.put(PointsDatabaseHelper.fldDescription, mCaption);
        v.put(PointsDatabaseHelper.fldLatitude, mLatitude);
        v.put(PointsDatabaseHelper.fldLongitude, mLongitude);
        SimpleDateFormat sf = new SimpleDateFormat(mResources.getString(R.string.sql_time_format));
        v.put(PointsDatabaseHelper.fldLastVisited, sf.format(mLastVisited));
        final int defaultImageId = mDefaultImageId;
        if (defaultImageId != -1) {
            v.put(PointsDatabaseHelper.fldDefaultIamge, defaultImageId);
        }
        else {
            v.putNull(PointsDatabaseHelper.fldDefaultIamge);
        }
        return v;
    }

    void makeInsert(SQLiteDatabase db, UpdateStep u) {
        ContentValues v = createPointValues();
        mPointId = (int)db.insertOrThrow(PointsDatabaseHelper.tblPoints, null, v);
    }

    void makeUpdate(SQLiteDatabase db, UpdateStep u) {
        ContentValues v = createPointValues();
        db.update(PointsDatabaseHelper.tblPoints,
                v,
                "id = ?",
                new String[] {Integer.toString(mPointId)});
    }

    ContentValues createImageValues(ImageRow image) {
        ContentValues v = new ContentValues(5);
        v.put(PointsDatabaseHelper.fldPoint, mPointId);
        v.put(PointsDatabaseHelper.fldImage, image.url);
        return v;
    }

    void makeInsertImage(SQLiteDatabase db, UpdateStep u) {
        ContentValues v = createImageValues(u.image);
        int imageId = (int)db.insertOrThrow(PointsDatabaseHelper.tblImages, null, v);
        u.image.id = imageId;
        if (u.isDefaultImage) {
            mDefaultImageId = imageId;
        }
    }

    void makeDeleteImage(SQLiteDatabase db, UpdateStep u) {
        db.delete(PointsDatabaseHelper.tblImages,
                "image_id = ?",
                new String[] {Integer.toString(u.image.id)});
    }

    public String getCaption() {
        return mCaption;
    }

    public void setCaption(String caption) {
        mCaption = caption.trim();
        if (validateCaption()) {
            checkCaptionDuplicate(caption, mPointId);
        }
    }
    
    public boolean validateCaption(){
        if (mCaption == null || mCaption.length() == 0) {
            reportError(MSG_ERROR, ITEM_CAPTION, R.string.error_caption_not_set);
            return false;
        }
        return true;
    }

    public double getLatitude() {
        return mLatitude;
    }

    public void setLatitude(double latitude) {
        mLatitude = latitude;
        if (validateLatitude()) {
            checkPointDuplicate(latitude, mLongitude, mPointId);
        }
    }
    
    public boolean validateLatitude() {
        if (Double.isNaN(mLatitude)) {
            reportError(MSG_ERROR, ITEM_LATITUDE, R.string.error_latitude_format);
            return false;
        }
        if (mLatitude < -90 || mLatitude > 90) {
            reportError(MSG_ERROR, ITEM_LATITUDE, R.string.error_latitude_out_of_range);
            return false;
        }
        return true;
    }

    public double getLongitude() {
        return mLongitude;
    }

    public void setLongitude(double longitude) {
        mLongitude = longitude;
        if (validateLongitude()) {
            checkPointDuplicate(mLatitude, longitude, mPointId);
        }
    }

    public boolean validateLongitude() {
        if (Double.isNaN(mLongitude)) {
            reportError(MSG_ERROR, ITEM_LONGITUDE, R.string.error_longitude_format);
            return false;
        }
        if (mLongitude < -90 || mLongitude > 90) {
            reportError(MSG_ERROR, ITEM_LONGITUDE, R.string.error_longitude_out_of_range);
            return false;
        }
        return true;
    }

    public Calendar getLastVisited() {
        return mLastVisited;
    }

    public void setLastVisited(Calendar lastVisited) {
        mLastVisited = lastVisited;
    }

    public boolean validateLastVisited () {
        if (!mLastVisited.isLenient()) {
            reportError(MSG_ERROR, ITEM_LAST_VISITED, R.string.error_last_visited_illegal);
            return false;
        }
        if (mLastVisited.getTimeInMillis() == 0) {
            reportError(MSG_ERROR, ITEM_LAST_VISITED, R.string.error_last_visited_format);
            return false;
        }
        return true;
    }

    public int getDefaultImage() {
        return mDefaultImage;
    }

    public void setDefaultImage(int defaultImage) {
        mDefaultImage = defaultImage;
        mDefaultImageId = mImages.get(defaultImage).id;
    }

    public void addImage(String url) {
        if (url == null || url.trim().length() == 0) {
            return;
        }
        for (ImageRow imageRow : mImages) {
            if (imageRow.url.equalsIgnoreCase(url)) {
                return;
            }
        }
        ImageRow imageRow = new ImageRow();
        imageRow.url = url;
        mImages.add(imageRow);
        notifyAddCard(mImages.size());
    }

    public void deleteImage(int pos) {
        mImages.remove(--pos);
        if (mDefaultImage == pos) {
            mDefaultImage = -1;
            mOldDefaultImageId = -1;
        }
        else if (mDefaultImage > pos){
            --mDefaultImage;
        }
        notifyDeleteCard(pos);
    }

    public boolean getDefaultImage(int pos) {
        return mDefaultImage == pos - 1;
    }

    public void setDefaultImage(int pos, boolean setDefault) {
        int oldDefaultImage = mDefaultImage;
        --pos;
        if (setDefault) {
            mDefaultImage = pos;
            mOldDefaultImageId = mImages.get(pos).id;
        }
        else if (mDefaultImage == pos){
            mDefaultImage = -1;
            mDefaultImageId = -1;
        }
        if (oldDefaultImage == mDefaultImage) {
            return;
        }
        if (oldDefaultImage != -1) {
            notifyUpdateCard(oldDefaultImage + 1);
        }
        if (mDefaultImage != -1) {
            notifyUpdateCard(mDefaultImage + 1);
        }
    }

    public void toggleDefaultImage(int pos) {
        int oldDefaultImage = mDefaultImage;
        --pos;
        if (pos == oldDefaultImage) {
            mDefaultImage = -1;
            mDefaultImageId = -1;
        }
        else {
            mDefaultImage = pos;
            mOldDefaultImageId = mImages.get(pos).id;
        }
        if (oldDefaultImage != -1) {
            notifyUpdateCard(oldDefaultImage + 1);
        }
        if (mDefaultImage != -1) {
            notifyUpdateCard(mDefaultImage + 1);
        }
    }

    private RecyclerView.Adapter getAdapter() {
        if (mAdapterReference == null) {
            return null;
        }
        return mAdapterReference.get();
    }

    private void reportError(int msgType, int itemType, int msgRes) {
        if (mErrorsListener == null) {
            return;
        }
        ErrorsListener listener = mErrorsListener.get();
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

    private void checkCaptionDuplicate (final String caption, final int pointId) {
        AsyncTask<Void, Void, Boolean> task = new AsyncTask<Void, Void, Boolean>() {
            final String mCaption = caption;
            final int mPointId = pointId;

            @Override
            protected Boolean doInBackground(Void... voids) {
                SQLiteDatabase db = getDatabase();
                Cursor cursor = db.query(PointsDatabaseHelper.tblPoints,
                        new String[] {PointsDatabaseHelper.fldDescription},
                        "id <> ?",
                        new String[] {Integer.toString(pointId)},
                        null, null, null);
                if (cursor.getCount() == 0) {
                    return false;
                }
                cursor.moveToFirst();
                while (cursor.isAfterLast()) {
                    String name = cursor.getString(0);
                    if (mCaption.equalsIgnoreCase(name)) {
                        return true;
                    }
                }
                return false;
            }

            @Override
            protected void onPostExecute(Boolean hasDuplicate) {
                super.onPostExecute(hasDuplicate);
                if (hasDuplicate) {
                    reportError(MSG_WARNING, ITEM_CAPTION, R.string.warning_caption_duplicate);
                }
            }
        };
        task.execute();
    }

    private void checkPointDuplicate (final double latitude, final double longitude, final int pointId) {
        AsyncTask<Void, Void, Boolean> task = new AsyncTask<Void, Void, Boolean>() {
            final double mLatitude = latitude;
            final double mLongitude = longitude;
            final int mPointId = pointId;

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

    private void notifyAddCard(int pos) {
        RecyclerView.Adapter adapter = getAdapter();
        if (adapter == null) {
            return;
        }
        adapter.notifyItemInserted(pos);
    }

    private void notifyDeleteCard(int pos) {
        RecyclerView.Adapter adapter = getAdapter();
        if (adapter == null) {
            return;
        }
        adapter.notifyItemRemoved(pos);
    }

    private void notifyUpdateCard(int pos) {
        RecyclerView.Adapter adapter = getAdapter();
        if (adapter == null) {
            return;
        }
        adapter.notifyItemChanged(pos);
    }
 /*   public String getCaption () {
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
    }*/
}
