package com.example.user.testtask;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.SubscriptSpan;

import com.ibm.icu.util.*;
import com.ibm.icu.text.*;

import java.text.ParsePosition;
import java.util.Arrays;

/**
 * Created by const on 04.11.2016.
 */

public class Util {
    public static void startExternalImageViewer(String url, Activity activity) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(Uri.parse(url), "image/*");
        activity.startActivity(intent);
    }

    public static final String patternTimeZone = "XXXXX";

    public static SpannableString formatDateTimeTimeZone(Calendar v) {
        SimpleDateFormat dateFormat = getDateTimeInstance();
        String formattedDateTime = dateFormat.format(v);
        dateFormat.applyPattern(patternTimeZone);
        String formattedTimeZone = dateFormat.format(v);
        String resultString = formattedDateTime + formattedTimeZone;
        int startSubscript = formattedDateTime.length();
        int endSubscript = resultString.length();
        SpannableString rv = new SpannableString(resultString);
        rv.setSpan(new SubscriptSpan(), startSubscript, endSubscript, Spanned.SPAN_EXCLUSIVE_INCLUSIVE);
        return rv;
    }

    public static String formatDate(Calendar v) {
        SimpleDateFormat dateFormat = (SimpleDateFormat)SimpleDateFormat.getDateInstance(DateFormat.SHORT);
        return dateFormat.format(v);
    }

    public static String formatTime(Calendar v) {
        SimpleDateFormat dateFormat = (SimpleDateFormat)SimpleDateFormat.getTimeInstance(DateFormat.SHORT);
        return dateFormat.format(v);
    }

    public static Calendar parseDateTime(String src) {
        Calendar rv = new GregorianCalendar();
        SimpleDateFormat dateFormat = getDateTimeInstance();
        dateFormat.applyPattern(dateFormat.toPattern() + patternTimeZone);
        ParsePosition pos = new ParsePosition(0);
        dateFormat.parse(src, rv, pos);
        if (pos.getIndex() == 0) {
            dateFormat = getDateTimeInstance();
            dateFormat.parse(src, rv, pos);
        }
        return pos.getIndex() == 0 ? null : rv;
    }

    private static SimpleDateFormat getDateTimeInstance() {
        return (SimpleDateFormat)SimpleDateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT);
    }

    public static boolean is24HourFormat() {
        java.util.TimeZone timeZone = java.util.TimeZone.getDefault();
        return getDateTimeInstance().toPattern().indexOf("H") >= 0;
    }

    public static class TimeZoneRow implements Comparable {
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
            TimeZoneRow c = (TimeZoneRow) another;
            int rv = new Integer(intOffset).compareTo(c.intOffset);
            if (rv == 0) {
                rv = name.compareTo(c.name);
            }
            return rv;
        }
    }

    static TimeZoneRow[] getTimeZones() {
        String[] timeZonesIds = TimeZone.getAvailableIDs();
        TimeZoneRow[] timeZones = new TimeZoneRow[timeZonesIds.length];
        SimpleDateFormat formatter = (SimpleDateFormat)SimpleDateFormat.getInstance();
        formatter.applyPattern(Util.patternTimeZone);
        Calendar cal = GregorianCalendar.getInstance();
        int ii = 0;
        for (int i = 0; i < timeZonesIds.length; ++i) {
            String id = TimeZone.getCanonicalID(timeZonesIds[i]);
            TimeZone timeZone = TimeZone.getTimeZone(id);
            String name = timeZone.getDisplayName();
            if (name.startsWith("GMT")) {
                continue;
            }
            cal.setTimeZone(timeZone);
            String offset = formatter.format(cal);
            timeZones[ii++] = new TimeZoneRow(id, name, offset, timeZone.getRawOffset());
        }
        Arrays.sort(timeZones, 0, ii);
        int j = 1;
        for (int i = 1; i < ii; ++i) {
            if (timeZones[i-1].compareTo(timeZones[i]) != 0) {
                if (j != i) {
                    timeZones[j++] = timeZones[i];
                }
            }
        }

        TimeZoneRow[] rv;
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.FROYO) {
            rv = new TimeZoneRow[j];
            System.arraycopy(timeZones, 0, rv, 0, j);
        }
        else {
            rv = Arrays.copyOf(timeZones, j);
        }
        return rv;
    }

}
