package com.holdingscythe.pocketamcreader.utils;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;

import com.holdingscythe.pocketamcreader.R;
import com.holdingscythe.pocketamcreader.S;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.ref.SoftReference;
import java.util.HashMap;
import java.util.Stack;

// TODO deprecated
// TODO: cleanup

public class PictureLoader {
    private static HashMap<String, SoftReference<Bitmap>> cache = new HashMap<String, SoftReference<Bitmap>>();
    private BitmapFactory.Options o;
    private BitmapFactory.Options o2;
    private BitmapFactory.Options o3;

    private File cacheDir;
    private String settingPicturesFolder;
    private Boolean settingLowQualityThumbs;
    private final int thumbWidth;
    private final int thumbHeight;
    final int stub_id = R.drawable.movie_thumb_stub;

    /**
     * Constructor
     */
    public PictureLoader(Context context) {
        // Make the background thread low priority. This way it will not affect the UI performance
        picturesLoaderThread.setPriority(Thread.NORM_PRIORITY - 1);

        // Get folder to get pictures from
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        this.settingPicturesFolder = preferences.getString("settingPicturesFolder", "/");
        this.settingLowQualityThumbs = preferences.getBoolean("settingLowQualityThumbs", true);

        // Get device relative sizes for thumbnails
        final LayoutInflater inflater = LayoutInflater.from(context);
        View v = inflater.inflate(R.layout.list_movie_item, null);
        ImageView moviePictureView = (ImageView) v.findViewById(R.id.imageCover);
        this.thumbHeight = moviePictureView.getLayoutParams().height;
        this.thumbWidth = moviePictureView.getLayoutParams().width;
        // Enable for AR driven thumbnails. Needs also change in layout.
        // this.thumbWidth = (int) Math.round((float) this.thumbHeight * Movies.THUMB_AR);
        if (S.DEBUG)
            Log.d(S.TAG, "Thumbnail size set to: " + this.thumbWidth + "x" + this.thumbHeight);

        // Prepare BitmapFactory objects - get sizes for thumb
        this.o = new BitmapFactory.Options();
        this.o.inJustDecodeBounds = true;

        // Prepare BitmapFactory objects - make actual thumb
        this.o2 = new BitmapFactory.Options();
        if (this.settingLowQualityThumbs) {
            this.o2.inDither = true;
            this.o2.inScaled = true;
            this.o2.inPreferredConfig = Bitmap.Config.RGB_565;
            this.o2.inSampleSize = S.THUMB_IN_SAMPLE_SIZE;
        } else {
            this.o2.inDither = false;
            this.o2.inScaled = false;
            this.o2.inPreferredConfig = Bitmap.Config.ARGB_8888;
        }

        // Prepare BitmapFactory objects - get thumb to memory
        this.o3 = new BitmapFactory.Options();
        this.o3.inPurgeable = true;
        this.o3.inInputShareable = true;

        // Find the directory to save cached pictures
        if (android.os.Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED))
            // Mimic getExternalFilesDir on API<8
            cacheDir = new File(android.os.Environment.getExternalStorageDirectory(), "/Android/data/"
                    + context.getPackageName() + "/thumbs/");
        else
            cacheDir = context.getCacheDir();
        if (!cacheDir.exists())
            cacheDir.mkdirs();
    }

    /**
     * Display picture in list
     */
    public void displayPicture(String pictureNumber, String catalogPath, Activity activity, ImageView imageView) {
        String devicePath;
        if (catalogPath == null || catalogPath.equals("")) {
            picturesQueue.Clean(imageView);
            imageView.setImageResource(stub_id);
        } else {
            devicePath = this.settingPicturesFolder + catalogPath;
            File localFile = new File(devicePath);
            if (!localFile.exists()) {
                picturesQueue.Clean(imageView);
                imageView.setImageResource(stub_id);
            } else {
                if (cache.containsKey(devicePath)) {
                    SoftReference<Bitmap> bmpRef = cache.get(devicePath);
                    Bitmap bmp = bmpRef.get();
                    if (bmp != null) {
                        imageView.setImageBitmap(bmp);
                        if (S.DEBUG)
                            Log.d(S.TAG, "Providing picture L1: " + catalogPath);
                    } else {
                        queuePicture(pictureNumber, catalogPath, devicePath, activity, imageView);
                        imageView.setImageResource(stub_id);
                    }
                } else {
                    queuePicture(pictureNumber, catalogPath, devicePath, activity, imageView);
                    imageView.setImageResource(stub_id);
                }
            }
        }
    }

    /**
     * Queue photo for asynchronous loading
     */
    private void queuePicture(String pictureNumber, String catalogPath, String devicePath, Activity activity,
                              ImageView imageView) {
        // This ImageView may be used for other images before. So there may be some old tasks in the queue.
        // We need to discard them.
        picturesQueue.Clean(imageView);
        PictureToLoad p = new PictureToLoad(pictureNumber, catalogPath, devicePath, imageView, activity);
        synchronized (picturesQueue.picturesToLoad) {
            picturesQueue.picturesToLoad.push(p);
            picturesQueue.picturesToLoad.notifyAll();
        }

        // Start thread if it's not started yet
        if (picturesLoaderThread.getState() == Thread.State.NEW)
            picturesLoaderThread.start();
    }

    /**
     * Get bitmap from cache or from storage
     */
    private Bitmap getBitmap(String numberVal, String devicePath) {
        // Identify pictures by picture number and hashCode
        String tmpFilename = numberVal + "-" + String.valueOf(devicePath.hashCode());
        File cachedPictureFile = new File(this.cacheDir, tmpFilename);
        File devicePathFile = new File(devicePath);

        if (!cachedPictureFile.exists() || (cachedPictureFile.lastModified() < devicePathFile.lastModified())) {
            cachedPictureFile = this.smartResizeImage(devicePathFile, cachedPictureFile);
            if (S.WARN) {
                if (cachedPictureFile != null)
                    Log.d(S.TAG, "Cached picture: " + devicePath);
                else
                    Log.w(S.TAG, "Caching failed for: " + devicePath);
            }
        }

        if (cachedPictureFile != null) {
            try {
                if (S.DEBUG)
                    Log.d(S.TAG, "Providing picture L2: " + devicePath);

                FileInputStream inputStream = new FileInputStream(cachedPictureFile);
                Bitmap bmp = BitmapFactory.decodeStream(inputStream, null, this.o3);
                inputStream.close();
                return bmp;
            } catch (FileNotFoundException e) {
                return null;
            } catch (IOException e) {
                return null;
            }
        } else {
            return null;
        }
    }

    /**
     * Write smart resized picture to disk cache
     */
    private File smartResizeImage(File origImage, File tmpImage) {
        if (!origImage.exists() || !origImage.canRead())
            return null;

        try {
            FileInputStream inputStream = new FileInputStream(origImage);
            BitmapFactory.decodeStream(inputStream, null, this.o);
            inputStream.close();

            // Get size for smart resize
            float targetAR = (float) this.thumbWidth / (float) this.thumbHeight;
            float sourceAR = (float) this.o.outWidth / (float) this.o.outHeight;
            int srcWidth;
            int srcHeight;
            int offsetLeft;
            int offsetTop;

            if (targetAR >= sourceAR) {
                // Use 100% width
                srcWidth = this.o.outWidth;
                srcHeight = Math.round((float) this.o.outWidth / (float) this.thumbWidth * this.thumbHeight);
                offsetLeft = 0;
                offsetTop = Math.round((float) (this.o.outHeight - srcHeight) / 2);
            } else {
                // Use 100% height
                srcWidth = Math.round((float) this.o.outHeight / (float) this.thumbHeight * this.thumbWidth);
                srcHeight = this.o.outHeight;
                offsetLeft = Math.round((float) (this.o.outWidth - srcWidth) / 2);
                offsetTop = 0;
            }

            if (this.settingLowQualityThumbs) {
                srcWidth = (srcWidth / S.THUMB_IN_SAMPLE_SIZE);
                srcHeight = (srcHeight / S.THUMB_IN_SAMPLE_SIZE);
                offsetLeft = (offsetLeft / S.THUMB_IN_SAMPLE_SIZE);
                offsetTop = (offsetTop / S.THUMB_IN_SAMPLE_SIZE);
            }

            // Create thumbnail
            FileInputStream inputStream2 = new FileInputStream(origImage);
            Bitmap thumbBitmap = Bitmap.createBitmap(BitmapFactory.decodeStream(inputStream2, null, this.o2));
            inputStream2.close();

            FileOutputStream outputStream = new FileOutputStream(tmpImage);
            thumbBitmap = Bitmap.createBitmap(thumbBitmap, offsetLeft, offsetTop, srcWidth, srcHeight);
            thumbBitmap = Bitmap.createScaledBitmap(thumbBitmap, this.thumbWidth, this.thumbHeight, true);
            thumbBitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream);
            thumbBitmap = null;
            outputStream.close();

            return tmpImage;
        } catch (FileNotFoundException e) {
            if (S.ERROR)
                Log.e(S.TAG, "Couldn't create thumbnail - file not found.");
            return null;
        } catch (NullPointerException e) {
            if (S.ERROR)
                Log.e(S.TAG, "Couldn't create thumbnail - null pointer exception.");
            return null;
        } catch (Exception e) {
            if (S.ERROR)
                Log.e(S.TAG, "Couldn't create thumbnail - unknown error.");
            return null;
        }

    }

    /**
     * Clear thumbnail cache
     */
    public void clearCache() {
        // clear memory cache
        cache.clear();

        // clear SD cache
        File[] files = cacheDir.listFiles();
        for (File f : files)
            f.delete();
    }

    /**
     * Stop background thread if necessary
     */
    public void stopThread() {
        picturesLoaderThread.interrupt();
    }

    PicturesQueue picturesQueue = new PicturesQueue();
    PicturesLoader picturesLoaderThread = new PicturesLoader();

    /**
     * Stores list of pictures to download
     */
    class PicturesQueue {
        private Stack<PictureToLoad> picturesToLoad = new Stack<PictureToLoad>();

        // removes all instances of this ImageView
        public void Clean(ImageView image) {
            int i = 0;

            synchronized (picturesQueue.picturesToLoad) {
                try {
                    while (i < picturesToLoad.size()) {
                        if (picturesToLoad.get(i).imageView == image) {
                            if (S.VERBOSE)
                                Log.v(S.TAG,
                                        "Trashing imageView: " + image.toString() + " - "
                                                + String.valueOf(picturesToLoad.get(i).catalogPath)
                                );
                            picturesToLoad.remove(i);
                        } else {
                            i++;
                        }
                    }
                } catch (Exception e) {
                    if (S.ERROR)
                        Log.e(S.TAG, "Trashing imageView ERROR");
                }
            }
        }
    }

    /**
     * Background thread to load pictures
     */
    class PicturesLoader extends Thread {
        @Override
        public void run() {
            try {
                while (true) {
                    // thread waits until there are any pictures to load in the queue
                    if (picturesQueue.picturesToLoad.size() == 0)
                        synchronized (picturesQueue.picturesToLoad) {
                            picturesQueue.picturesToLoad.wait();
                        }
                    if (picturesQueue.picturesToLoad.size() != 0) {
                        PictureToLoad photoToLoad;
                        synchronized (picturesQueue.picturesToLoad) {
                            photoToLoad = picturesQueue.picturesToLoad.remove(0);
                        }
                        Bitmap bmp = getBitmap(photoToLoad.pictureNumber, photoToLoad.devicePath);
                        cache.put(photoToLoad.devicePath, new SoftReference<Bitmap>(bmp));
                        Object tag = photoToLoad.imageView.getTag();
                        if (tag != null && ((String) tag).equals(photoToLoad.catalogPath)) {
                            BitmapDisplayer bd = new BitmapDisplayer(bmp, photoToLoad.imageView);
                            Activity a = photoToLoad.activity;
                            a.runOnUiThread(bd);
                        }
                    }
                    if (Thread.interrupted())
                        break;
                }
            } catch (InterruptedException e) {
                // allow thread to exit
            }
        }
    }

    /**
     * Used to display bitmap in the UI thread
     */
    class BitmapDisplayer implements Runnable {
        Bitmap bitmap;
        ImageView imageView;

        public BitmapDisplayer(Bitmap b, ImageView i) {
            this.bitmap = b;
            this.imageView = i;
        }

        @Override
        public void run() {
            if (this.bitmap != null)
                try {
                    this.imageView.setImageBitmap(bitmap);
                } catch (Exception e) {
                    // Do nothing, image is not shown
                }
            else
                this.imageView.setImageResource(stub_id);
        }
    }

    /**
     * Task for the queue
     */
    private class PictureToLoad {
        public String pictureNumber;
        public String catalogPath;
        public String devicePath;
        public ImageView imageView;
        public Activity activity;

        public PictureToLoad(String n, String c, String d, ImageView i, Activity a) {
            this.pictureNumber = n;
            this.catalogPath = c;
            this.devicePath = d;
            this.imageView = i;
            this.activity = a;
        }
    }

}
