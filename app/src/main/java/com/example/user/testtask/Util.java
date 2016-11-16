package com.example.user.testtask;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;

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

    public static String formatDateTime(Calendar v) {
        DateFormat dateFormat = SimpleDateFormat.getDateTimeInstance();
        dateFormat.setCalendar((Calendar)v.clone());
        return dateFormat.format(v.getTime());
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
