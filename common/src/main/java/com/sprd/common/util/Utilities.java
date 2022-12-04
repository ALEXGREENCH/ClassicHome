package com.sprd.common.util;

import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Process;
import android.os.UserHandle;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.widget.Toast;

import com.sprd.simple.launcher.common.R;

/**
 * Created by SPRD on 12/26/17.
 */
public class Utilities {
    private static final String TAG = "Utilities";

    public static final ComponentName CALL_LOG = new ComponentName("com.android.dialer",
            "com.android.dialer.calllog.CallLogActivity");

    /**
     * Creates the application intent based on a component name and various launch flags.
     *
     * @param context the class name of the component representing the intent
     */
    public static Intent constructLauncherIntent(Context context, String pkg, String cls) {
        Intent intent = null;
        if(!TextUtils.isEmpty(pkg) && !TextUtils.isEmpty(cls)) {
            ComponentName component = new ComponentName(pkg, cls);
            intent = new Intent(Intent.ACTION_MAIN);
            intent.addCategory(Intent.CATEGORY_LAUNCHER);
            intent.setComponent(component);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
            return intent;
        } else if(context != null && !TextUtils.isEmpty(pkg) && TextUtils.isEmpty(cls)){
            intent = getLaunchIntentForPackage(context, pkg);
        }
        return intent;
    }

    private static Intent getLaunchIntentForPackage(Context context, String pkg) {
        if (context == null || TextUtils.isEmpty(pkg)) {
            return null;
        }

        try {
            PackageManager pm = context.getPackageManager();
            return pm.getLaunchIntentForPackage(pkg);
        } catch (Exception e){
            LogUtils.d(TAG,"constructLauncherIntent error!");
        }
        return null;
    }

    @SuppressWarnings("unused")
    public static Drawable loadAppIcon(Context context, ComponentName cn){
        Drawable icon = null;
        if (context == null) {
            LogUtils.e(TAG, "loadAppIcon input error!!!");
            return null;
        }

        PackageManager pm = context.getPackageManager();
        try {
            icon = pm.getActivityInfo(cn,0).loadIcon(pm);
            if (icon == null) {
                icon = pm.getApplicationIcon(cn.getPackageName());
            }
        } catch (Exception e) {
            LogUtils.w(TAG, "Can't loadAppIcon for " + cn, e);
        }

        if (icon == null) {
            icon = pm.getDefaultActivityIcon();
        }

        return icon;
    }

    public static CharSequence loadAppLabel(Context context,  ComponentName cn){
        CharSequence title = "";
        if(context == null || cn == null){
            return title;
        }

        PackageManager pm = context.getPackageManager();
        try {
            title = pm.getActivityInfo(cn,0).loadLabel(pm);
            if (title == null) {
                title = pm.getApplicationLabel(pm.getApplicationInfo(cn.getPackageName(),0));
            }
        } catch (Exception e){
            LogUtils.w(TAG,"loadAppLabel failed.", e);
            title = "";
        }

        return title;
    }

    public static boolean startActivity(Context context, Intent intent) {
        if(context == null || intent == null) {
            return false;
        }

        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        try {
            context.startActivity(intent);
            return true;
        } catch (SecurityException e) {
            LogUtils.e(TAG, "startActivity failed", e);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(context, R.string.activity_not_found, Toast.LENGTH_SHORT).show();
        }

        return false;
    }

    public static boolean startActivity(Context context, String pkg) {
        Intent intent = getLaunchIntentForPackage(context, pkg);
        return intent != null && startActivity(context, intent);
    }

    public static boolean startActivity(Context context, ComponentName cn) {
        Intent intent = constructLauncherIntent(context, cn.getPackageName(), cn.getClassName());
        return intent != null && startActivity(context, intent);
    }

    public static void sendBroadcast(Context context, Intent intent, boolean fast) {
        if (context != null && intent != null) {
            if (fast) {
                intent.addFlags(Intent.FLAG_RECEIVER_FOREGROUND);
            }
            context.sendBroadcast(intent);
            //noinspection UnnecessaryToStringCall
            LogUtils.d(TAG, "sendBroadcast success, bc:" + intent.toString());
        }
    }

    @SuppressWarnings("unused")
    public static float dpiFromPx(Context context, int px){
        if (context == null || px <= 0) {
            throw new RuntimeException("dpiFromPx, input error, px:" + px);
        }
        DisplayMetrics dm = context.getResources().getDisplayMetrics();
        float densityRatio = (float) dm.densityDpi / DisplayMetrics.DENSITY_DEFAULT;
        return (px / densityRatio);
    }

    public static UserHandle getNoEmptyUser(UserHandle user) {
        return user == null ? Process.myUserHandle() : user;
    }
}
