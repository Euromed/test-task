package com.example.user.testtask;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.SubscriptSpan;

import com.ibm.icu.text.DateFormat;
import com.ibm.icu.text.SimpleDateFormat;
import com.ibm.icu.util.*;
import com.ibm.icu.text.*;

import java.text.*;

/**
 * Created by const on 04.11.2016.
 */

public class Util {
    public static void startExternalImageViewer(String url, Activity activity) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(Uri.parse(url), "image/*");
        activity.startActivity(intent);
    }

    public static SpannableString formatDateTimeTimeZone(Calendar v) {
        DateFormat dateFormat = SimpleDateFormat.getDateTimeInstance();
        String formattedDateTime = dateFormat.format(v);
        int dateTimeLength = formattedDateTime.length();
        SpannableString rv = new SpannableString(formattedDateTime + " " + v.getTimeZone().getDisplayName());
        rv.setSpan(new SubscriptSpan(), dateTimeLength, rv.length(), Spanned.SPAN_EXCLUSIVE_INCLUSIVE);
        return rv;
    }

    public static Calendar parseDateTime(String src) {
        Calendar rv = new GregorianCalendar();
        DateFormat dateFormat = SimpleDateFormat.getDateTimeInstance();

        ((SimpleDateFormat)dateFormat).parse(src, rv, new ParsePosition(0));
        try {
            dateFormat.parse(src);
            rv = dateFormat.getCalendar();
        }
        catch (ParseException e) {
        } TimeZone tz;
        return rv;
    }
}
