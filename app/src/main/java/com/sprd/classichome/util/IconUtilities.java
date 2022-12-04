package com.sprd.classichome.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.NinePatchDrawable;

/**
 * Created by SPRD on 9/27/17.
 */
public final class IconUtilities {
    @SuppressWarnings("unused")
    private static final String TAG = "Gridhome.IconUtilities";

    //Scan icon step, the speed is faster if the number is bigger
    public static int ICON_SCAN_STEP = 3;

    private enum StartScanBorderType {LEFT, TOP, RIGHT, BOTTOM}


    public static Drawable removeDrawableTransPadding(Context context, Drawable res) {
        if (res == null) {
            return null;
        }
        Bitmap bitmap = IconUtilities.drawable2Bitmap(res);
        int padding = IconUtilities.getBitmapMinTransparentPadding(
                bitmap,
                IconUtilities.ICON_SCAN_STEP);

        return IconUtilities.bitmap2Drawable(context,
                IconUtilities.cropBitmap(bitmap, padding));
    }

    /**
     * Get the min padding of the transparent space around the bitmap .
     */
    public static int getBitmapMinTransparentPadding(Bitmap bitmap, int gap) {
        int lPadding;
        int rPadding;
        int tPadding;
        int bPadding;
        int minPadding;

        if (bitmap == null || bitmap.isRecycled()) {
            return 0;
        }
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();

        final int left = 0;
        final int top = 0;
        final int right = width - 1;
        final int bottom = height - 1;


        //Get the left min padding of the transparent space

        Rect scanRect = new Rect(left, top, right/2, bottom);
        lPadding = getBitmapTransparentDepth(bitmap, scanRect, StartScanBorderType.LEFT, gap);
        if(lPadding == 0) {
            return 0;
        }
        minPadding = lPadding;

        //Get the top min padding of the transparent space
        scanRect.set(minPadding, top, right , minPadding);
        tPadding = getBitmapTransparentDepth(bitmap, scanRect, StartScanBorderType.TOP, gap);
        if(tPadding == 0) {
            return 0;
        }
        minPadding = Math.min(minPadding, tPadding);


        //Get the right min padding of the transparent space
        scanRect.set(right - minPadding, minPadding, right, bottom);
        rPadding = getBitmapTransparentDepth(bitmap, scanRect, StartScanBorderType.RIGHT, gap);
        if(rPadding == 0) {
            return 0;
        }
        minPadding = Math.min(minPadding, rPadding);

        //Get the bottom min padding of the transparent space
        scanRect.set(minPadding, bottom - minPadding, right - minPadding, bottom);
        bPadding = getBitmapTransparentDepth(bitmap, scanRect, StartScanBorderType.BOTTOM, gap);
        minPadding = Math.min(minPadding, bPadding);
        return minPadding;
    }


    private static int getBitmapTransparentDepth(Bitmap bitmap, Rect scanRect,
                                                 StartScanBorderType type, int gap) {
        if (bitmap == null || scanRect == null || scanRect.isEmpty()) {
            return 0;
        }

        switch (type) {
            case LEFT:
                for (int x = scanRect.left; x <= scanRect.right; x++) {
                    for (int y = scanRect.top; y <= scanRect.bottom; y += gap) {
                        if (bitmap.getPixel(x,y) != Color.TRANSPARENT) {
                            return Math.abs(x - scanRect.left);
                        }
                    }
                }
                break;
            case TOP:
                for (int y = scanRect.top; y <= scanRect.bottom; y++) {
                    for (int x = scanRect.left; x <= scanRect.right; x += gap) {
                        if (bitmap.getPixel(x,y) != Color.TRANSPARENT) {
                            return Math.abs(y - scanRect.top);
                        }
                    }
                }
                break;
            case RIGHT:
                for (int x = scanRect.right; x >= scanRect.left; x--) {
                    for (int y = scanRect.top; y <= scanRect.bottom; y += gap) {
                        if (bitmap.getPixel(x,y) != Color.TRANSPARENT) {
                            return Math.abs(x - scanRect.right);
                        }
                    }
                }
                break;
            case BOTTOM:
                for (int y = scanRect.bottom; y >= scanRect.top; y--) {
                    for (int x = scanRect.left; x <= scanRect.right; x += gap) {
                        if (bitmap.getPixel(x,y) != Color.TRANSPARENT) {
                            return Math.abs(y - scanRect.bottom);
                        }
                    }
                }
                break;
        }
        return Integer.MAX_VALUE;
    }

    public static Bitmap cropBitmap(Bitmap bitmap, int padding) {
        int w = bitmap.getWidth() - 2*padding;
        int h = bitmap.getHeight() - 2*padding;
        return Bitmap.createBitmap(bitmap, padding, padding, w, h, null, false);
    }

    public static Bitmap drawable2Bitmap(Drawable drawable) {
        if (drawable instanceof BitmapDrawable) {
            return ((BitmapDrawable) drawable).getBitmap();
        } else if (drawable instanceof NinePatchDrawable) {
            Bitmap bitmap = Bitmap
                    .createBitmap(
                            drawable.getIntrinsicWidth(),
                            drawable.getIntrinsicHeight(),
                            drawable.getOpacity() != PixelFormat.OPAQUE ? Bitmap.Config.ARGB_8888
                                    : Bitmap.Config.RGB_565);
            Canvas canvas = new Canvas(bitmap);
            drawable.setBounds(0, 0, drawable.getIntrinsicWidth(),
                    drawable.getIntrinsicHeight());
            drawable.draw(canvas);
            return bitmap;
        } else {
            return null;
        }
    }

    public static Drawable bitmap2Drawable(Context context, Bitmap bitmap) {
        return new BitmapDrawable(context.getResources(), bitmap);
    }
}
