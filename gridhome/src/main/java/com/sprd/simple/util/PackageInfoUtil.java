package com.sprd.simple.util;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;

import com.sprd.common.util.HomeConstants;
import com.sprd.common.util.LogUtils;
import com.sprd.simple.launcher.Launcher;
import com.sprd.simple.launcher.gridhome.R;
import com.sprd.simple.model.AppInfo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static android.content.Context.TELEPHONY_SERVICE;

/**
 * Created by SPRD on 8/16/2016.
 */
public class PackageInfoUtil {
    static final String TAG = "PackageInfoUtil";

    private static AppInfo getAppInfo(ResolveInfo app, PackageManager pm) {
        if (app == null) {
            return null;
        }
        AppInfo appInfo = new AppInfo();
        appInfo.setAppLabel((String) app.loadLabel(pm));
        appInfo.setAppDrawableIcon(app.loadIcon(pm));
        appInfo.setAppPkg(app.activityInfo.packageName);
        appInfo.setAppClassName(app.activityInfo.name);
        return appInfo;
    }

    /**
     * Find all applications that hold the launcher attribution.
     *
     * @param context
     * @return all apps that hold the launcher attribution.
     */
    public static List<ResolveInfo> getAllLauncherAttrApplication(Context context) {
        Intent intent = new Intent(Intent.ACTION_MAIN, null);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        List<ResolveInfo> resolveInfos = context.getPackageManager().queryIntentActivities(intent, 0);
        Collections.sort(resolveInfos, new ResolveInfo.DisplayNameComparator(context.getPackageManager()));
        return resolveInfos;
    }

    /**
     * Only for appfolder, find all applications that hold the launcher attribution.
     *
     * @param context
     * @return all apps For AppFolder.
     */
    public static List<AppInfo> getLauncherAttrAppForAppFolder(Context context) {
        List<ResolveInfo> resolveInfos = getAllLauncherAttrApplication(context);

        // find all apps on desktop and tools, then add to set.
        Set<String> notNeedExistAppFolder = new HashSet<String>();
        notNeedExistAppFolder.addAll(getDesktopSupportApps(context));
        notNeedExistAppFolder.addAll(getToolApps(context));

        // remove those apks which they do not need to show in appFolder.
        return getAppsByFilter(context, resolveInfos, notNeedExistAppFolder);
    }

    /**
     * Find all apk on desktop
     *
     * @param context
     * @return Apps should show on the desktop
     */
    public static Set<String> getDesktopNeedApps(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(HomeConstants.SP_NAME, Context.MODE_PRIVATE);
        Set<String> packageNameSet = sharedPreferences.getStringSet(HomeConstants.HOME_APPS_NAMES, null);
        if (packageNameSet == null) {
            packageNameSet = new HashSet<String>();
        }
        // add self to set
        packageNameSet.add(context.getPackageName());
        return packageNameSet;
    }

    /**
     * Find all apk support show on desktop
     *
     * @param context
     * @return Apps support show on the desktop
     */
    public static Set<String> getDesktopSupportApps(Context context){
        Set<String> packageNameSet = new HashSet<String>();
        packageNameSet.addAll(getWorkspaceScreenApps(context,Launcher.sDEFAULT_WORKSPACE));
        packageNameSet.addAll(getWorkspaceScreenApps(context,Launcher.sTHIRD_WORKSPACE));
        packageNameSet.addAll(getWorkspaceScreenApps(context,Launcher.sFOURTH_WORKSPACE));
        return packageNameSet;
    }

    public static Set<String> getWorkspaceScreenApps(Context context,int workspaceScreen){
        Set<String> packageNameSet = new HashSet<String>();
        int resId = getWorkspaceArrayId(workspaceScreen);
        if(resId != 0) {
            TypedArray typedArray = context.getResources().obtainTypedArray(resId);
            int defValue = -1;
            int index = 0;
            for (int i = 0; i < typedArray.length(); i++) {
                TypedArray tempArray = context.getResources().obtainTypedArray(typedArray.getResourceId(i, defValue));
                String pkgName = context.getString(tempArray.getResourceId(index, defValue));
                if (TextUtils.isEmpty(pkgName)) {
                    continue;
                }
                packageNameSet.add(pkgName);
            }
        }
        Log.d(TAG,"getWorkspaceScreenApps packageNameSet = "+packageNameSet);
        return packageNameSet;
    }

    public static int getWorkspaceArrayId(int workspaceScreen){
        int resId = 0;
        if(workspaceScreen == Launcher.sDEFAULT_WORKSPACE){
            resId = R.array.default_page_app_names;
        }else if(workspaceScreen == Launcher.sTHIRD_WORKSPACE){
            resId = R.array.third_page_app_names;
        }else if(workspaceScreen == Launcher.sFOURTH_WORKSPACE){
            resId = R.array.fourth_page_app_names;
        }
        return resId;
    }

    /**
     * Find all apk in toolFolder.
     *
     * @param context
     * @return ToolApps
     */
    public static List<String> getToolApps(Context context) {
        String[] toolApps = context.getResources().getStringArray(R.array.tool_apps);
        return Arrays.asList(toolApps);
    }

    /**
     * remove those apks which they do not need to show in appFolder.
     *
     * @param context
     * @param sources
     * @param needRemoveSet
     * @return apps after filter
     */
    private static List<AppInfo> getAppsByFilter(Context context, List<ResolveInfo> sources,
            Set<String> needRemoveSet) {
        List<AppInfo> appInfos = new ArrayList<AppInfo>();
        for (ResolveInfo app : sources) {
            if (app == null) {
                continue;
            }
            if (!needRemoveSet.contains(app.activityInfo.packageName)) {
                AppInfo appInfo = getAppInfo(app, context.getPackageManager());
                if (appInfo != null) {
                    appInfos.add(appInfo);
                }
            }
        }
        Log.d(TAG,"appInfos Size = "+ appInfos.size());
        return appInfos;
    }

    /**
     * judge airplane mode. 0 is on, 1 is off.
     *
     * @param context
     * @return true is on, or off.
     */
    public static boolean isAirplaneModeOn(Context context) {
        return Settings.System.getInt(context.getContentResolver(), Settings.System.AIRPLANE_MODE_ON, 0) != 0;
    }

    /**
     * Find app by packageName only.
     *
     * @param context
     * @param packageName
     * @return ResolveInfo
     */
    private static ResolveInfo findAppInfoByPackageName(Context context, String packageName) {
        Intent intent = new Intent();
        intent.setPackage(packageName);
        List<ResolveInfo> tempAllApps = context.getPackageManager().queryIntentActivities(intent, 0);
        return tempAllApps != null && tempAllApps.size() > 0 ? tempAllApps.get(0) : null;
    }

    /**
     * To determine whether the Sim card has been inserted.
     *
     * @param context
     * @return if has been inserted, return true.
     */
    public static boolean hasExistSimCard(Context context) {
        TelephonyManager tm = (TelephonyManager) context.getSystemService(TELEPHONY_SERVICE);
        String simSerialNumber = tm != null ? tm.getSimSerialNumber() : "";
        if (TextUtils.isEmpty(simSerialNumber)) {
            return false;
        }
        return true;
    }

    /**
     * find application name.
     * @param context
     * @param packageName
     * @param className
     * @return application name
     */
    public static String getApplicationName(Context context, String packageName, String className) {
        String applicationName = "";
        if (TextUtils.isEmpty(packageName) && TextUtils.isEmpty(className)) {
            LogUtils.w(TAG, "", new RuntimeException("getApplicationName error, packageName & className is Empty"));
            return applicationName;
        }
        PackageManager packageManager = context.getPackageManager();
        try {
            ActivityInfo activityInfo = packageManager.getActivityInfo(new ComponentName(packageName, className), 0);
            applicationName = activityInfo.loadLabel(packageManager).toString();
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        } finally {
            if (TextUtils.isEmpty(applicationName)) {
                try {
                    ApplicationInfo applicationInfo = packageManager.getApplicationInfo(packageName, 0);
                    applicationName = packageManager.getApplicationLabel(applicationInfo).toString();
                } catch (PackageManager.NameNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }
        return applicationName;
    }

    /**
     * obtain all app info on current page.
     * @param context
     * @param resId
     * @return app info list
     */
    public static List<AppInfo> loadAppInfos(Context context, int resId, boolean needAdd) {
        TypedArray typedArray = context.getResources().obtainTypedArray(resId);
        List<AppInfo> datas = new ArrayList<AppInfo>();
        int defValue = -1;
        int index;
        for (int i = 0; i < typedArray.length(); i++) {
            TypedArray tempArray = context.getResources().obtainTypedArray(typedArray.getResourceId(i, defValue));
            index = 0;
            AppInfo appInfo = new AppInfo();

            String packageName= context.getString(tempArray.getResourceId(index++, defValue));
            appInfo.setAppPkg(packageName);

            int resVaule = tempArray.getResourceId(index++, defValue);
            if(resVaule != -1) {
                appInfo.setAppIcon(resVaule);
            }else{
                appInfo.setAppDrawableIcon(getAppIcon(context, packageName));
            }

            appInfo.setAppClassName(context.getString(tempArray.getResourceId(index++, defValue)));
            if (index < tempArray.length()) {
                appInfo.setBackground(tempArray.getResourceId(index, defValue));
            }
            String appName = getApplicationName(context, appInfo.getAppPkg(), appInfo.getAppClassName());
            if (TextUtils.isEmpty(appName) && !needAdd) {
                continue;
            }
            appInfo.setAppLabel(appName);
            datas.add(appInfo);

            tempArray.recycle();
        }
        typedArray.recycle();
        return datas;
    }

    /**
     * find application icon.
     * @param context
     * @param packageName
     * @return app icon
     */
    public static Drawable getAppIcon(Context context, String packageName){
        ResolveInfo app = findAppInfoByPackageName(context, packageName);
        if(app != null){
            return app.loadIcon(context.getPackageManager());
        }else{
            return null;
        }
    }

    /**
     * obtain app info on current position.
     * @param context
     * @param resId
     * @param position
     * @param appLabel
     * @return app info.
     */
    public static AppInfo loadAppInfo(Context context, int resId, int position, String appLabel) {
        int defValue = -1;
        TypedArray typedArray = context.getResources().obtainTypedArray(resId);
        AppInfo appInfo = new AppInfo();
        appInfo.setBackground(typedArray.getResourceId(position, defValue));
        appInfo.setAppLabel(appLabel);
        typedArray.recycle();
        return appInfo;
    }
}
