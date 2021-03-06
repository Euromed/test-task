package com.example.user.testtask;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentTransaction;
import android.text.SpannableString;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import java.util.Calendar;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,
            PointsListFragment.OnFragmentInteractionListener {

    private final int PointsListFragmentIdx = 0;
    private final int MapFragmentIdx = 1;
    private final String CurrentFragmentIdx = "CurrentFragmentIdx";
    private int currentFragment;

//    private PointsDataService points;
//    private boolean pointsDataServiceBound = false;
//    private ServiceConnection pointsDataServiceConnection = new ServiceConnection() {
//        @Override
//        public void onServiceConnected(ComponentName componentName, IBinder binder) {
//            PointsDataService.PointsDataBinder pointsBinder =
//                    (PointsDataService.PointsDataBinder) binder;
//            points = pointsBinder.getPoints();
//            pointsDataServiceBound = true;
//        }
//        @Override
//        public void onServiceDisconnected(ComponentName componentName) {
//            pointsDataServiceBound = false;
//        }
//    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = new Intent(this, PointsDataService.class);
        intent.setAction(PointsDataService.ACTION_LOAD_JSON);
        intent.putExtra(PointsDataService.EXTRA_URL, "http://interesnee.ru/files/android-middle-level-data.json");
        intent.putExtra(PointsDataService.EXTRA_FORCE_REPLACE, true);
        startService(intent);
//        bindService(null, pointsDataServiceConnection, Context.BIND_AUTO_CREATE);

        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
                Calendar cal = Calendar.getInstance();
                DateTimePickerDialog.showDialog(cal, getSupportFragmentManager(), getResources());
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        if (savedInstanceState == null) {
            currentFragment = PointsListFragmentIdx;
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            switch (currentFragment) {
                case PointsListFragmentIdx:
                    PointsListFragment fragment = new PointsListFragment();
                    ft.add(R.id.fragment_container, fragment);
                    break;
                case MapFragmentIdx:
                    //frameLayout.addView(new PointsListFragment());
                    break;
            }
            ft.commit();
        }
        else {
            currentFragment = savedInstanceState.getInt(CurrentFragmentIdx);
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_camera) {
            // Handle the camera action
        } else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(CurrentFragmentIdx, currentFragment);
    }

    public void onFragmentInteraction(Uri uri) {

    }

    @Override
    public void onClick(int pointId, String name) {
        Intent intent = new Intent(this, PointDetailActivity.class);
        intent.putExtra(PointDetailActivity.EXTRA_POINT, pointId);
        intent.putExtra(PointDetailActivity.EXTRA_NAME, name);
        startActivity(intent);
    }
}
