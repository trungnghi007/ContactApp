package com.example.contactapp.utils;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class Utils {

    public static Bitmap uriToBitmap(Context context, Uri uri) throws IOException {
        Bitmap bitmap = null;
        InputStream inputStream = null;
        try {
            ContentResolver contentResolver = context.getContentResolver();
            inputStream = contentResolver.openInputStream(uri);
            if (inputStream != null) {
                bitmap = BitmapFactory.decodeStream(inputStream);
                if (bitmap == null) {
                    Log.e("Utils", "Failed to decode bitmap from URI: " + uri);
                    throw new IOException("Failed to decode bitmap");
                }
            } else {
                Log.e("Utils", "InputStream is null for URI: " + uri);
                throw new IOException("InputStream is null");
            }

        }
        catch (FileNotFoundException e) {
            Log.e("Utils", "File not found for URI: " + uri, e);
            throw new IOException("File not found", e);
        } finally {
            if (inputStream != null){
                inputStream.close();
            }
        }

        return bitmap;
    }
    public static Uri copyUriToCache(Context context, Uri uri, String filename) throws IOException {
        InputStream inputStream = null;
        OutputStream outputStream = null;
        File tempFile = new File(context.getCacheDir(), filename);
        try {
            inputStream = context.getContentResolver().openInputStream(uri);
            if (inputStream != null) {
                Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                if (bitmap == null) {
                    throw new IOException("Failed to decode bitmap");
                }
                outputStream = new FileOutputStream(tempFile);
                bitmap.compress(Bitmap.CompressFormat.JPEG, 90, outputStream);
                outputStream.close();
            } else {
                throw new IOException("InputStream is null");
            }
        } catch (FileNotFoundException e) {
            Log.e("Utils", "File not found for URI: " + uri, e);
            throw new IOException("File not found", e);
        }finally {
            if (inputStream != null) {
                inputStream.close();
            }
            if (outputStream != null) {
                outputStream.close();
            }
        }
        return getNewCacheImageUri(context,filename);

    }

    private static Uri getNewCacheImageUri(Context context, String fileName) {
        File cacheDir = new File(context.getCacheDir(), fileName);
        return Uri.fromFile(cacheDir);
    }
}