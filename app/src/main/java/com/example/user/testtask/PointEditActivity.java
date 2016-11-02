package com.example.user.testtask;

import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Parcelable;
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

    public static final String BUNDLE_RECYCLER_LAYOUT = "com.example.user.testtask.pointeditactivity.bundle_recycler_layout";

    private int pointId = -1;
    private String pointName = null;
    private SQLiteDatabase db = null;
    private RecyclerView recyclerView = null;

    private class AsyncResult {
        Point point;
        EditPointCardAdapter adapter;
    }

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
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

        recyclerView = (RecyclerView)findViewById(R.id.edit_view);
        PointsDatabaseHelper dbh = new PointsDatabaseHelper(this);
        Point point = new Point(dbh, this, getResources(), pointId);
        EditPointCardAdapter cardAdapter = new EditPointCardAdapter(point, this);
        point.setAdapter(cardAdapter);
        recyclerView.setAdapter(cardAdapter);
        point.refresh(savedInstanceState);
        /*final PointEditActivity activity = this;
        AsyncTask<Void, Void, AsyncResult> task = new AsyncTask<Void, Void, AsyncResult>() {
            Parcelable savedRecyclerLayoutState = null;

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                if (savedInstanceState != null) {
                    savedRecyclerLayoutState = savedInstanceState.getParcelable(BUNDLE_RECYCLER_LAYOUT);
                }
            }

            @Override
            protected AsyncResult doInBackground(Void... params) {
                AsyncResult asr = new AsyncResult();
                if (db == null) {
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
                //RecyclerView rv = (RecyclerView)activity.findViewById(R.id.edit_view);
                recyclerView.setAdapter(asr.adapter);
                if (savedRecyclerLayoutState != null) {
                    recyclerView.getLayoutManager().onRestoreInstanceState(savedRecyclerLayoutState);
                }
            }
        };
        task.execute();*/
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_point_edit, menu);
        return true;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(EXTRA_POINT, pointId);
        outState.putString(EXTRA_NAME, getTitle().toString());
    }

/*    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        Parcelable savedRecyclerLayoutState = savedInstanceState.getParcelable(BUNDLE_RECYCLER_LAYOUT);
        recyclerView.getLayoutManager().onRestoreInstanceState(savedRecyclerLayoutState);
    }*/

    @Override
    public void onImageClick(int image) {

    }

    @Override
    public void onStarButtonClick(int image) {

    }
}
