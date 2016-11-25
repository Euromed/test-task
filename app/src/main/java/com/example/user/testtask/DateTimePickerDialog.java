package com.example.user.testtask;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
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

import com.ibm.icu.text.SimpleDateFormat;
import com.ibm.icu.util.*;

import java.util.Arrays;

/**
 * Created by const on 22.11.2016.
 */

public class DateTimePickerDialog extends DialogFragment
    implements DatePicker.OnDateChangedListener, TimePicker.OnTimeChangedListener, ListView.OnItemSelectedListener {

    public interface ResultListener {
        void onDateTimeSubmit(Calendar newDateOrNull);
    }

    private static final String BUNDLE_OLD_DATE = "DateTimePickerDialog.ext_old_date";
    private static final String BUNDLE_NEW_DATE = "DateTimePickerDialog.ext_new_date";
    private static final String PARAM_DATE = "DateTimePickerDialog.param_date";

    private ResultListener mResultListener = null;
    private Calendar oldDate;
    private Calendar newDate;
    TextView mDateTitle = null;
    TextView mTimeTitle = null;
    TextView mTimeZoneTitle = null;

    private static class TimeZoneRow implements Comparable{
        final String id;
        final String name;
        final String offset;
        final int intOffset;

        TimeZoneRow(String id, String name, String offset, int intOffset) {
            this.id = id;
            this.name = name;
            this.offset = offset;
            this.intOffset = intOffset;
        }

        @Override
        public int compareTo(Object another) {
            if (another == null && another instanceof TimeZoneRow) {
                return 1;
            }
            TimeZoneRow c = (TimeZoneRow)another;
            int rv = new Integer(intOffset).compareTo(c.intOffset);
            if (rv == 0) {
                rv = name.compareTo(c.name);
            }
            return rv;
        }

        static TimeZoneRow[] getTimeZones() {
            String[] timeZonesIds = TimeZone.getAvailableIDs();
            TimeZoneRow[] timeZones = new TimeZoneRow[timeZonesIds.length];
            SimpleDateFormat formatter = (SimpleDateFormat)SimpleDateFormat.getInstance();
            formatter.applyPattern(Util.patternTimeZone);
            Calendar cal = GregorianCalendar.getInstance();
            for (int i = 0; i < timeZonesIds.length; ++i) {
                String id = timeZonesIds[i];
                TimeZone timeZone = TimeZone.getTimeZone(id);
                cal.setTimeZone(timeZone);
                String name = timeZone.getDisplayName();
                String offset = formatter.format(cal);
                timeZones[i] = new TimeZoneRow(id, name, offset, timeZone.getRawOffset());
            }
            Arrays.sort(timeZones);
            return timeZones;
        }
    }

    private class TimeZoneAdapter extends ArrayAdapter<TimeZoneRow> {
        private final LayoutInflater mInflater;

        TimeZoneAdapter(Context context) {
            super(context, R.layout.list_view_row_time_zone, TimeZoneRow.getTimeZones());
            mInflater = LayoutInflater.from(context);
        }

        @NonNull
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View rv = convertView;
            if (rv == null) {
                rv = mInflater.inflate(R.layout.list_view_row_time_zone, parent, false);
            }

            TimeZoneRow timeZoneRow = getItem(position);

            TextView textView = (TextView)rv.findViewById(R.id.time_zone_name);
            textView.setText(timeZoneRow.name);

            textView = (TextView)rv.findViewById(R.id.time_zone_offset);
            textView.setText(timeZoneRow.offset);

            return rv;
        }
    }

    public static void showDialog(Calendar date, FragmentManager fragmentManager, boolean fullScreen) {
        Bundle arg = new Bundle();
        arg.putSerializable(PARAM_DATE, date);
        DateTimePickerDialog dlg = new DateTimePickerDialog();
        dlg.setArguments(arg);
        if (fullScreen) {
            dlg.show(fragmentManager, "dateTimePickerDialog");
        }
        else {
            FragmentTransaction transaction = fragmentManager.beginTransaction();
            transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
            transaction.add(android.R.id.content, dlg)
                    .addToBackStack(null)
                    .commit();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable(BUNDLE_OLD_DATE, oldDate);
        outState.putSerializable(BUNDLE_NEW_DATE, newDate);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Bundle arguments = getArguments();
        if (arguments != null) {
            oldDate = (Calendar)arguments.getSerializable(PARAM_DATE);
            newDate = (Calendar)oldDate.clone();
        }

        if (savedInstanceState != null) {
            oldDate = (Calendar)savedInstanceState.getSerializable(BUNDLE_OLD_DATE);
            newDate = (Calendar)savedInstanceState.getSerializable(BUNDLE_NEW_DATE);
        }

        View view = inflater.inflate(R.layout.dialog_fragment_date_time_picker, container);

        DatePicker datePicker = (DatePicker)view.findViewById(R.id.datePicker);
        datePicker.init(newDate.get(Calendar.YEAR), newDate.get(Calendar.MONTH), newDate.get(Calendar.DATE), this);

        TimePicker timePicker = (TimePicker)view.findViewById(R.id.timePicker);
        timePicker.setCurrentHour(newDate.get(Calendar.HOUR_OF_DAY));
        timePicker.setCurrentMinute(newDate.get(Calendar.MINUTE));
        timePicker.setOnTimeChangedListener(this);

        ListView timeZonesListView = (ListView)view.findViewById(R.id.time_zone_picker);
        TimeZoneAdapter timeZoneAdapter = new TimeZoneAdapter(getContext());
        timeZonesListView.setAdapter(timeZoneAdapter);
        int offset = newDate.getTimeZone().getRawOffset();
        TimeZoneRow timeZoneRow = null;
        for (int i = 0; i < timeZoneAdapter.getCount(); ++i) {
            timeZoneRow = timeZoneAdapter.getItem(i);
            if (timeZoneRow.intOffset == offset) {
                timeZonesListView.setSelection(i);
                break;
            }
        }
        timeZonesListView.setOnItemSelectedListener(this);

        mDateTitle = new  TextView(getContext());
        mTimeTitle = new  TextView(getContext());
        mTimeZoneTitle = new  TextView(getContext());
        mDateTitle.setText(Util.formatDate(newDate));
        mTimeTitle.setText(Util.formatTime(newDate));
        mTimeZoneTitle.setText(timeZoneRow.offset);

        TabHost tabHost = (TabHost)view.findViewById(R.id.tabhost);
        tabHost.setup();
        tabHost.addTab(tabHost.newTabSpec("DatePicker")
            .setContent(R.id.datePicker)
            .setIndicator("1"/*mDateTitle*/));
        tabHost.addTab(tabHost.newTabSpec("TimePicker")
                .setContent(R.id.timePicker)
                .setIndicator("2"/*mTimeTitle*/));
        tabHost.addTab(tabHost.newTabSpec("TimeZoneList")
                .setContent(R.id.time_zone_picker)
                .setIndicator("3"/*mTimeZoneTitle*/));
        TabWidget tabWidget = tabHost.getTabWidget();

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
        Dialog dialog = super.onCreateDialog(savedInstanceState);
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
        newDate.set(year, monthOfYear, dayOfMonth);
        mDateTitle.setText(Util.formatDate(newDate));
    }

    @Override
    public void onTimeChanged(TimePicker view, int hourOfDay, int minute) {
        newDate.set(Calendar.HOUR_OF_DAY, hourOfDay);
        newDate.set(Calendar.MINUTE, minute);
        mTimeTitle.setText(Util.formatTime(newDate));
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        TimeZoneRow item = (TimeZoneRow)parent.getItemAtPosition(position);
        TimeZone newTimeZone = TimeZone.getTimeZone(item.id);
        newDate.setTimeZone(newTimeZone);
        mTimeZoneTitle.setText(item.offset);
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    @Override
    public void onCancel(DialogInterface dialog) {
        super.onCancel(dialog);
        newDate = (Calendar)oldDate.clone();
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
        if (mResultListener != null) {
            mResultListener.onDateTimeSubmit(oldDate.equals(newDate) ? null : newDate);
        }
    }

}
