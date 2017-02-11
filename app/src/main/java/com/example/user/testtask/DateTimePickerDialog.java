package com.example.user.testtask;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.ListView;
import android.widget.TabHost;
import android.widget.TabWidget;
import android.widget.TextView;
import android.widget.TimePicker;

import java.util.*;

/**
 * Created by const on 22.11.2016.
 */

public class DateTimePickerDialog extends DialogFragment
    implements DatePicker.OnDateChangedListener, TimePicker.OnTimeChangedListener, ListViewCheckedListener.OnItemCheckedListener {

    public interface ResultListener {
        void onDateTimeSubmit(Calendar newDateOrNull);
    }

    private static final String BUNDLE_OLD_DATE = "DateTimePickerDialog.ext_old_date";
    private static final String BUNDLE_NEW_DATE = "DateTimePickerDialog.ext_new_date";
    private static final String BUNDLE_FULL_SCREEN = "DateTimePickerDialog.ext_full_screen";
    private static final String PARAM_DATE = "DateTimePickerDialog.param_date";

    private boolean mFullScreen = false;
    private Calendar mOldDate;
    private Calendar mNewDate;

    private ResultListener mResultListener = null;
    TextView mDateTitle = null;
    TextView mTimeTitle = null;
    TextView mTimeZoneTitle = null;

    private static class TimeZoneAdapter extends ArrayAdapter<Util.TimeZoneRow> {
        private final LayoutInflater mInflater;

        TimeZoneAdapter(Context context) {
            super(context, R.layout.list_view_row_time_zone, Util.getTimeZones(context.getResources()));
            mInflater = LayoutInflater.from(context);
        }

        @NonNull
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View rv = convertView;
            if (rv == null) {
                rv = mInflater.inflate(R.layout.list_view_row_time_zone, parent, false);
            }

            Util.TimeZoneRow timeZoneRow = getItem(position);

            TextView textView = (TextView)rv.findViewById(R.id.time_zone_name);
            textView.setText(timeZoneRow.name);

            textView = (TextView)rv.findViewById(R.id.time_zone_offset);
            textView.setText(timeZoneRow.offset);

            return rv;
        }
    }

    public static void showDialog(Calendar date, FragmentManager fragmentManager, Resources r) {
        Bundle arg = new Bundle();
        arg.putSerializable(PARAM_DATE, date);
        DateTimePickerDialog dlg = new DateTimePickerDialog();
        dlg.setArguments(arg);

        dlg.mFullScreen = r.getBoolean(R.bool.open_data_picker_dialog_full_screen);
        dlg.show(fragmentManager, "dateTimePickerDialog");
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable(BUNDLE_FULL_SCREEN, mFullScreen);
        outState.putSerializable(BUNDLE_OLD_DATE, mOldDate);
        outState.putSerializable(BUNDLE_NEW_DATE, mNewDate);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Bundle arguments = getArguments();
        if (arguments != null) {
            mOldDate = (Calendar)arguments.getSerializable(PARAM_DATE);
            mNewDate = (Calendar) mOldDate.clone();
        }

        View view = inflater.inflate(R.layout.dialog_fragment_date_time_picker, container, false);

        DatePicker datePicker = (DatePicker)view.findViewById(R.id.datePicker);
        datePicker.init(mNewDate.get(Calendar.YEAR), mNewDate.get(Calendar.MONTH), mNewDate.get(Calendar.DATE), this);

        TimePicker timePicker = (TimePicker)view.findViewById(R.id.timePicker);
        timePicker.setCurrentHour(mNewDate.get(Calendar.HOUR_OF_DAY));
        timePicker.setCurrentMinute(mNewDate.get(Calendar.MINUTE));
        timePicker.setIs24HourView(Util.is24HourFormat());
        timePicker.setOnTimeChangedListener(this);

        ListViewCheckedListener timeZonesListView = (ListViewCheckedListener) view.findViewById(R.id.time_zone_picker);
        TimeZoneAdapter timeZoneAdapter = new TimeZoneAdapter(getContext());
        timeZonesListView.setAdapter(timeZoneAdapter);
        int offset = mNewDate.getTimeZone().getRawOffset();
        Util.TimeZoneRow timeZoneRow = null;
        for (int i = 0; i < timeZoneAdapter.getCount(); ++i) {
            timeZoneRow = timeZoneAdapter.getItem(i);
            if (timeZoneRow.intOffset == offset) {
                timeZonesListView.setItemChecked(i, true);
                timeZonesListView.setSelection(i);
                break;
            }
        }
        timeZonesListView.setOnItemCheckedListener(this);

        TabHost tabHost = (TabHost)view.findViewById(R.id.tabhost);
        tabHost.setup();
        tabHost.addTab(tabHost.newTabSpec("DatePicker")
            .setContent(R.id.datePicker)
            .setIndicator("DateTitle"));
        tabHost.addTab(tabHost.newTabSpec("TimePicker")
                .setContent(R.id.timePicker)
                .setIndicator("TimeTitle"));
        tabHost.addTab(tabHost.newTabSpec("TimeZoneList")
                .setContent(R.id.time_zone_picker)
                .setIndicator("TimeZoneTitle"));
        TabWidget tabWidget = tabHost.getTabWidget();
        mDateTitle = (TextView)tabWidget.getChildTabViewAt(0).findViewById(android.R.id.title);
        mTimeTitle = (TextView)tabWidget.getChildTabViewAt(1).findViewById(android.R.id.title);
        mTimeZoneTitle = (TextView)tabWidget.getChildTabViewAt(2).findViewById(android.R.id.title);
        mDateTitle.setText(Util.formatDate(mNewDate));
        mTimeTitle.setText(Util.formatTime(mNewDate));
        mTimeZoneTitle.setText(timeZoneRow.offset);

        view.findViewById(R.id.cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Dialog dlg = getDialog();
                if (dlg != null) {
                    dlg.cancel();
                }
            }
        });
        view.findViewById(R.id.ok).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        return view;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            mFullScreen = savedInstanceState.getBoolean(BUNDLE_FULL_SCREEN);
            mOldDate = (Calendar)savedInstanceState.getSerializable(BUNDLE_OLD_DATE);
            mNewDate = (Calendar)savedInstanceState.getSerializable(BUNDLE_NEW_DATE);
        }

        Dialog dialog = null;
        if (mFullScreen) {
            dialog = new Dialog(getContext(), R.style.AppTheme_NoActionBar);
        }
        else {
            dialog = super.onCreateDialog(savedInstanceState);
        }
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        return dialog;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            mResultListener = (ResultListener)context;
        } catch (ClassCastException e) {
            // The activity doesn't implement the interface, throw exception
            throw new ClassCastException(context.toString()
                    + " must implement DateTimePickerDialog.ResultListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mResultListener = null;
    }

    @Override
    public void onDateChanged(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
        mNewDate.set(year, monthOfYear, dayOfMonth);
        mDateTitle.setText(Util.formatDate(mNewDate));
    }

    @Override
    public void onTimeChanged(TimePicker view, int hourOfDay, int minute) {
        mNewDate.set(Calendar.HOUR_OF_DAY, hourOfDay);
        mNewDate.set(Calendar.MINUTE, minute);
        mTimeTitle.setText(Util.formatTime(mNewDate));
    }

    @Override
    public void onItemChecked(AdapterView<?> parent, View view, int position, long id) {
        Util.TimeZoneRow item = (Util.TimeZoneRow)parent.getItemAtPosition(position);
        TimeZone newTimeZone = TimeZone.getTimeZone(item.id);
        mNewDate.setTimeZone(newTimeZone);
        mTimeZoneTitle.setText(item.offset);
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        super.onCancel(dialog);
        mNewDate = (Calendar) mOldDate.clone();
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
        if (mResultListener != null) {
            mResultListener.onDateTimeSubmit(mOldDate.equals(mNewDate) ? null : mNewDate);
        }
    }
}
