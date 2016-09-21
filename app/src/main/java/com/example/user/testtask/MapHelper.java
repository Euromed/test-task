package com.example.user.testtask;

import android.content.res.Resources;
import android.widget.ImageView;

/**
 * Created by const on 21.09.2016.
 */
public class MapHelper {
    public static void LoadStatic(ImageView v, double latitude, double longitude) {
        int width = v.getWidth();
        int height = v.getHeight();
        Resources resources = v.getContext().getResources();
        int density = Math.round(resources.getDisplayMetrics().xdpi);
        int scale = 1;
        int requestedWidth = width;
        int requestedHeight = height;
        if (density > 160 || requestedWidth > 640 || requestedHeight > 640) {
            scale = 2;
            requestedWidth /= 2;
            requestedHeight /= 2;
        }
        if (requestedWidth > 640 || requestedHeight > 640) {
            if (width > height) {
                requestedWidth = 640;
                requestedHeight = height * 640 / width;
            }
            else {
                requestedWidth = width * 640 / height;
                requestedHeight = 640;
            }
        }
        String mapURL = String.format(resources.getString(R.string.header_static_map_request),
                latitude, longitude,
                requestedWidth, requestedHeight,
                scale,
                resources.getString(R.string.google_maps_key));
        BitmapWorkerTask.loadBitmap(mapURL, v);
    }
}
