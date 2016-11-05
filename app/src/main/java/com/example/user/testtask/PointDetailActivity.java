package com.example.user.testtask;

import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

import us.fatehi.pointlocation6709.Angle;
import us.fatehi.pointlocation6709.Latitude;
import us.fatehi.pointlocation6709.Longitude;
import us.fatehi.pointlocation6709.format.PointLocationFormatType;
import us.fatehi.pointlocation6709.format.PointLocationFormatter;

public class PointDetailActivity extends AppCompatActivity
    implements ImageCardAdapter.OnCardInteractionListener,
        Point.EventsListener
{

    public static final String EXTRA_POINT = "point";
    public static final String EXTRA_NAME = "point_name";

    private int mPointId = -1;
    private Point mPoint = null;
    private ImageCardAdapter mAdapter = null;

    @Override
    public void notifyError(int msgType, int msgItem, String msg) {

    }

    @Override
    public void notifyImageInserted(int pos) {
        mAdapter.notifyItemChanged(pos);
    }

    @Override
    public void notifyImageChanged(int pos) {

    }

    @Override
    public void notifyImageRemoved(int pos) {

    }

    @Override
    public void notifyDataSetChanged() {
        double latitude = mPoint.getLatitude();
        double longitude = mPoint.getLongitude();

        ImageView headerMap = (ImageView)findViewById(R.id.header_image);
        MapHelper.LoadStatic(latitude, longitude, headerMap);

        TextView latitudeView = (TextView)findViewById(R.id.latitude_edit);
        String str = "";
        try {
            Latitude val = new Latitude(Angle.fromDegrees(latitude));
            str = PointLocationFormatter.formatLatitude(val, PointLocationFormatType.HUMAN_LONG);
        }
        catch (Exception e) {}
        latitudeView.setText(str);

        TextView longitudeView = (TextView)findViewById(R.id.longitude_edit);
        str = "";
        try {
            Longitude val = new Longitude(Angle.fromDegrees(longitude));
            str = PointLocationFormatter.formatLongitude(val, PointLocationFormatType.HUMAN_LONG);
        }
        catch (Exception e) {}
        longitudeView.setText(str);

        TextView lastVisited = (TextView)findViewById(R.id.last_visited);
        SimpleDateFormat sf = (SimpleDateFormat)SimpleDateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT);
        lastVisited.setText(sf.format(mPoint.getLastVisited()));
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
            mPointId = intent.getIntExtra(EXTRA_POINT, -1);
            setTitle(intent.getStringExtra(EXTRA_NAME));
        }
        else {
            mPointId = savedInstanceState.getInt(EXTRA_POINT);
            setTitle(savedInstanceState.getString(EXTRA_NAME));
        }

        PointsDatabaseHelper dbh = new PointsDatabaseHelper(this);
        mPoint = new Point(dbh, this, getResources(), mPointId);
        mAdapter = new ImageCardAdapter(mPoint, this);
        RecyclerView rv = (RecyclerView)findViewById(R.id.images_view);
        rv.setAdapter(mAdapter);
        mPoint.refresh(savedInstanceState);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_point_detail, menu);
        return (true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();

        switch (itemId) {
            case R.id.action_edit:
                Intent intent = new Intent(this, PointEditActivity.class);
                intent.putExtra(PointEditActivity.EXTRA_POINT, mPointId);
                intent.putExtra(PointEditActivity.EXTRA_NAME, getTitle());
                startActivity(intent);
                return (true);
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mPoint.save();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(EXTRA_POINT, mPointId);
        outState.putString(EXTRA_NAME, getTitle().toString());
        mPoint.saveState(outState);
    }

    @Override
    public void onImageClick(int image) {
        String url = mPoint.getImageUrl(image);
        Util.startExternalImageViewer(url, this);
    }

    @Override
    public void onStarButtonClick(int image) {
        mPoint.toggleDefaultImage(image);
    }
}
