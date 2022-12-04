package com.sprd.classichome;

import android.app.Application;
import android.content.Intent;
import android.content.IntentFilter;

import com.sprd.classichome.model.HomeMonitorCallbacks;
import com.sprd.classichome.model.LauncherModel;
import com.sprd.common.util.LogUtils;
import com.sprd.common.util.TextToSpeechUtils;

/**
 * Created by SPRD on 10/1/17.
 */
public class HomeApplication extends Application {
    private static final String TAG = "HomeApplication";

    private LauncherModel mModel;
    private static HomeApplication INSTANCE;

    public static HomeApplication getInstance() {
        if (INSTANCE == null) {
            throw new RuntimeException("HomeApplication is not running!");
        }
        return INSTANCE;
    }

    public HomeApplication() {
        super();
        INSTANCE = this;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        if (LogUtils.DEBUG) LogUtils.d(TAG, "HomeApplication initiated");

        mModel = new LauncherModel(this);

        // Register intent receivers
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_TIME_TICK);
        filter.addAction(Intent.ACTION_TIME_CHANGED);
        filter.addAction(Intent.ACTION_TIMEZONE_CHANGED);

        filter.addAction(Intent.ACTION_LOCALE_CHANGED);
        filter.addAction(Intent.ACTION_CONFIGURATION_CHANGED);
        registerReceiver(mModel, filter);

        filter = new IntentFilter(Intent.ACTION_PACKAGE_ADDED);
        filter.addAction(Intent.ACTION_PACKAGE_REMOVED);
        filter.addAction(Intent.ACTION_PACKAGE_CHANGED);
        filter.addDataScheme("package");
        registerReceiver(mModel, filter);

        //init TTS
        TextToSpeechUtils.getInstance(this);
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        unregisterReceiver(mModel);
        INSTANCE = null;
    }

    public LauncherModel setHomeCallback(HomeMonitorCallbacks homeMonitorCallbacks) {
        mModel.addCallback(homeMonitorCallbacks);
        return mModel;
    }

    public void removeHomeCallback(HomeMonitorCallbacks homeMonitorCallbacks) {
        mModel.removeCallback(homeMonitorCallbacks);
    }

    public LauncherModel getModel() {
        return mModel;
    }
}
