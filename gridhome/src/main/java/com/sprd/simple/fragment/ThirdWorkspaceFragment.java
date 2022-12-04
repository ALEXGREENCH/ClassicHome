package com.sprd.simple.fragment;

import android.content.ActivityNotFoundException;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Toast;

import com.sprd.common.util.TextToSpeechUtils;
import com.sprd.simple.adapter.ThirdPageAdapter;
import com.sprd.simple.launcher.gridhome.R;
import com.sprd.simple.model.AppInfo;
import com.sprd.common.util.MissCallContentObserver;
import com.sprd.simple.util.PackageInfoUtil;
import com.sprd.common.util.UnreadInfoUtil;

import java.util.List;


/**
 * Created by SPRD on 2016/7/19.
 */
public class ThirdWorkspaceFragment extends LauncherFragment<AppInfo> {
    private static final String TAG = "ThirdWorkspaceFragment";
    private final static int UPDATE_ADAPTER_FOR_HANDLER = 1000;
    private MissCallContentObserver mCallObserver;

    @Override
    protected List<AppInfo> loadData() {
        return PackageInfoUtil.loadAppInfos(getActivity(), R.array.third_page_app_names, true);
    }

    public ThirdWorkspaceFragment() {
        super();
    }

    @Override
    protected void registerContentObservers() {
        mCallObserver = new MissCallContentObserver(mContext, mHandler);
        mContext.getContentResolver().registerContentObserver(UnreadInfoUtil.CALLS_CONTENT_URI, true, mCallObserver);
    }

    @Override
    protected void unregisterContentObservers() {
        mContext.getContentResolver().unregisterContentObserver(mCallObserver);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = super.onCreateView(inflater, container, savedInstanceState);

        // init GridView
        mAdapter = new ThirdPageAdapter(mDatas, getActivity(), mGridView);
        mGridView.setAdapter(mAdapter);

        new UnreadInfoThread().start();

        return rootView;

    }

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case UPDATE_ADAPTER_FOR_HANDLER:
                    break;
                case UnreadInfoUtil.MISS_CALL_MESSAGE:
                    int callCount = Integer.parseInt(String.valueOf(msg.obj));
                    mAdapter.setUnCount(callCount);
                    Log.i(TAG, "Handler callCount = " + callCount);
                    break;
                default:
                    break;
            }
            mAdapter.notifyDataSetChanged();
        }
    };

    class UnreadInfoThread extends Thread {
        @Override
        public void run() {
            int callCount = UnreadInfoUtil.getMissedCallCount(mContext);
            mAdapter.setUnCount(callCount);
            Log.i(TAG, "UnreadInfoThread callCount = " + callCount);
            mHandler.sendEmptyMessage(UPDATE_ADAPTER_FOR_HANDLER);
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
        try {
            startActivity(intentForPosition(position));
        } catch (ActivityNotFoundException e) {
            Log.i(TAG, "App not found");
            Toast.makeText(getActivity(), R.string.activity_not_found, Toast.LENGTH_LONG).show();
        }
    }


    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        mAdapter.setSelectPosition(position);
        AppInfo appInfo = getData(position);
        String appName = appInfo != null ? appInfo.getAppLabel() : "";
        if (isActived() && !TextUtils.isEmpty(appName)) {
            TextToSpeechUtils.speak(appName, getActivity().getApplicationContext());
        }
    }
}
