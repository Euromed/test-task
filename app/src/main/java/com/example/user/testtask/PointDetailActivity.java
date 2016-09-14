package com.example.user.testtask;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;

public class PointDetailActivity extends AppCompatActivity
    implements ImageCardAdapter.OnCardInteractionListener
{

    public static final String EXTRA_POINT = "point";
    public static final String EXTRA_NAME = "point_name";

    private int pointId = -1;
    private SQLiteDatabase db = null;

    private class AsyncResult {
        Point point;
        ImageCardAdapter adapter;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_point_detail);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        if (savedInstanceState == null) {
            Intent intent = getIntent();
            pointId = intent.getIntExtra(EXTRA_POINT, -1);
            setTitle(intent.getStringExtra(EXTRA_NAME));
        }
        else {
            pointId = savedInstanceState.getInt(EXTRA_POINT);
            setTitle(savedInstanceState.getString(EXTRA_NAME));
        }


        final PointDetailActivity activity = this;
        AsyncTask<Void, Void, AsyncResult> task = new AsyncTask<Void, Void, AsyncResult>() {
            @Override
            protected AsyncResult doInBackground(Void... params) {
                AsyncResult asr = new AsyncResult();
                if (db == null) {
                    PointsDatabaseHelper dbh = new PointsDatabaseHelper(activity);
                    db = dbh.getWritableDatabase();
                }
                asr.point = new Point(db, pointId);
                asr.adapter = new ImageCardAdapter(asr.point, activity);
                asr.adapter.getItemCount();
                return asr;
            }

            @Override
            protected void onPostExecute(AsyncResult asr) {
                super.onPostExecute(asr);
                TextView latitude = (TextView)activity.findViewById(R.id.latitude_edit);
                latitude.setText(asr.point.getLatitude());
                TextView longitude = (TextView)activity.findViewById(R.id.longitude_edit);
                longitude.setText(asr.point.getLatitude());
                TextView lastVisited = (TextView)activity.findViewById(R.id.last_visited);
                lastVisited.setText(asr.point.getLastVisited());
                RecyclerView rv = (RecyclerView)activity.findViewById(R.id.images_view);
                rv.setAdapter(asr.adapter);
            }
        };
        task.execute();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putInt(EXTRA_POINT, pointId);
        outState.putString(EXTRA_NAME, getTitle().toString());
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onImageClick(int image) {

    }

    @Override
    public void onStarButtonClick(int image) {

    }
}
