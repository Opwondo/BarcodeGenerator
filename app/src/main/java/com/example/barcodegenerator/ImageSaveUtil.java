package com.example.barcodegenerator;

import android.content.ContentValues;
import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ImageSaveUtil {

    public static boolean saveImageToGallery(Context context, Bitmap bitmap, String fileName) {
        if (bitmap == null) {
            Toast.makeText(context, "Failed to save image: Bitmap is null", Toast.LENGTH_SHORT).show();
            return false;
        }

        // For Android 10 (API 29) and above, use MediaStore
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            return saveImageMediaStore(context, bitmap, fileName);
        } else {
            // For older versions, use legacy method
            return saveImageLegacy(context, bitmap, fileName);
        }
    }

    private static boolean saveImageMediaStore(Context context, Bitmap bitmap, String fileName) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(MediaStore.Images.Media.DISPLAY_NAME, fileName);
        contentValues.put(MediaStore.Images.Media.MIME_TYPE, "image/png");
        contentValues.put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/BarcodeGenerator");

        Uri uri = context.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);

        if (uri == null) {
            Toast.makeText(context, "Failed to create new MediaStore record", Toast.LENGTH_SHORT).show();
            return false;
        }

        try (OutputStream outputStream = context.getContentResolver().openOutputStream(uri)) {
            if (outputStream != null) {
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
                Toast.makeText(context, "Image saved to Gallery!", Toast.LENGTH_LONG).show();
                return true;
            }
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(context, "Failed to save image: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
        return false;
    }

    private static boolean saveImageLegacy(Context context, Bitmap bitmap, String fileName) {
        String savedImagePath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
                + "/BarcodeGenerator/" + fileName;

        try {
            File file = new File(savedImagePath);

            // Create directories if they don't exist
            File parentDir = file.getParentFile();
            if (parentDir != null && !parentDir.exists()) {
                parentDir.mkdirs();
            }

            FileOutputStream outputStream = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
            outputStream.close();

            // Notify media scanner
            MediaStore.Images.Media.insertImage(context.getContentResolver(), file.getAbsolutePath(), file.getName(), file.getName());

            Toast.makeText(context, "Image saved to Gallery!", Toast.LENGTH_LONG).show();
            return true;

        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(context, "Failed to save image: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
        return false;
    }

    public static String generateFileName(String type, String format) {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        return type + "_" + format + "_" + timeStamp + ".png";
    }
}