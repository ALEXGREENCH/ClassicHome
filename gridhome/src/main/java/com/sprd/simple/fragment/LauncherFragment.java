package com.sprd.simple.fragment;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;

import com.sprd.common.util.AppIntentUtil;
import com.sprd.common.util.KeyCodeEventUtil;
import com.sprd.simple.adapter.BaseGridAdapter;
import com.sprd.simple.launcher.gridhome.R;
import com.sprd.simple.model.AppInfo;
import com.sprd.simple.model.LauncherGridView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by SPRD on 2016/11/8.
 */
public abstract class LauncherFragment<T extends AppInfo> extends Fragment
        implements AdapterView.OnItemLongClickListener,
        AdapterView.OnItemClickListener, AdapterView.OnItemSelectedListener {
    private static final String TAG = "LauncherFragment";
    private boolean mIsActived;
    protected LauncherGridView mGridView;
    protected Context mContext;
    protected BaseGridAdapter mAdapter;
    protected List<T> mDatas = new ArrayList<T>();

    public List<T> getDatas() {
        return mDatas;
    }

    public void setDatas(List<T> datas) {
        this.mDatas = datas;
    }

    public BaseGridAdapter getAdapter() {
        return mAdapter;
    }

    public void setAdapter(BaseGridAdapter adapter) {
        this.mAdapter = adapter;
    }

    public LauncherGridView getGridView() {
        return mGridView;
    }

    public void setGridView(LauncherGridView gridView) {
        this.mGridView = gridView;
    }

    public LauncherFragment() {
        mIsActived = false;
    }

    public boolean isActived() {
        return mIsActived;
    }

    public void setActived(boolean actived) {
        mIsActived = actived;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = getActivity();
        mDatas = loadData();
        if (mDatas == null) {
            mDatas = new ArrayList<T>();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_pager, container, false);
        initGridView(root);
        registerContentObservers();
        return root;
    }

    protected void initGridView(View root) {
        mGridView = (LauncherGridView) root.findViewById(R.id.default_grid_view);
        mGridView.setSelector(new ColorDrawable(Color.TRANSPARENT));
        // SOS Long click Listener
        mGridView.setOnItemLongClickListener(this);
        // View click Listener
        mGridView.setOnItemClickListener(this);
        // view has focus Listener
        mGridView.setOnItemSelectedListener(this);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unregisterContentObservers();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mDatas != null) {
            mDatas.clear();
            mDatas = null;
        }
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        return KeyCodeEventUtil.longPressKeyEventForMainActivity(mContext, KeyEvent.KEYCODE_DPAD_CENTER);
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
    }

    protected Intent intentForPosition(int position) {
        T info = getData(position);
        Intent intent = new Intent();
        if (info != null) {
            intent.setClassName(info.getAppPkg(), info.getAppClassName());
        }
        AppIntentUtil.intentSetFlag(intent);
        return intent;
    }

    public T getData(int position) {
        T data = mDatas != null && position > -1 && position < mDatas.size() ? mDatas.get(position) : null;
        return data;
    }

    protected abstract List<T> loadData();

    protected abstract void registerContentObservers();

    protected abstract void unregisterContentObservers();
}
