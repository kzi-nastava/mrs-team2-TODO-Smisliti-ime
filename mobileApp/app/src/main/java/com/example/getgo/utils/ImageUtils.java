package com.example.getgo.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;

import androidx.core.content.res.ResourcesCompat;

import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;

public class ImageUtils {

    /**
     * Converts a drawable resource to a BitmapDescriptor for use with Google Maps markers.
     * Handles both vector drawables (XML) and bitmap drawables (PNG/JPG).
     */
    public static BitmapDescriptor getBitmapDescriptorFromDrawable(Context context, int drawableRes, int width, int height) {
        Drawable drawable = ResourcesCompat.getDrawable(context.getResources(), drawableRes, null);

        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);

        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }

    /**
     * Convenience method with default size
     */
    public static BitmapDescriptor getBitmapDescriptorFromDrawable(Context context, int drawableRes) {
        return getBitmapDescriptorFromDrawable(context, drawableRes, 60, 60);
    }
}