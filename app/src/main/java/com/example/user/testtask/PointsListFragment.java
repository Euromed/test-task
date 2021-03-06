package com.example.user.testtask;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link PointsListFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link PointsListFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class PointsListFragment extends Fragment {

//    public static final String BROADCAST_DATABASE_READY = "com.example.user.testtask.action.BROADCAST_DATABASE_READY";
//    public static final String BROADCAST_DATABASE_CHANCHED = "com.example.user.testtask.action.BROADCAST_DATABASE_CHANCHED";

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private static final String BUNDLE_ITEM_COUNT = "PointListFragment.bundle_item_count";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private SQLiteDatabase db = null;
    private PointCardAdapter pointCardAdapter = null;

    private OnFragmentInteractionListener mListener;
    private PointsBroadcastReceiver mBroadcastReceiver;

    private class PointsBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            Log.i("PBR", "Broadcast received - " + intent.getAction());
            switch (intent.getAction()) {
            case PointsDataService.BROADCAST_RESULT:
                if (intent.getIntExtra(PointsDataService.EXTRA_RESULT, 0) > 0) {
                    UpdateRecyclerView();
                }
                break;
//            case BROADCAST_DATABASE_CHANCHED:
//            case BROADCAST_DATABASE_READY:
//                UpdateRecyclerView();
//                break;
            }
        }
    }

    public PointsListFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment PointsListFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static PointsListFragment newInstance(String param1, String param2) {
        PointsListFragment fragment = new PointsListFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        RecyclerView rv = (RecyclerView)inflater.inflate(R.layout.fragment_points_list, container, false);
        Points points = null;
        if (savedInstanceState != null) {
            points = new PointsDummy(savedInstanceState.getInt(BUNDLE_ITEM_COUNT));
        }
        pointCardAdapter = new PointCardAdapter(points, mListener);
        rv.setAdapter(pointCardAdapter);

        mBroadcastReceiver = new PointsBroadcastReceiver();
        LocalBroadcastManager.getInstance(getContext()).registerReceiver(mBroadcastReceiver, new IntentFilter(PointsDataService.BROADCAST_RESULT));
        UpdateRecyclerView();
        return rv;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        LocalBroadcastManager.getInstance(getContext()).unregisterReceiver(mBroadcastReceiver);
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    void UpdateRecyclerView() {
        AsyncTask<Void, Void, Points> task = new AsyncTask<Void, Void, Points>() {
            @Override
            protected Points doInBackground(Void... params) {
                if (db == null) {
                    PointsDatabaseHelper dbh = new PointsDatabaseHelper(getContext());
                    db = dbh.getWritableDatabase();
                }
                Points points = new Points(db);
                points.getCount();
                return (points);
            }

            @Override
            protected void onPostExecute(Points dataProvider) {
                super.onPostExecute(dataProvider);
                pointCardAdapter.changeDataProvider(dataProvider);
            }
        };
        task.execute();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(BUNDLE_ITEM_COUNT, pointCardAdapter.getItemCount());
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
        void onClick(int pointId, String name);
    }
}
