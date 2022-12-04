package com.sprd.common.util;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;

import java.lang.reflect.Method;

/**
 * Created by SPRD on 9/29/2017.
 */
public class StatusBarUtils {
    private static final String TAG = "StatusBarUtils";
    private static final String STATUS_BAR_SERVICE = "statusbar";

    private static Class<?> mClassType = null;
    private static Method mGetMethod = null;


    private static Class<?> getStatusBarManagerClass() throws ClassNotFoundException {
        if (mClassType == null) {
            mClassType = Class.forName("android.app.StatusBarManager");
        }
        return mClassType;
    }

    private static Method getMethod() throws Exception {
        if (mGetMethod == null) {
            Class clazz = getStatusBarManagerClass();
            mGetMethod = clazz.getMethod("expandNotificationsPanel");
        }
        return mGetMethod;
    }

    @SuppressLint("WrongConstant")
    public static void expandNotificationsPanel(Context context) {
        try {
            getMethod().invoke(context.getSystemService(STATUS_BAR_SERVICE));
        } catch (Exception e) {
            LogUtils.w(TAG, "Unable expandNotificationsPanel", e);
        }
    }

    public static void setWindowStatusBarColor(Activity activity) {
        /*Window window = activity.getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.setStatusBarColor(activity.getResources().getColor(R.color.tools_status_bar_color));
        }*/
    }

    public static int getStatusBarHeight(Context context) {
        int result = 0;
        int resourceId = context.getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = context.getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }
}
