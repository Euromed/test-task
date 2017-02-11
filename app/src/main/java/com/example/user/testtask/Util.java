package com.example.user.testtask;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.res.Resources;
import android.content.res.XmlResourceParser;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.SubscriptSpan;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.text.DateFormat;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by const on 04.11.2016.
 */

public class Util {
    private static final String TAG = "Util";

    public static void startExternalImageViewer(String url, Activity activity, @NonNull View view) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(Uri.parse(url), "image/*");
        try {
            activity.startActivity(intent);
            return;
        }
        catch (ActivityNotFoundException e) { }

        intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(url));
        try {
            activity.startActivity(intent);
            return;
        }
        catch (ActivityNotFoundException e) { }

        Snackbar.make(view, R.string.error_activity_not_found, Snackbar.LENGTH_LONG)
                .show();
    }

    public static final String patternTimeZone = "XXX";

    public static SpannableString formatDateTimeTimeZone(Calendar v) {
        SimpleDateFormat dateFormat = (SimpleDateFormat)SimpleDateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT);
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
        dateFormat.setTimeZone(v.getTimeZone());
        return dateFormat.format(v.getTime());
    }

    public static String formatTime(Calendar v) {
        SimpleDateFormat dateFormat = (SimpleDateFormat)SimpleDateFormat.getTimeInstance(DateFormat.SHORT);
        dateFormat.setTimeZone(v.getTimeZone());
        return dateFormat.format(v.getTime());
    }

    public static Calendar parseDateTime(String src) {
        com.ibm.icu.util.Calendar val = new com.ibm.icu.util.GregorianCalendar();
        com.ibm.icu.text.SimpleDateFormat dateFormat = getDateTimeInstance();
        dateFormat.applyPattern(dateFormat.toPattern() + patternTimeZone);
        ParsePosition pos = new ParsePosition(0);
        dateFormat.parse(src, val, pos);
        if (pos.getIndex() == 0) {
            dateFormat = getDateTimeInstance();
            dateFormat.parse(src, val, pos);
        }
        if (pos.getIndex() == 0) {
            return (null);
        }
        Calendar rv = new GregorianCalendar();
        rv.setTimeInMillis(val.getTimeInMillis());
        TimeZone tz = TimeZone.getTimeZone(val.getTimeZone().getID());
        rv.setTimeZone(tz);
        return (rv);
    }

    private static com.ibm.icu.text.SimpleDateFormat getDateTimeInstance() {
        return (com.ibm.icu.text.SimpleDateFormat)com.ibm.icu.text.SimpleDateFormat.
                getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT);
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

    static TimeZoneRow[] getTimeZones(Resources r) {
        String[] timeZonesIds = getTimeZonesIds(r);
        TimeZoneRow[] timeZones = new TimeZoneRow[timeZonesIds.length];
        SimpleDateFormat formatter = (SimpleDateFormat)SimpleDateFormat.getInstance();
        formatter.applyPattern(Util.patternTimeZone);
        Date date = new Date();
        int ii = 0;
        for (int i = 0; i < timeZonesIds.length; ++i) {
            String id = timeZonesIds[i]; // TimeZone.getCanonicalID(timeZonesIds[i]);
            TimeZone timeZone = TimeZone.getTimeZone(id);
            String name = timeZone.getDisplayName();
            if (name.startsWith("GMT")) {
                continue;
            }
            formatter.setTimeZone(timeZone);
            String offset = formatter.format(date);
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

    private static final Object sLastLockObj = new Object();
    private static String[] sLastZones = null;

    /**
     * Returns the time zones for the country, which is the code
     * attribute of the timezone element in time_zones_by_country.xml. Do not modify.
     *
     * @param r is app resource.
     * @return TimeZone list, maybe empty but never null. Do not modify.
     * @hide
     */
    public static String[] getTimeZonesIds(Resources r) {
        synchronized (sLastLockObj) {
            if (sLastZones != null) {
                return sLastZones;
            }
        }

        ArrayList<String> tzs = new ArrayList<String>();

        XmlResourceParser parser = r.getXml(R.xml.time_zones_by_country);

        try {
            beginDocument(parser, "timezones");

            while (true) {
                nextElement(parser);

                String element = parser.getName();
                if (element == null || !(element.equals("timezone"))) {
                    break;
                }

                String code = parser.getAttributeValue(null, "code");

                if (parser.next() == XmlPullParser.TEXT) {
                    String zoneIdString = parser.getText();
                    TimeZone tz = TimeZone.getTimeZone(zoneIdString);
                    if (tz.getID().startsWith("GMT") == false) {
                        // tz.getID doesn't start not "GMT" so its valid
                        tzs.add(zoneIdString);
                    }
                }
            }
        } catch (XmlPullParserException e) {
            Log.e(TAG, "Got xml parser exception getTimeZones(): e=", e);
        } catch (IOException e) {
            Log.e(TAG, "Got IO exception getTimeZones(): e=", e);
        } finally {
            parser.close();
        }

        synchronized(sLastLockObj) {
            // Cache the last result;
            sLastZones = tzs.toArray(new String[0]);
            return sLastZones;
        }
    }

    public static final void beginDocument(XmlPullParser parser, String firstElementName) throws XmlPullParserException, IOException
    {
        int type;
        while ((type=parser.next()) != parser.START_TAG
                && type != parser.END_DOCUMENT) {
            ;
        }

        if (type != parser.START_TAG) {
            throw new XmlPullParserException("No start tag found");
        }

        if (!parser.getName().equals(firstElementName)) {
            throw new XmlPullParserException("Unexpected start tag: found " + parser.getName() +
                    ", expected " + firstElementName);
        }
    }

    public static final void nextElement(XmlPullParser parser) throws XmlPullParserException, IOException
    {
        int type;
        while ((type=parser.next()) != parser.START_TAG
                && type != parser.END_DOCUMENT) {
            ;
        }
    }

}
