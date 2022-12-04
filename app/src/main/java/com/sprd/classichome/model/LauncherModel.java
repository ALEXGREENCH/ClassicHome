/*
 * Copyright (C) 2008 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.sprd.classichome.model;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.TypedArray;
import android.content.res.XmlResourceParser;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Process;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Xml;

import com.sprd.classichome.AppItemInfo;
import com.sprd.classichome.util.AppsSort;
import com.sprd.classichome.util.ComponentKey;
import com.sprd.classichome.util.IconUtilities;
import com.sprd.common.FeatureOption;
import com.sprd.common.util.LogUtils;
import com.sprd.simple.launcher2.R;

import org.xmlpull.v1.XmlPullParser;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Maintains in-memory state of the Launcher. It is expected that there should be only one
 * LauncherModel object held in a static. Also provide APIs for updating the database state
 * for the Launcher.
 */
public class LauncherModel extends BroadcastReceiver {
    static final String TAG = "Gridhome.Model";

    static final boolean DEBUG_LOADER = LogUtils.DEBUG_LOADER;
    private static final boolean DEBUG_RECEIVER = LogUtils.DEBUG;

    private int mPreDate;

    private static final String XML_ITEM_TAG = "App";
    private final Object mLock = new Object();
    @SuppressWarnings("FieldMayBeFinal")
    private ArrayList<WeakReference<HomeMonitorCallbacks>> mCallbacks = new ArrayList<>();

    //Read only: include the xml customize_apps.xml
    public static final HashMap<ComponentKey, AppItemInfo> mBgCustomizeAppsList = new HashMap<>();

    // < only access in worker thread include the all installed apps>
    public static final AllAppsList mBgAllAppsList = new AllAppsList();

    //Read only: include the xml customize_apps.xml
    private static final ArrayList<AppItemInfo> mMainMenuApps = new ArrayList<>();

    // < only access in worker thread include the all installed apps>
    private static final ArrayList<AppItemInfo> mExtraApps = new ArrayList<>();


    public static boolean isLoaded;

    @SuppressWarnings("FieldMayBeFinal")
    private LoaderTask mLoaderTask;
    private static final Handler mMainHandler = new Handler();
    private static final HandlerThread sWorkerThread = new HandlerThread("launcher-loader");

    static {
        sWorkerThread.start();
    }

    private static final Handler sWorker = new Handler(sWorkerThread.getLooper());
    private boolean mNeedForceLoad = true;

    public LauncherModel(Context context) {
        mLoaderTask = new LoaderTask(context);
        runOnWorkerThread(mLoaderTask);
    }

    public void addCallback(HomeMonitorCallbacks cb) {
        synchronized (mLock) {
            if (cb != null) {
                mCallbacks.add(new WeakReference<>(cb));
            }
        }
    }

    public void removeCallback(HomeMonitorCallbacks cb) {
        ArrayList<WeakReference<HomeMonitorCallbacks>> removeCallbacks
                = new ArrayList<>();
        for (WeakReference<HomeMonitorCallbacks> callback : mCallbacks) {
            if (callback.get() == cb) {
                removeCallbacks.add(callback);
            }
        }
        mCallbacks.removeAll(removeCallbacks);
    }

    private void onDateChanged() {
        for (WeakReference<HomeMonitorCallbacks> callback : mCallbacks) {
            HomeMonitorCallbacks cb = callback.get();
            if (cb != null) {
                cb.onDateChanged();
            }
        }
    }

    private void notifyAppsUpdated() {
        for (WeakReference<HomeMonitorCallbacks> callback : mCallbacks) {
            HomeMonitorCallbacks cb = callback.get();
            if (cb != null) {
                cb.notifyAppsUpdated();
            }
        }
    }

    private void runOnMainThread(Runnable r) {
        if (sWorkerThread.getThreadId() == Process.myTid()) {
            // If we are on the worker thread, post onto the main handler
            //noinspection ConstantConditions
            if (mMainHandler != null) {
                mMainHandler.post(r);
            }
        } else {
            r.run();
        }
    }

    /**
     * Runs the specified runnable immediately if called from the worker thread, otherwise it is
     * posted on the worker thread handler.
     */
    private static void runOnWorkerThread(Runnable r) {
        if (sWorkerThread.getThreadId() == Process.myTid()) {
            r.run();
        } else {
            // If we are not on the worker thread, then post to the worker handler
            sWorker.post(r);
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (DEBUG_RECEIVER) LogUtils.d(TAG, "onReceive intent=" + intent);

        final String action = intent.getAction();
        if (Intent.ACTION_TIME_TICK.equals(action)
                || Intent.ACTION_TIME_CHANGED.equals(action)
                || Intent.ACTION_TIMEZONE_CHANGED.equals(action)) {
            int curDate = Calendar.getInstance().get(Calendar.DATE);
            if (curDate != mPreDate) {
                mPreDate = curDate;
                onDateChanged();
            }
        } else if (Intent.ACTION_LOCALE_CHANGED.equals(action)
                || Intent.ACTION_CONFIGURATION_CHANGED.equals(action)) {
            mNeedForceLoad = true;
            runOnWorkerThread(mLoaderTask);
        } else if (Intent.ACTION_PACKAGE_ADDED.equals(action)
                || Intent.ACTION_PACKAGE_REMOVED.equals(action)
                || Intent.ACTION_PACKAGE_CHANGED.equals(action)) {
            final String pkgName = intent.getData() != null ?
                    intent.getData().getSchemeSpecificPart() : null;
            if (pkgName != null) {
                if (Intent.ACTION_PACKAGE_ADDED.equals(action)) {
                    mBgAllAppsList.addPackage(context, pkgName, null);
                }
                if (Intent.ACTION_PACKAGE_REMOVED.equals(action)) {
                    mBgAllAppsList.removePackage(context, pkgName, null);
                }
                if (Intent.ACTION_PACKAGE_CHANGED.equals(action)) {
                    mBgAllAppsList.updatePackage(context, pkgName, null);
                }
                if (!mLoaderTask.isLoading()) {
                    runOnWorkerThread(mLoaderTask);
                }
            }
        }
    }

    /**
     * Runnable for the thread that loads the contents of the launcher:
     * - all apps icons
     */
    private class LoaderTask implements Runnable {
        @SuppressWarnings("FieldMayBeFinal")
        private Context mContext;
        private boolean mIsLoading;

        LoaderTask(Context context) {
            mContext = context;
            mIsLoading = true;
        }

        boolean isLoading() {
            return mIsLoading;
        }

        public void run() {
            if (mNeedForceLoad) {
                loadCustomizeApp();
                loadAllApps();
            }
            verifyAllAppList();
            splitAllApps();
            mNeedForceLoad = false;
            mIsLoading = false;

            runOnMainThread(LauncherModel.this::notifyAppsUpdated);
        }

        /**
         * Loads the list of installed applications.
         */
        private void loadAllApps() {
            if (DEBUG_LOADER) LogUtils.d(TAG, "loadAllApps");
            synchronized (mBgAllAppsList.added) {
                PackageManager pm = mContext.getPackageManager();

                Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
                mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
                //not support multi-user
                @SuppressLint("QueryPermissionsNeeded")
                final List<ResolveInfo> apps = pm.queryIntentActivities(mainIntent, 0);

                mBgAllAppsList.clear();
                for (ResolveInfo app : apps) {
                    AppItemInfo appInfo = new AppItemInfo(
                            app.loadLabel(pm),
                            app.loadIcon(pm),
                            app.activityInfo.applicationInfo.packageName,
                            app.activityInfo.name);
                    mBgAllAppsList.added.add(appInfo);
                }
            }
            isLoaded = true;
        }

        /**
         * Parser the list of xml config applications.
         */
        private void loadCustomizeApp() {
            if (DEBUG_LOADER) LogUtils.d(TAG, "loadCustomizeApp:");
            synchronized (mBgCustomizeAppsList) {
                mBgCustomizeAppsList.clear();
                try {
                    XmlResourceParser parser = mContext.getResources().getXml(R.xml.customize_apps);
                    AttributeSet attrs = Xml.asAttributeSet(parser);

                    int eventType = parser.getEventType();
                    while (eventType != XmlPullParser.END_DOCUMENT) {
                        if (eventType == XmlPullParser.START_TAG) {
                            String tagName = parser.getName();
                            if (XML_ITEM_TAG.equals(tagName)) {
                                final TypedArray a = mContext.obtainStyledAttributes(attrs, R.styleable.AppInfo);
                                String pkgName = a.getString(R.styleable.AppInfo_pkgName);
                                String clsName = a.getString(R.styleable.AppInfo_clsName);
                                int position = a.getInteger(R.styleable.AppInfo_position, AppItemInfo.POSITION_INVALID);
                                Drawable icon = a.getDrawable(R.styleable.AppInfo_icon);
                                CharSequence label = a.getString(R.styleable.AppInfo_label);
                                String group = a.getString(R.styleable.AppInfo_group);

                                // package name must not be null or empty
                                if (pkgName != null) {
                                    AppItemInfo appInfo = new AppItemInfo(label, icon, pkgName, clsName, group, position);
                                    mBgCustomizeAppsList.put(appInfo.getComponentKey(), appInfo);
                                }
                                a.recycle();
                            }
                        }
                        eventType = parser.next();
                    }
                } catch (Exception e) {
                    LogUtils.w(TAG, "parse xml failed", e);
                }
            }
        }

        private void verifyAllAppList() {
            verifyRemovePackageActivities();
            verifyAddAppInfo();
        }

        private void verifyAddAppInfo() {
            synchronized (mBgAllAppsList.data) {
                if (!mBgAllAppsList.added.isEmpty()) {
                    synchronized (mBgAllAppsList.added) {
                        for (AppItemInfo apInfo : mBgAllAppsList.added) {
                            AppItemInfo cusAppInfo = getCusAppInfo(apInfo);
                            if (cusAppInfo != null) {
                                mergeItemInfo(cusAppInfo, apInfo);
                            }
                            if(FeatureOption.SPRD_REMOVE_APP_ICON_TRANSPADDING_SUPPORT){
                                apInfo.icon = IconUtilities.removeDrawableTransPadding(mContext, apInfo.icon);
                            }
                            mBgAllAppsList.put(apInfo);
                        }
                        mBgAllAppsList.added.clear();
                    }
                }
            }
        }

        private AppItemInfo getCusAppInfo(AppItemInfo info){
            AppItemInfo cusAppInfo = mBgCustomizeAppsList.get(info.getComponentKey());
            if (cusAppInfo == null) {
                ComponentKey cpk = new ComponentKey(new ComponentName(info.pkgName, ""), info.user);
                cusAppInfo = mBgCustomizeAppsList.get(cpk);
            }
            return cusAppInfo;
        }

        private void verifyRemovePackageActivities() {
            HashMap<ComponentKey, AppItemInfo> allApps = new HashMap<>(mBgAllAppsList.data);
            synchronized (mBgAllAppsList.data) {
                if (!mBgAllAppsList.removed.isEmpty()) {
                    synchronized (mBgAllAppsList.removed) {
                        for (ComponentKey cpk : mBgAllAppsList.removed) {
                            for (Map.Entry<ComponentKey, AppItemInfo> entry : allApps.entrySet()) {
                                AppItemInfo appInfo = entry.getValue();
                                final String pkgName = cpk.componentName != null ?
                                        cpk.componentName.getPackageName() : null;
                                if (appInfo.pkgName.equals(pkgName) && appInfo.user.equals(cpk.user)) {
                                    mBgAllAppsList.remove(appInfo);
                                }
                            }
                        }
                        mBgAllAppsList.removed.clear();
                    }

                }
            }
        }

        private void splitAllApps() {
            ArrayList<AppItemInfo> mainMenuApps = new ArrayList<>();
            ArrayList<AppItemInfo> extraApps = new ArrayList<>();
            HashMap<ComponentKey, AppItemInfo> allApps = mBgAllAppsList.data;
            synchronized (mBgAllAppsList.data) {
                for (Map.Entry<ComponentKey, AppItemInfo> entry : allApps.entrySet()) {
                    AppItemInfo appInfo = entry.getValue();

                    if (!AppItemInfo.GROUP_HIDE.equals(appInfo.group)) {
                        if (AppItemInfo.GROUP_EXTRA.equals(appInfo.group)) {
                            extraApps.add(appInfo);
                        } else if (AppItemInfo.GROUP_MAIN_MENU.equals(appInfo.group)) {
                            mainMenuApps.add(appInfo);
                        }
                    }
                }
                addMainMenuApps(mainMenuApps);
                addExtraApps(extraApps);
            }
        }


        private void addMainMenuApps(ArrayList<AppItemInfo> apps) {
            if (apps != null && apps.size() > 0) {
                synchronized (mMainMenuApps) {
                    AppsSort.sort(apps, AppsSort.SortType.NAME);
                    AppsSort.verifyPosition(apps);
                    mMainMenuApps.clear();
                    mMainMenuApps.addAll(apps);
                }
            }
        }

        private void addExtraApps(ArrayList<AppItemInfo> apps) {
            if (apps != null && apps.size() > 0) {
                synchronized (mExtraApps) {
                    AppsSort.sort(apps, AppsSort.SortType.NAME);
                    AppsSort.verifyPosition(apps);
                    mExtraApps.clear();
                    mExtraApps.addAll(apps);
                }
            }
        }

        private void mergeItemInfo(AppItemInfo src, AppItemInfo des) {
            if (src != null) {
                if (!TextUtils.isEmpty(src.title)) {
                    des.title = src.title;
                }
                if (src.icon != null) {
                    des.icon = src.icon;
                    des.iconCustomized = true;
                }
                if (src.position != AppItemInfo.POSITION_INVALID) {
                    des.position = src.position;
                }
                if (src.isGroupValid(src.group)) {
                    des.group = src.group;
                }
            }
        }
    }


    public static ArrayList<AppItemInfo> getExtraAppsList() {
        return new ArrayList<>(mExtraApps);
    }

    public static ArrayList<AppItemInfo> getMainMenuAppsList() {
        return new ArrayList<>(mMainMenuApps);
    }
}
