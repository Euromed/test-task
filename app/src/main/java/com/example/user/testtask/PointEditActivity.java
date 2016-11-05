package com.example.user.testtask;

import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.View;

public class PointEditActivity extends AppCompatActivity
        implements EditPointCardAdapter.OnCardInteractionListener,
        Point.EventsListener
{

    public static final String EXTRA_POINT = "point";
    public static final String EXTRA_NAME = "point_name";

    public static final String BUNDLE_RECYCLER_LAYOUT = "com.example.user.testtask.pointeditactivity.bundle_recycler_layout";

    private int pointId = -1;
    private String pointName = null;
    private SQLiteDatabase db = null;
    private RecyclerView mRecyclerView = null;
    private EditPointCardAdapter mAdapter = null;
    private Point mPoint = null;

    @Override
    public void notifyError(int msgType, int msgItem, String msg) {

    }

    @Override
    public void notifyImageInserted(int pos) {
        mAdapter.notifyItemInserted(pos + 1);
    }

    @Override
    public void notifyImageChanged(int pos) {
        mAdapter.notifyItemChanged(pos + 1);
    }

    @Override
    public void notifyImageRemoved(int pos) {
        mAdapter.notifyItemRemoved(pos + 1);
    }

    @Override
    public void notifyDataSetChanged() {
        mAdapter.notifyDataSetChanged();
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

        mRecyclerView = (RecyclerView)findViewById(R.id.edit_view);
        PointsDatabaseHelper dbh = new PointsDatabaseHelper(this);
        mPoint = new Point(dbh, this, getResources(), pointId);
        mAdapter = new EditPointCardAdapter(mPoint, this);
        mRecyclerView.setAdapter(mAdapter);
        mPoint.refresh(savedInstanceState);
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
        mPoint.saveState(outState);
    }

    @Override
    public void onImageClick(int image) {
        String url = mPoint.getImageUrl(image - 1);
        Util.startExternalImageViewer(url, this);
    }

    @Override
    public void onStarButtonClick(int image) {
        mPoint.toggleDefaultImage(image - 1);
    }
}
