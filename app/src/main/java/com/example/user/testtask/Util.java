package com.example.user.testtask;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.SubscriptSpan;

import com.ibm.icu.util.*;
import com.ibm.icu.text.*;

import java.text.ParsePosition;

/**
 * Created by const on 04.11.2016.
 */

public class Util {
    public static void startExternalImageViewer(String url, Activity activity) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(Uri.parse(url), "image/*");
        activity.startActivity(intent);
    }

    private static final String patternTimeZone = "XXXXX";

    public static SpannableString formatDateTimeTimeZone(Calendar v) {
        SimpleDateFormat dateFormat = getDateTimeInstance();
        String formattedDateTime = dateFormat.format(v);
        String pattern = dateFormat.toPattern();
        dateFormat.applyPattern(patternTimeZone);
        String formattedTimeZone = dateFormat.format(v);
        String resultString = formattedDateTime + formattedTimeZone;
        int startSubscript = formattedDateTime.length();
        int endSubscript = resultString.length();
        SpannableString rv = new SpannableString(resultString);
        rv.setSpan(new SubscriptSpan(), startSubscript, endSubscript, Spanned.SPAN_EXCLUSIVE_INCLUSIVE);
        return rv;
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
}
