package com.example.mad_project.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;

import com.google.android.material.imageview.ShapeableImageView;
import com.example.mad_project.R;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class ImageUtils {
    
    public static Uri getImageUriFromBitmap(Context context, Bitmap bitmap) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(context.getContentResolver(), bitmap, "Title", null);
        return Uri.parse(path);
    }
    
    public static String saveImageToInternalStorage(Context context, Uri imageUri) {
        try {
            File directory = new File(context.getFilesDir(), "profile_images");
            if (!directory.exists()) {
                directory.mkdirs();
            }

            String fileName = "profile_" + System.currentTimeMillis() + ".jpg";
            File file = new File(directory, fileName);

            try (InputStream inputStream = context.getContentResolver().openInputStream(imageUri);
                 OutputStream outputStream = new FileOutputStream(file)) {
                byte[] buffer = new byte[1024];
                int length;
                while ((length = inputStream.read(buffer)) > 0) {
                    outputStream.write(buffer, 0, length);
                }
                return file.getAbsolutePath();
            }
        } catch (IOException e) {
            Log.e("ImageUtils", "Error saving image", e);
            return null;
        }
    }
    
    public static void loadProfileImage(Context context, ShapeableImageView imageView, String imagePath) {
        if (imagePath != null && !imagePath.isEmpty()) {
            try {
                Uri imageUri = Uri.parse(imagePath);
                imageView.setImageURI(imageUri);
                imageView.clearColorFilter();
                return;
            } catch (Exception e) {
                Log.e("ImageUtils", "Error loading image", e);
            }
        }
        setDefaultProfileImage(context, imageView);
    }

    private static void setDefaultProfileImage(Context context, ShapeableImageView imageView) {
        imageView.setImageResource(R.drawable.baseline_person_24);
        imageView.setColorFilter(context.getResources().getColor(R.color.white, context.getTheme()));
    }
} 