package com.sprd.common.util;

import android.annotation.SuppressLint;
import android.text.TextUtils;

import java.lang.reflect.Method;

/**
 * Created by SPRD on 6/13/17.
 */

public class SystemPropertiesUtils {

    private static final String TAG = "SystemPropertiesUtils";

    private static Class<?> mClassType = null;
    private static Method mGetMethod = null;
    private static Method mGetBooleanMethod = null;


    @SuppressLint("PrivateApi")
    private static Class<?> getSystemPropertiesClass() throws ClassNotFoundException {
        if (mClassType == null) {
            mClassType = Class.forName("android.os.SystemProperties");
        }
        return mClassType;
    }

    private static Method getMethod() throws Exception {
        if (mGetMethod == null) {
            Class<?> clazz = getSystemPropertiesClass();
            mGetMethod = clazz.getDeclaredMethod("get", String.class);
        }
        return mGetMethod;
    }

    private static Method getBooleanMethod() throws Exception {
        if (mGetBooleanMethod == null) {
            Class<?> clazz = getSystemPropertiesClass();
            mGetBooleanMethod = clazz.getDeclaredMethod("getBoolean", String.class, boolean.class);
        }
        return mGetBooleanMethod;
    }

    public static String get(String key, String def) {
        try {
            String value = (String) getMethod().invoke(null, key);
            if (!TextUtils.isEmpty(value)) {
                return value;
            }
        } catch (Exception e) {
            LogUtils.d(TAG, "Unable to read system properties");
        }

        return def;
    }

    public static boolean getBoolean(String key, boolean def) {
        boolean value = def;
        try {
            //noinspection ConstantConditions
            value = (Boolean) getBooleanMethod().invoke(null, key, def);
        } catch (Exception e) {
            LogUtils.d(TAG, "Unable to read system properties");
        }
        return value;
    }
}
