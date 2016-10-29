package com.example.user.testtask;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.util.Log;
import android.view.ViewDebug;
import android.widget.ImageView;

import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by const on 16.07.2016.
 */
class BitmapWorkerTask extends AsyncTask<Object, Void, Bitmap> {
    private final WeakReference<ImageView> imageViewReference;
    private final Resources resources;
    private int data = 0;
    private String urlData;

    static class AsyncDrawable extends BitmapDrawable {
        private final WeakReference<BitmapWorkerTask> bitmapWorkerTaskReference;

        public AsyncDrawable(Resources res, Bitmap bitmap,
                             BitmapWorkerTask bitmapWorkerTask) {
            super(res, bitmap);
            bitmapWorkerTaskReference =
                    new WeakReference<BitmapWorkerTask>(bitmapWorkerTask);
        }

        public BitmapWorkerTask getBitmapWorkerTask() {
            return bitmapWorkerTaskReference.get();
        }
    }

    public static void loadBitmap(int resId, ImageView imageView) {
        loadBitmap(resId, null, imageView);
    }

    public static void loadBitmap(String url, ImageView imageView) {
        loadBitmap(-1, url, imageView);
    }

    public static void loadBitmap(int resId, String url, ImageView imageView) {
        if (cancelPotentialWork(resId, url, imageView)) {
            final BitmapWorkerTask task = new BitmapWorkerTask(imageView);
            final AsyncDrawable asyncDrawable =
                    new AsyncDrawable(imageView.getResources(), null, task);
            imageView.setImageDrawable(asyncDrawable);

            int w = imageView.getWidth();
            int h = imageView.getHeight();
            Log.i("BWT", "Load bitmap - view size (" + w + ", " + h + ")\n");

            task.execute(resId, url, imageView.getWidth(), imageView.getHeight());
        }
    }

    public static boolean cancelPotentialWork(int data, String url, ImageView imageView) {
        final BitmapWorkerTask bitmapWorkerTask = getBitmapWorkerTask(imageView);

        if (bitmapWorkerTask != null) {
            final int bitmapData = bitmapWorkerTask.data;
            final String bitmapUrlData = bitmapWorkerTask.urlData;
            // If bitmapData is not yet set or it differs from the new data
            if (bitmapData == 0 || bitmapData != data ||
                    bitmapUrlData != url && (bitmapUrlData == null || url == null || !bitmapUrlData.equals(url))) {
                // Cancel previous task
                bitmapWorkerTask.cancel(true);
            } else {
                // The same work is already in progress
                return false;
            }
        }
        // No task associated with the ImageView, or an existing task was cancelled
        return true;
    }

    private static BitmapWorkerTask getBitmapWorkerTask(ImageView imageView) {
        if (imageView != null) {
            final Drawable drawable = imageView.getDrawable();
            if (drawable instanceof AsyncDrawable) {
                final AsyncDrawable asyncDrawable = (AsyncDrawable) drawable;
                return asyncDrawable.getBitmapWorkerTask();
            }
        }
        return null;
    }

    public BitmapWorkerTask(ImageView imageView) {
        // Use a WeakReference to ensure the ImageView can be garbage collected
        imageViewReference = new WeakReference<ImageView>(imageView);
        resources = imageView.getResources();
    }

    // Decode image in background.
    @Override
    protected Bitmap doInBackground(Object... params) {
        data = (Integer)params[0];
        urlData = (String)params[1];
        int width = (Integer)params[2];
        int height = (Integer)params[3];
        Bitmap bitmap = null;
        InputStream is = null;
        Log.i("BWT", "Load bitmap - " + (urlData == null ? data : urlData) + "\n");
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = 1;
        options.outWidth = width;
        options.outHeight = height;
        if (urlData == null) {
            bitmap = BitmapFactory.decodeResource(resources, data, options);
        }
        else {
            try {
                HttpURLConnection conn = null;
                for (int i = 0; i < 10; ++i) {
                    URL url = new URL(urlData);
                    conn = (HttpURLConnection) url.openConnection();
                    conn.setReadTimeout(10000 /* milliseconds */);
                    conn.setConnectTimeout(15000 /* milliseconds */);
                    conn.setRequestMethod("GET");
                    conn.setDoInput(true);
                    conn.setInstanceFollowRedirects(true);
                    conn.connect();
                    int response = conn.getResponseCode();
                    Log.i("BWT", "The response for image URL is: " + response);
                    if (response / 100 == 3) {
                        urlData = conn.getHeaderField("Location");
                        Log.i("BWT", "The response redirect to: " + urlData);
                        continue;
                    }
                    break;
                }
                is = conn.getInputStream();
                bitmap = BitmapFactory.decodeStream(is, null, options);
                Log.i("BWT", "Decoded bitmap - " + bitmap.getWidth() + "x" + bitmap.getHeight() + "\n");
            }
            catch (Exception e) {}
            finally {
                if (is != null) {
                    try {
                        is.close();
                    }
                    catch (Exception e) {}
                }
            }
        }
        return bitmap;
    }

    @Override
    protected void onPostExecute(Bitmap bitmap) {
        if (isCancelled()) {
            bitmap = null;
        }

        if (imageViewReference != null && bitmap != null) {
            final ImageView imageView = imageViewReference.get();
            final BitmapWorkerTask bitmapWorkerTask =
                    getBitmapWorkerTask(imageView);
            if (this == bitmapWorkerTask && imageView != null) {
                imageView.setImageBitmap(bitmap);
            }
        }
    }
}
