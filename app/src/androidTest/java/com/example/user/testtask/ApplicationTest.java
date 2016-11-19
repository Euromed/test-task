package com.example.user.testtask;

import android.app.Application;
import android.test.ApplicationTestCase;
import android.text.SpannableString;
import android.util.Log;

import com.ibm.icu.util.GregorianCalendar;

/**
 * <a href="http://d.android.com/tools/testing/testing_android.html">Testing Fundamentals</a>
 */
public class ApplicationTest extends ApplicationTestCase<Application> {
    public ApplicationTest() {
        super(Application.class);
        testFormatDateTime();
    }

    public void testFormatDateTime() {
        SpannableString v = Util.formatDateTimeTimeZone(GregorianCalendar.getInstance());
        Log.i("TFDT", v.toString());
    }
}