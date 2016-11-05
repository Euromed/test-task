package com.example.user.testtask;

import android.app.IntentService;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Intent;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.v4.content.LocalBroadcastManager;
import android.util.JsonReader;
import android.util.JsonToken;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.SimpleFormatter;

/* TODO: сделать описание параметров службы и возврата результата через LocalBroadcastManager */

public class PointsDataService extends IntentService {
    private static final String SERVICE_NAME = "PointsDataService";

    public static final String ACTION_LOAD_JSON = "com.example.user.testtask.action.LOAD_JSON";

    public static final String EXTRA_URL = "com.example.user.testtask.extra.URL";
    public static final String EXTRA_FORCE_REPLACE = "com.example.user.testtask.extra.FORCE_REPLACE";

    public static final String BROADCAST_RESULT = "com.example.user.testtask.action.BROADCAST_RESULT";
    public static final String EXTRA_RESULT = "com.example.user.testtask.RESULT";

    public static final int RESULT_OTHER_ERROR = -1;
    public static final int RESULT_IO_ERROR = -2;
    public static final int RESULT_PARSE_ERROR = -3;

    public PointsDataService() {
        super(SERVICE_NAME);
    }

    public static void startActionLoadJson(Context context, String url, boolean forceReplace) {
        Intent intent = new Intent(context, PointsDataService.class);
        intent.setAction(ACTION_LOAD_JSON);
        intent.putExtra(EXTRA_URL, url);
        intent.putExtra(EXTRA_FORCE_REPLACE, forceReplace);
        context.startService(intent);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_LOAD_JSON.equals(action)) {
                final String url = intent.getStringExtra(EXTRA_URL);
                final boolean forceReplace = intent.getBooleanExtra(EXTRA_FORCE_REPLACE, false);
                handleActionLoadJson(url, forceReplace);
            }
        }
    }

    private void handleActionLoadJson(String url, boolean forceReplace) {
        int loadedItemCount = 0;
        SQLiteOpenHelper pointsDatabaseHelper = new PointsDatabaseHelper(this);
        SQLiteDatabase db = null;
        for (int i = 0; db == null && i < 10; ++i) {
            try {
                db = pointsDatabaseHelper.getWritableDatabase();
            }
            catch (SQLiteException e) {
                Log.d(SERVICE_NAME, "Error during open SQLiteDatabase - " + e.getMessage());
            }
            try {
                Thread.sleep(100);
            }
            catch (InterruptedException e) {
            }
        }
        if (db == null) {
            loadedItemCount = RESULT_OTHER_ERROR;
        }
        else try {
            loadedItemCount = downloadJson(url, forceReplace, db);
        }
        catch (SQLiteException e) {
            loadedItemCount = RESULT_OTHER_ERROR;
        }
        catch (IOException e) {
            loadedItemCount = RESULT_IO_ERROR;
        }
        catch (ParseException e) {
            loadedItemCount = RESULT_PARSE_ERROR;
        }
        Intent resultIntent = new Intent(BROADCAST_RESULT);
        resultIntent.putExtra(EXTRA_RESULT, loadedItemCount);
        LocalBroadcastManager.getInstance(this).sendBroadcast(resultIntent);
    }

    private int downloadJson(String jsonUrl, boolean forceReplace, SQLiteDatabase db)
            throws IOException, SQLiteException, ParseException {
        InputStream is = null;
        int loadedItemCount = 0;

        try {
            URL url = new URL(jsonUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(10000 /* milliseconds */);
            conn.setConnectTimeout(15000 /* milliseconds */);
            conn.setRequestMethod("GET");
            conn.setDoInput(true);
            conn.connect();
            int response = conn.getResponseCode();
            Log.d(SERVICE_NAME, "The response for JSON URL is: " + response);
            is = conn.getInputStream();

            loadedItemCount = readJsonStream(is, forceReplace, db);
        } finally {
            if (is != null) {
                is.close();
            }
        }
        return loadedItemCount;
    }

    private int readJsonStream(InputStream in, boolean forceReplace, SQLiteDatabase db)
            throws IOException, ParseException {
        int loadedItemCount = 0;
        JsonReader reader = new JsonReader(new InputStreamReader(in, "UTF-8"));
        try {
            loadedItemCount = readPlaces(reader, forceReplace, db);
        } finally {
            reader.close();
        }
        return loadedItemCount;
    }

    private int readPlaces(JsonReader reader, boolean forceReplace, SQLiteDatabase db)
            throws IOException, ParseException {
        int loadedItemCount = 0;
        db.beginTransactionNonExclusive();
        try {
            reader.beginObject();
            while (reader.hasNext()) {
                String name = reader.nextName();
                if (name.equals("places")) {
                    reader.beginArray();
                    while (reader.hasNext()) {
                        loadedItemCount += readPlace(reader, forceReplace, db);
                    }
                    reader.endArray();
                }
            }
            reader.endObject();
            db.setTransactionSuccessful();
        }
        finally {
            db.endTransaction();
        }
        return loadedItemCount;
    }

    private int readPlace(JsonReader reader, boolean forceReplace, SQLiteDatabase db) throws IOException, ParseException {
        double latitude = 1000;
        double longitude = 1000;
        String description = null;
        String image = null;
        Date lastVisited = null;
        int pointId = -1;

        // Чтение объекта
        reader.beginObject();
        while (reader.hasNext()) {
            String name = reader.nextName();
            if (name.equals("latitude")) {
                latitude = reader.nextDouble();
            }
            else if (name.equals("longtitude")) {
                longitude = reader.nextDouble();
            }
            else if (name.equals("text")) {
                description = reader.nextString().trim();
            }
            else if (name.equals("image") && reader.peek() != JsonToken.NULL) {
                image = reader.nextString().trim();
            }
            else if (name.equals("lastVisited")) {
                // Thu Apr 29 05:25:29 +0400 2010
                SimpleDateFormat formatter = new SimpleDateFormat("EEE MMM d HH:mm:ss zzz yyyy");
                lastVisited = formatter.parse(reader.nextString());
            }
            else {
                reader.skipValue();
            }
        }
        reader.endObject();

        // Проверка целостности и уникальности
        if (latitude <= -180 || latitude > 180 || longitude <= -180 || longitude > 180) {
            throw new IOException("Geo coordinates is out of range");
        }
        if (lastVisited == null || description == null) {
            return 0;
        }

        Cursor pointCursor = db.query(PointsDatabaseHelper.tblPoints,
                new String[] {PointsDatabaseHelper.fldPointId},
                "latitude = ? and longitude = ?",
                new String[] {Double.toString(latitude), Double.toString(longitude)},
                null, null, null);
        if (pointCursor.getCount() > 0) {
            if (!forceReplace) {
                return 0;
            }
            pointCursor.moveToFirst();
            pointId = pointCursor.getInt(0);
        }

        // Сохранение в БД
        ContentValues values = new ContentValues(4);
        values.put(PointsDatabaseHelper.fldLatitude, latitude);
        values.put(PointsDatabaseHelper.fldLongitude, longitude);
        values.put(PointsDatabaseHelper.fldName, description);
        SimpleDateFormat formatter = new SimpleDateFormat("yyy-MM-dd HH:mm:sszzz");
        values.put(PointsDatabaseHelper.fldLastVisited, formatter.format(lastVisited));
        if (pointId == -1) {
            pointId = (int)db.insertOrThrow(PointsDatabaseHelper.tblPoints, null, values);
        }
        else {
            db.update(PointsDatabaseHelper.tblPoints, values, "point_id = ?", new String[] {Integer.toString(pointId)});
            db.delete(PointsDatabaseHelper.tblImages, "point = ?", new String[] {Integer.toString(pointId)});
        }
        if (image != null) {
            values.clear();
            values.put(PointsDatabaseHelper.fldPoint, pointId);
            values.put(PointsDatabaseHelper.fldImage, image);
            db.insert(PointsDatabaseHelper.tblImages, null, values);
        }
        return 1;
    }
}
