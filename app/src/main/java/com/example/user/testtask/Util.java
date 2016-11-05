package com.example.user.testtask;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;

/**
 * Created by const on 04.11.2016.
 */

public class Util {
    public static void startExternalImageViewer(String url, Activity activity) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(Uri.parse(url), "image/*");
        activity.startActivity(intent);
    }
}
