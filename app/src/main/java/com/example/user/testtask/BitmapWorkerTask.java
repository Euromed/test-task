package com.example.user.testtask;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ImageView;

import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by const on 16.07.2016.
 */
public class BitmapWorkerTask extends AsyncTask<Void, Void, Bitmap> {

    static private class AsyncDrawable extends BitmapDrawable {
        private final WeakReference<ImageView> imageViewReference;
        private final ImageLoader imageLoader;
        private int width;
        private int height;
        private BitmapWorkerTask task = null;

        AsyncDrawable(ImageLoader imageLoader, ImageView imageView, Bitmap bitmap) {
            super(imageView.getResources(), bitmap);
            this.imageLoader = imageLoader;
            imageViewReference = new WeakReference<>(imageView);
            imageView.setImageDrawable(this);
            width = imageView.getWidth();
            height = imageView.getHeight();
        }

        void load() {
            ImageView imageView = imageViewReference.get();
            if (imageView != null && width > 0 && height > 0 && this == imageView.getDrawable()) {
                cancelTaskIfExists();
                task = new BitmapWorkerTask(this, width, height);
                task.execute();
            }
        }

        void cancelTaskIfExists() {
            if (task != null) {
                task.cancel(true);
                task = null;
            }
        }

        @Override
        protected void onBoundsChange(Rect bounds) {
            super.onBoundsChange(bounds);
            int w = bounds.width();
            int h = bounds.height();
            if (w != width || h != height) {
                width = w;
                height = h;
                load();
            }
        }
    }

    static abstract class ImageLoader {
        public boolean equals(Object otherObj) {
            return (this == otherObj || otherObj != null && getClass() == otherObj.getClass());
        }

        public abstract Bitmap load(int width, int height);

        BitmapFactory.Options createBitmapFactoryOptions(int width, int height) {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inSampleSize = 1;
            options.outWidth = width;
            options.outHeight = height;
            return (options);
        }
    }

    static private class ResourceImageLoader extends ImageLoader {
        private final int resId;
        private final Resources resources;

        ResourceImageLoader (int resId, Resources resources) {
            this.resId = resId;
            this.resources = resources;
        }

        @Override
        public boolean equals(Object otherObj) {
            if (super.equals(otherObj)) {
                ResourceImageLoader other = (ResourceImageLoader)otherObj;
                return (resId == other.resId && resources.equals(other.resources));
            }
            return (false);
        }

        @Override
        public Bitmap load(int width, int height) {
            BitmapFactory.Options options = createBitmapFactoryOptions(width, height);
            return BitmapFactory.decodeResource(resources, resId, options);
        }
    }

    public static void loadBitmap(final int resId, final ImageView imageView) {
        ImageLoader imageLoader = new ResourceImageLoader(resId, imageView.getResources());
        loadBitmap(imageLoader, imageView);
    }

    static class UrlImageLoader extends ImageLoader {
        private final String url;

        UrlImageLoader (String url) {
            this.url = url;
        }

        @Override
        public boolean equals(Object otherObj) {
            if (super.equals(otherObj)) {
                UrlImageLoader other = (UrlImageLoader)otherObj;
                return (url.equals(other.url));
            }
            return (false);
        }

        @Override
        public Bitmap load(int width, int height) {
            BitmapFactory.Options options = createBitmapFactoryOptions(width, height);
            Bitmap bitmap = null;
            InputStream is = null;
            String urlString = url;
            try {
                HttpURLConnection conn = null;
                for (int i = 0; i < 10; ++i) {
                    URL url = new URL(urlString);
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
                        urlString = conn.getHeaderField("Location");
                        Log.i("BWT", "The response redirect to: " + urlString);
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
    }

    public static void loadBitmap(final String url, ImageView imageView) {
        ImageLoader imageLoader = new UrlImageLoader(url);
        loadBitmap(imageLoader, imageView);
    }

    public static void loadBitmap(ImageLoader imageLoader, ImageView imageView) {
        loadBitmap(imageLoader, imageView, null);
    }

    public static void loadBitmap(ImageLoader imageLoader, ImageView imageView, Bitmap bitmap) {
        if (cancelPotentialWork(imageLoader, imageView)) {
            final AsyncDrawable asyncDrawable = new AsyncDrawable(imageLoader, imageView, bitmap);
            asyncDrawable.load();
        }
    }

    public static boolean cancelPotentialWork(ImageLoader imageLoader, ImageView imageView) {
        final AsyncDrawable asyncDrawable = getAsyncDrawable(imageView);
        if (asyncDrawable != null) {
            if (imageLoader.equals(asyncDrawable.imageLoader)) {
                return (false);
            }
            asyncDrawable.cancelTaskIfExists();
        }
        return (true);
    }

    private static AsyncDrawable getAsyncDrawable(ImageView imageView) {
        if (imageView != null) {
            final Drawable drawable = imageView.getDrawable();
            if (drawable instanceof AsyncDrawable) {
                return ((AsyncDrawable)drawable);
            }
        }
        return (null);
    }

    private final AsyncDrawable asyncDrawable;
    private final int width;
    private final int height;

    private BitmapWorkerTask(AsyncDrawable asyncDrawable, int width, int height) {
        this.asyncDrawable = asyncDrawable;
        this.width = width;
        this.height = height;
    }

    // Decode image in background.
    @Override
    protected Bitmap doInBackground(Void... voids) {
        return asyncDrawable.imageLoader.load(width, height);
    }

    @Override
    protected void onPostExecute(Bitmap bitmap) {
        final ImageView imageView = asyncDrawable.imageViewReference.get();
        if (!isCancelled() && bitmap != null && asyncDrawable == getAsyncDrawable(imageView)) {
            imageView.setImageBitmap(bitmap);
        }
    }
}

/*
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
                    conn.setReadTimeout(10000 /* milliseconds * /);
                    conn.setConnectTimeout(15000 /* milliseconds * /);
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

 */