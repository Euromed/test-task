package com.example.user.testtask;

import android.text.SpannableString;
import android.util.Log;

import java.util.GregorianCalendar;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by const on 19.11.2016.
 */
public class UtilTest {
    @Test
    public void formatDateTimeTimeZone() throws Exception {
        SpannableString v = Util.formatDateTimeTimeZone(GregorianCalendar.getInstance());
        Log.i("TFDT", v.toString());
    }

    @Test
    public void parseDateTime() throws Exception {

    }

}