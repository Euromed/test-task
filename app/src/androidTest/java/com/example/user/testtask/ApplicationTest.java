package com.example.user.testtask;

import android.app.Application;
import android.test.ApplicationTestCase;
import android.text.SpannableString;
import android.util.Log;

import java.util.*;

/**
 * <a href="http://d.android.com/tools/testing/testing_android.html">Testing Fundamentals</a>
 */
public class ApplicationTest extends ApplicationTestCase<Application> {
    public ApplicationTest() {
        super(Application.class);
    }

    public void testFormatDateTime() {
        SpannableString v = Util.formatDateTimeTimeZone(GregorianCalendar.getInstance());
        Log.i("TFDT", v.toString());
    }

    public void testParseDateTime() throws Exception {
        Calendar cal;
        SpannableString info;

        cal = Util.parseDateTime("01.10.1980 22:40-8");
        assertNotNull(cal);
        info = Util.formatDateTimeTimeZone(cal);
        Log.i("TPDT", info.toString());

        cal = Util.parseDateTime("01.10.1980, 22:40");
        assertNotNull(cal);
        info = Util.formatDateTimeTimeZone(cal);
        Log.i("TPDT", info.toString());
    }
}