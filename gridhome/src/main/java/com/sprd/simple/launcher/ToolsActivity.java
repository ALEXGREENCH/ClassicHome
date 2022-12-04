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

/**
 * Created by SPDR on 2016/7/21.
 */
public class ToolsActivity extends BaseActivity<AppInfo> {
    private static final String TAG = "ToolsActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE,
                R.layout.tools_title_bar);
    }

    @Override
    protected List<AppInfo> loadData() {
        return PackageInfoUtil.loadAppInfos(this, R.array.tools_page_app_names, false);
    }

    @Override
    protected void launchApp(int position) {
        Log.i(TAG, "launchApp flag = " + position);
        try {
            startActivity(intentForPosition(position));
        } catch (ActivityNotFoundException e) {
            Log.i(TAG, "App not found");
            Toast.makeText(this, R.string.activity_not_found, Toast.LENGTH_LONG)
                    .show();
        }
    }

}
