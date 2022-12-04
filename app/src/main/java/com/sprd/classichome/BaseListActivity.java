package com.sprd.classichome;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.sprd.PlatformHelper;
import com.sprd.android.support.featurebar.FeatureBarHelper;
import com.sprd.classichome.model.HomeMonitorCallbacks;
import com.sprd.classichome.model.LauncherModel;
import com.sprd.common.util.FeatureBarUtil;
import com.sprd.common.util.TextToSpeechUtils;
import com.sprd.common.util.Utilities;
import com.sprd.simple.launcher2.R;

import java.util.ArrayList;

/**
 * Created by SPRD on 2017/9/30.
 */

public abstract class BaseListActivity extends Activity implements
        AdapterView.OnItemSelectedListener,
        AdapterView.OnItemClickListener {

    private ListView mListView;
    protected BaseListAdapter mAdapter;
    protected LauncherModel mModel;

    @SuppressWarnings("FieldMayBeFinal")
    private HomeMonitorCallbacks mCallback = new HomeMonitorCallbacks() {
        @Override
        public void notifyAppsUpdated() {
            if (mAdapter != null) {
                mAdapter.notifyAppsUpdated(getApps());
            }
        }
    };
    private FeatureBarHelper mFeatureBarHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mModel = ((HomeApplication) getApplication()).setHomeCallback(mCallback);

        setContentView(R.layout.base_list_activity_main);
        mListView = findViewById(R.id.base_list_view);
        initListView();
        setSoftKey();
    }

    private void initListView() {
        if (mListView != null) {
            mAdapter = new BaseListAdapter(this);
            mAdapter.setApps(getApps());
            mListView.setAdapter(mAdapter);
            mListView.setOnItemClickListener(this);
            mListView.setOnItemSelectedListener(this);
        }
    }

    protected void setSoftKey() {
        if (PlatformHelper.isTargetBuild() && mFeatureBarHelper == null) {
            mFeatureBarHelper = new FeatureBarHelper(this);
        }
        FeatureBarUtil.hideSoftKey(mFeatureBarHelper, FeatureBarUtil.SoftKey.LFK);
    }

    abstract ArrayList<AppItemInfo> getApps();

    @Override
    protected void onDestroy() {
        super.onDestroy();

        ((HomeApplication) getApplication()).removeHomeCallback(mCallback);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (view != null && view.getTag() != null) {
            Intent intent = Utilities.constructLauncherIntent(this,
                    ((AppItemInfo) view.getTag()).pkgName,
                    ((AppItemInfo) view.getTag()).clsName);
            Utilities.startActivity(this, intent);
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        if (view == null) return;
        AppItemInfo info = (AppItemInfo) view.getTag();
        if (info != null) {
            TextToSpeechUtils.speak(info.title.toString(), this);
        }

    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }
}
