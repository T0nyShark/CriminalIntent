package com.industries.shark.criminalintent;


import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;

import java.io.File;

public class PictureUtils {

    public static Bitmap getScaledBitmap(String path, int destWidth, int destHeight){
           //Read in the dimensions of the image on disk

        BitmapFactory.Options options = new BitmapFactory.Options();

        options.inJustDecodeBounds = true;

        BitmapFactory.decodeFile(path, options);

        float scrWidth = options.outWidth;
        float scrHeight = options.outHeight;

        //Calculating scale ratio

        int inSampleSize = 1;

        if (scrHeight > destHeight || scrWidth > destWidth){
               float heightScale = scrHeight/destHeight;
               float widthScale = scrWidth/destWidth;

               inSampleSize = Math.round(heightScale > widthScale ? heightScale : widthScale);
        }

              options = new BitmapFactory.Options();
        options.inSampleSize = inSampleSize;

        //Read in and create final bitmap
        return BitmapFactory.decodeFile(path, options);

    }

    public static Bitmap getScaledBitmap(String path, Activity activity){
        Point size = new Point();

        activity.getWindowManager().getDefaultDisplay()
                .getSize(size);
        return getScaledBitmap(path, size.x, size.y);
    }

    public static Bitmap convertFileToBitmap(File file){
        if (!file.exists()||file.getAbsolutePath().isEmpty()){
            return null;
        }
        Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());

        return bitmap;

    }
}
