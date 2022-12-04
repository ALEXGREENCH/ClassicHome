package com.sprd.classichome;

import android.os.Bundle;

import com.sprd.classichome.model.LauncherModel;

import java.util.ArrayList;

/**
 * Created by SPRD on 2017/9/30.
 */

public class ExtrasActivity extends BaseListActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    ArrayList<AppItemInfo> getApps() {
        return LauncherModel.getExtraAppsList();
    }
}
