package com.sprd.wallpaperpicker;

import android.content.Context;
import android.content.res.TypedArray;

import com.sprd.common.util.LogUtils;

public class WallpaperUtil {
    private static final boolean DEBUG = LogUtils.DEBUG_ALL;
    private static final String TAG = "WallpaperUtil";
    private static int mResIds[] = null;

    public static void initWallpaperResArray(Context context) {
        if (mResIds == null) {
            TypedArray typedArray = context.getResources().obtainTypedArray(R.array.wallpapers);
            int defValue = -1;
            mResIds = new int[typedArray.length()];
            for (int i = 0; i < mResIds.length; i++) {
                mResIds[i] = typedArray.getResourceId(i, defValue);
            }
            typedArray.recycle();
        }
    }

    public static int getWallpaperRes(Context context, int position) {
        if (mResIds == null) {
            initWallpaperResArray(context);
        }
        if (position >= mResIds.length) {
            if (DEBUG) LogUtils.i(TAG, "getWallpaperRes position too big, position = " + position + ",mResIds.length = " + mResIds.length);
            return 0;
        }
        return mResIds[position];
    }
    public static int getWallpaperResCount(Context context) {
        if (mResIds == null) {
            initWallpaperResArray(context);
        }
        return mResIds.length;
    }
}
