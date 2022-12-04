package com.sprd.classichome.util;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.UserHandle;

import com.sprd.classichome.AppItemInfo;
import com.sprd.classichome.Home;
import com.sprd.classichome.mainmenu.MainMenuActivity;
import com.sprd.common.util.Utilities;
import com.sprd.simple.launcher2.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by SPRD on 9/27/17.
 */
public final class UtilitiesExt {

    public static void goHome(Context context) {
        Utilities.startActivity(context, getHomeComponentName(context));
    }

    public static void goMainMenu(Context context) {
        Utilities.startActivity(context, getMainMenuComponentName(context));
    }

    public static ComponentName getHomeComponentName(Context context) {
        return new ComponentName(context, Home.class);
    }

    public static ComponentName getMainMenuComponentName(Context context) {
        return new ComponentName(context, MainMenuActivity.class);
    }

    public static ComponentName getLFComponentName(Context context) {
        return ComponentName.unflattenFromString(
                context.getResources().getString(R.string.left_button_launch_app));
    }

    public static ComponentName getRTComponentName(Context context) {
        return ComponentName.unflattenFromString(
                context.getResources().getString(R.string.right_button_launch_app));
    }

    /**
     * Query the package manager for MAIN/LAUNCHER activities in the supplied package.
     */
    public static ArrayList<AppItemInfo> findActivitiesForPackage(Context context, String packageName, @SuppressWarnings("unused") UserHandle user) {
        final ArrayList<AppItemInfo> appItemInfos = new ArrayList<>();
        final PackageManager packageManager = context.getPackageManager();

        final Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        mainIntent.setPackage(packageName);

        @SuppressLint("QueryPermissionsNeeded")
        final List<ResolveInfo> apps = packageManager.queryIntentActivities(mainIntent, 0);

        if(apps != null && apps.size() > 0) {
            for (ResolveInfo info : apps) {
                AppItemInfo appItemInfo = new AppItemInfo(
                        info.loadLabel(packageManager),
                        info.loadIcon(packageManager),
                        info.activityInfo.applicationInfo.packageName,
                        info.activityInfo.name);

                appItemInfos.add(appItemInfo);
            }
        }
        return appItemInfos;
    }
}
