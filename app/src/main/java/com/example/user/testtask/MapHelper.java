package com.example.user.testtask;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.widget.ImageView;

/**
 * Created by const on 21.09.2016.
 */
public class MapHelper {
    static class StaticMapLoader extends BitmapWorkerTask.ImageLoader {
        final private double latitude;
        final private double longitude;
        private final Resources resources;

        StaticMapLoader (double latitude, double longitude, Resources resources) {
            this.latitude = latitude;
            this.longitude = longitude;
            this.resources = resources;
        }

        @Override
        public Bitmap load(int width, int height) {
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
            String mapUrl = String.format(resources.getString(R.string.header_static_map_request),
                    latitude, longitude,
                    requestedWidth, requestedHeight,
                    scale,
                    resources.getString(R.string.google_maps_key));
            BitmapWorkerTask.UrlImageLoader urlImageLoader = new BitmapWorkerTask.UrlImageLoader(mapUrl);
            return urlImageLoader.load(width, height);
        }

        @Override
        public boolean equals(Object otherObj) {
            if (super.equals(otherObj)) {
                StaticMapLoader other = (StaticMapLoader)otherObj;
                return (latitude == other.latitude &&
                        longitude == other.longitude &&
                        resources.equals(other.resources));
            }
            return (false);
        }
    }

    public static void LoadStatic(double latitude, double longitude, ImageView v) {
        BitmapWorkerTask.ImageLoader imageLoader = new StaticMapLoader(latitude, longitude, v.getResources());
        BitmapWorkerTask.loadBitmap(imageLoader, v);
    }
}
