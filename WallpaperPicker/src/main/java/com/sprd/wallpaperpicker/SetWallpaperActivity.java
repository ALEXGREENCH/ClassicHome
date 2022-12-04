package com.sprd.wallpaperpicker;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;

import com.sprd.PlatformHelper;
import com.sprd.android.support.featurebar.FeatureBarHelper;
import com.sprd.common.util.FeatureBarUtil;
import com.sprd.common.util.LogUtils;
import com.sprd.common.view.LoopGridView;

public class SetWallpaperActivity extends Activity implements AdapterView.OnItemClickListener {
    private static final boolean DEBUG = LogUtils.DEBUG_ALL;
    private static final String TAG = "SetWallpaperActivity";
    private static final int REQUEST_CODE = 1;
    private static final int RESULT_CODE_FINISH = -1;
    private LoopGridView mGridView = null;
    private FeatureBarHelper mFeatureBarHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (DEBUG) LogUtils.i(TAG, "onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.wallpaper_layout);
        setAdapter();
        setSoftKey();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (DEBUG) LogUtils.i(TAG, "onActivityResult " + requestCode + ' ' + resultCode);
        if (requestCode != REQUEST_CODE) {
            return;
        }
        if (resultCode == RESULT_CODE_FINISH) {
            finish();
        } else if (resultCode >= 0) {
            mGridView.setSelection(resultCode);
        }
    }

    private void setSoftKey() {
        if (PlatformHelper.isTargetBuild() && mFeatureBarHelper == null) {
            mFeatureBarHelper = new FeatureBarHelper(this);
        }

        FeatureBarUtil.hideSoftKey(mFeatureBarHelper, FeatureBarUtil.SoftKey.LFK);
    }

    private void handleCenterKey(int id) {
        if (DEBUG) LogUtils.i(TAG, "handleCenterKey id = " +id);
        Intent intent = new Intent(this, PreviewWallpaperActivity.class);
        Bundle data = new Bundle();
        data.putInt("Position", id);
        intent.putExtras(data);
        startActivityForResult(intent, REQUEST_CODE);
    }

    private void setAdapter() {
        if (DEBUG) LogUtils.i(TAG, "setAdapter");
        mGridView = findViewById(R.id.grid_view);
        SetWallpaperAdapter adapter = new SetWallpaperAdapter(this);
        mGridView.setAdapter(adapter);
        mGridView.setOnItemClickListener(this);
    }

    @Override
    public void onItemClick(AdapterView<?> container, View view, int position, long id) {
        if (DEBUG) LogUtils.i(TAG, "onItemClick mPosition = " +position);
        handleCenterKey(position);
    }
}
