package com.sprd.simple.launcher;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.sprd.PlatformHelper;
import com.sprd.android.support.featurebar.FeatureBarHelper;
import com.sprd.common.util.AppIntentUtil;
import com.sprd.common.util.StatusBarUtils;
import com.sprd.common.util.TextToSpeechUtils;
import com.sprd.simple.adapter.BaseItemAdapter;
import com.sprd.simple.adapter.BaseListViewAdapter;
import com.sprd.simple.launcher.gridhome.R;
import com.sprd.simple.model.AppInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by SPRD on 2016/11/11.
 */

public abstract class BaseActivity<T extends AppInfo> extends Activity implements
        AdapterView.OnItemClickListener,AdapterView.OnItemSelectedListener{
    protected FeatureBarHelper mFeatureBarHelper;
    protected TextView mLeftSkView;
    protected TextView mCenterSkView;
    protected TextView mRightSkView;
    protected Context mContext;
    protected BaseListViewAdapter mAdapter = null;
    protected ListView mListView;
    protected List<AppInfo> mInfos;
    private static final String TAG = "BaseActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
        StatusBarUtils.setWindowStatusBarColor(this); // change status bar color
        mContext = this;
        mInfos = loadData();
        if (mInfos == null) {
            mInfos = new ArrayList<>();
        }
        initListView(new BaseItemAdapter(getApplicationContext(), mInfos));
        setSoftKey();
    }

    protected void initListView(BaseListViewAdapter adapter) {
        setContentView(R.layout.activity_base);
        mListView = (ListView) findViewById(R.id.base_list_item);
        mAdapter = adapter;
        mAdapter.setPosition(0);
        mListView.setAdapter(mAdapter);
        mListView.setOnItemClickListener(this);
        mListView.setOnItemSelectedListener(this);
    }

    protected void setSoftKey() {
        if (PlatformHelper.isTargetBuild()) {
            mFeatureBarHelper = new FeatureBarHelper(this);
            ViewGroup vp = mFeatureBarHelper.getFeatureBar();
            mLeftSkView = (TextView) mFeatureBarHelper.getOptionsKeyView();
            mCenterSkView = (TextView) mFeatureBarHelper.getCenterKeyView();
            mCenterSkView.setText(R.string.choose);
            mRightSkView = (TextView) mFeatureBarHelper.getBackKeyView();
            vp.removeView(mLeftSkView);
        }
    }

    /**
     * Each item click and enter different app
     *
     * @param parent
     * @param view
     * @param position
     * @param id
     */
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        launchApp(position);
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id){
        mAdapter.setPosition(position);
        mAdapter.notifyDataSetChanged();
        AppInfo info = getData(position);
        String appName = info != null ? info.getAppLabel() : "";
        if (!TextUtils.isEmpty(appName)) {
            TextToSpeechUtils.speak(appName, getApplicationContext());
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent){

    }

    public AppInfo getData(int position) {
        AppInfo data = mInfos != null && position > -1 && position < mInfos.size() ? mInfos.get(position) : null;
        return data;
    }

    abstract void launchApp(int position);

    abstract List<AppInfo> loadData();

    protected Intent intentForPosition(int position) {
        AppInfo info = mInfos != null && position > -1 && position < mInfos.size() ? mInfos.get(position) : null;
        Intent intent = new Intent();
        if (info != null) {
            intent.setClassName(info.getAppPkg(), info.getAppClassName());
        }
        AppIntentUtil.intentSetFlag(intent);
        return intent;
    }

}
