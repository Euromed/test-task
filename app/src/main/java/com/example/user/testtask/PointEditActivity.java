package com.example.user.testtask;

import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import us.fatehi.pointlocation6709.Angle;
import us.fatehi.pointlocation6709.Latitude;
import us.fatehi.pointlocation6709.Longitude;
import us.fatehi.pointlocation6709.format.PointLocationFormatType;
import us.fatehi.pointlocation6709.format.PointLocationFormatter;

public class PointEditActivity extends AppCompatActivity
        implements EditPointCardAdapter.OnCardInteractionListener
{

    public static final String EXTRA_POINT = "point";
    public static final String EXTRA_NAME = "point_name";

    private int pointId = -1;
    private String pointName = null;
    private SQLiteDatabase db = null;

    private class AsyncResult {
        Point point;
        EditPointCardAdapter adapter;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_point_edit);
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

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeAsUpIndicator(R.drawable.ic_clear_white_24px);
        actionBar.setDisplayShowTitleEnabled(false);

        if (savedInstanceState == null) {
            Intent intent = getIntent();
            pointId = intent.getIntExtra(EXTRA_POINT, -1);
            pointName = intent.getStringExtra(EXTRA_NAME);
        }
        else {
            pointId = savedInstanceState.getInt(EXTRA_POINT);
            pointName = savedInstanceState.getString(EXTRA_NAME);
        }

        final PointEditActivity activity = this;
        AsyncTask<Void, Void, AsyncResult> task = new AsyncTask<Void, Void, AsyncResult>() {
            @Override
            protected AsyncResult doInBackground(Void... params) {
                AsyncResult asr = new AsyncResult();
                if (db == null) {
                    PointsDatabaseHelper dbh = new PointsDatabaseHelper(activity);
                    db = dbh.getWritableDatabase();
                }
                asr.point = new Point(db, pointId);
                asr.adapter = new EditPointCardAdapter(asr.point, activity);
                asr.adapter.getItemCount();
                return asr;
            }

            @Override
            protected void onPostExecute(AsyncResult asr) {
                super.onPostExecute(asr);

                RecyclerView rv = (RecyclerView)activity.findViewById(R.id.edit_view);
                LinearLayoutManager layoutManager = new LinearLayoutManager(activity);
                rv.setLayoutManager(layoutManager);
                rv.setAdapter(asr.adapter);
            }
        };
        task.execute();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_point_edit, menu);
        return true;
    }

    @Override
    public void onImageClick(int image) {

    }

    @Override
    public void onStarButtonClick(int image) {

    }
}
