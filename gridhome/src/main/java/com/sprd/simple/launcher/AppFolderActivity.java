package com.sprd.simple.launcher;

import android.content.ActivityNotFoundException;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.widget.Toast;

import com.sprd.simple.launcher.gridhome.R;
import com.sprd.simple.model.AppInfo;
import com.sprd.simple.util.PackageInfoUtil;

import java.util.List;

public class AppFolderActivity extends BaseActivity<AppInfo> {
    private static final String TAG = "AppFolderActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE,
                R.layout.browser_title_bar);
    }

    @Override
    protected List<AppInfo> loadData() {
        return PackageInfoUtil.getLauncherAttrAppForAppFolder(this);
    }

    @Override
    protected void launchApp(int position) {
        try {
            startActivity(intentForPosition(position));
        } catch (ActivityNotFoundException e) {
            Log.i(TAG, "App not found");
            Toast.makeText(this, R.string.activity_not_found, Toast.LENGTH_LONG)
                    .show();
        }
    }

}
