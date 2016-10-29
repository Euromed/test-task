package com.example.user.testtask;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.provider.ContactsContract;
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
    private final ImageLoader imageLoader;

    static class AsyncDrawable extends BitmapDrawable {
        private final WeakReference<BitmapWorkerTask> bitmapWorkerTaskReference;
        private BitmapWorkerTask task;
        private int width = -1;
        private int height = -1;

        public AsyncDrawable(Resources res, Bitmap bitmap,
                             BitmapWorkerTask bitmapWorkerTask) {
            super(res, bitmap);
            bitmapWorkerTaskReference =
                    new WeakReference<BitmapWorkerTask>(bitmapWorkerTask);
            task = bitmapWorkerTask;
        }

        public BitmapWorkerTask getBitmapWorkerTask() {
            return bitmapWorkerTaskReference.get();
        }

        void tryLoad(int width, int height) {
            if (task != null && width > 0 && height > 0) {
                task.execute(width, height);
                task = null;
            }
            this.width = width;
            this.height = height;
        }

        @Override
        protected void onBoundsChange(Rect bounds) {
            super.onBoundsChange(bounds);
            int w = bounds.width();
            int h = bounds.height();
            if (w != width || h != height) {
                tryLoad(w, h);
            }
        }
    }

    interface ImageLoader {
        public Bitmap load(int width, int height);
    }

    public static void loadBitmap(ImageLoader imageLoader, ImageView imageView) {
        if (cancelPotentialWork(imageLoader, imageView)) {
            final BitmapWorkerTask task = new BitmapWorkerTask(imageLoader, imageView);
            final AsyncDrawable asyncDrawable =
                    new AsyncDrawable(imageView.getResources(), null, task);
            imageView.setImageDrawable(asyncDrawable);
            asyncDrawable.tryLoad(imageView.getWidth(), imageView.getHeight());
        }
    }

    public static void loadBitmap(final String url, ImageView imageView) {
        ImageLoader imageLoader = new ImageLoader() {
            String urlData = url;
            @Override
            public Bitmap load(int width, int height) {
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inSampleSize = 1;
                options.outWidth = width;
                options.outHeight = height;
                Bitmap bitmap = null;
                InputStream is = null;
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
                return bitmap;
            }
        };
        loadBitmap(imageLoader, imageView);
    }

    public static void loadBitmap(final int resId, final ImageView imageView) {
        ImageLoader imageLoader = new ImageLoader() {
            final Resources resources = imageView.getResources();
            public Bitmap load(int width, int height) {
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inSampleSize = 1;
                options.outWidth = width;
                options.outHeight = height;
                return BitmapFactory.decodeResource(resources, resId, options);
            }
        };
        loadBitmap(imageLoader, imageView);
    }

    public static boolean cancelPotentialWork(ImageLoader imageLoader, ImageView imageView) {
        final BitmapWorkerTask bitmapWorkerTask = getBitmapWorkerTask(imageView);

        if (bitmapWorkerTask != null) {
            final ImageLoader workerImageLoader = bitmapWorkerTask.imageLoader;
            // If bitmapData is not yet set or it differs from the new data
            if (workerImageLoader == imageLoader) {
//-----!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
                // The same work is already in progress
                return (false);
            }
            // Cancel previous task
            bitmapWorkerTask.cancel(true);
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

    public BitmapWorkerTask(ImageLoader imageLoader, ImageView imageView) {
        // Use a WeakReference to ensure the ImageView can be garbage collected
        imageViewReference = new WeakReference<ImageView>(imageView);
        this.imageLoader = imageLoader;
    }

    // Decode image in background.
    @Override
    protected Bitmap doInBackground(Object... params) {
        int width = (Integer)params[0];
        int height = (Integer)params[1];
        return imageLoader.load(width, height);
    }

    @Override
    protected void onPostExecute(Bitmap bitmap) {
        if (!isCancelled() && imageViewReference != null && bitmap != null) {
            final ImageView imageView = imageViewReference.get();
            final BitmapWorkerTask bitmapWorkerTask =
                    getBitmapWorkerTask(imageView);
            if (this == bitmapWorkerTask && imageView != null) {
                imageView.setImageBitmap(bitmap);
            }
        }
    }
}
