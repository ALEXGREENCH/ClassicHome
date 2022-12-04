package com.sprd.simple.fragment;

import android.app.ActivityManager;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.Toast;

import com.sprd.common.util.TextToSpeechUtils;
import com.sprd.simple.adapter.FourthPageAdapter;
import com.sprd.simple.launcher.gridhome.R;
import com.sprd.simple.model.AppInfo;
import com.sprd.simple.util.MemoryInfoUtil;
import com.sprd.simple.util.PackageInfoUtil;

import java.util.List;


/**
 * Created by SPRD on 2016/7/19.
 */
public class FourthWorkspaceFragment extends LauncherFragment<AppInfo> {
    private static final String TAG = "FourthWorkspaceFragment";
    private static final int ICON_SPEED = 3;
    private static final int START_ANIMATION = 6;
    private static final int STOP_ANIMATION = 7;
    private static final int CLEAN_BACKGROUND_PROCESS = 8;
    private static final int UPDATE_SPEED_VIEW = 9;

    private static final int DELAY_CLEAN_BACKGROUND_PROCESS_TIME = 200;
    private static final int DELAY_UPDATE_SPEED_VIEW_TIME = 200;
    private static final int LONG_CLICK_DELAY_TIME = 500;
    private long mSpeedLastClickTime = 0;

    private ActivityManager mActivityManager = null;

    private Animation cleanAnimation;

    @Override
    protected List<AppInfo> loadData() {
        List<AppInfo> datas = PackageInfoUtil.loadAppInfos(getActivity(), R.array.fourth_page_app_names, true);
        //noinspection ConstantConditions
        if (datas != null && datas.size() > ICON_SPEED) {
            AppInfo appInfo = datas.get(ICON_SPEED);
            if (appInfo != null) {
                 appInfo.setAppLabel(getString(R.string.speed_name));
            }
        }
        return datas;
    }

    public FourthWorkspaceFragment() {
        super();
    }

    @Override
    protected void registerContentObservers() {
        // TODO: 16-11-28
    }

    @Override
    protected void unregisterContentObservers() {
        // TODO: 16-11-30
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = super.onCreateView(inflater, container, savedInstanceState);

        mActivityManager = (ActivityManager) getActivity().getSystemService(Context.ACTIVITY_SERVICE);

        cleanAnimation = AnimationUtils.loadAnimation(getActivity(), R.anim.clean);
        LinearInterpolator lin = new LinearInterpolator();
        cleanAnimation.setInterpolator(lin);

        // init GridView
        mAdapter = new FourthPageAdapter(mDatas, getActivity(), mGridView);
        mGridView.setAdapter(mAdapter);

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();

        handler.removeMessages(UPDATE_SPEED_VIEW);
        handler.sendEmptyMessageDelayed(UPDATE_SPEED_VIEW, DELAY_UPDATE_SPEED_VIEW_TIME);
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
            if (ICON_SPEED == position) {
                mSpeedLastClickTime = SystemClock.uptimeMillis();
                handler.removeMessages(CLEAN_BACKGROUND_PROCESS);
                handler.sendEmptyMessageDelayed(CLEAN_BACKGROUND_PROCESS, DELAY_CLEAN_BACKGROUND_PROCESS_TIME);
            } else {
                startActivity(intentForPosition(position));
            }
        } catch (ActivityNotFoundException e) {
            Log.i(TAG, "App not found");
            Toast.makeText(getActivity(), R.string.activity_not_found, Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        if (ICON_SPEED != position
                || (SystemClock.uptimeMillis() - mSpeedLastClickTime) >= LONG_CLICK_DELAY_TIME) {
            return super.onItemLongClick(parent, view, position, id);
        }
        return false;
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        mAdapter.setSelectPosition(position);
        String appName = getString(R.string.speed_name);
        if (ICON_SPEED != position) {
            AppInfo info = getData(position);
            appName = info != null ? info.getAppLabel() : "";
        }
        if (isActived() && !TextUtils.isEmpty(appName)) {
            TextToSpeechUtils.speak(appName, getActivity().getApplicationContext());
        }
    }

    class SecurityThread extends Thread {
        @Override
        public void run() {
            handler.sendEmptyMessage(START_ANIMATION);
            Log.i(TAG, "SecurityThread");
            List<ActivityManager.RunningAppProcessInfo> appProcessesList =
                    mActivityManager.getRunningAppProcesses();

            Context context = mContext.getApplicationContext();
            if (context == null) {
                Log.d(TAG, "get context is null");
                return;
            }
            long beforeMem = MemoryInfoUtil.getAvailMemory(context);
            Log.d(TAG, "-----------before memory info : " + beforeMem);

            int count = 0;
            if (appProcessesList != null) {
                for (int i = 0; i < appProcessesList.size(); ++i) {
                    ActivityManager.RunningAppProcessInfo appProcessInfo = appProcessesList.get(i);
                    Log.d(TAG, "process name : " + appProcessInfo.processName);
                    Log.d(TAG, "importance : " + appProcessInfo.importance);

                    if (appProcessInfo.importance > ActivityManager.RunningAppProcessInfo.IMPORTANCE_VISIBLE) {
                        String[] pkgList = appProcessInfo.pkgList;
                        for (String aPkgList : pkgList) {
                            Log.d( TAG, "It will be killed, package name : " + aPkgList );
                            try {
                                mActivityManager.killBackgroundProcesses(aPkgList);
                                count++;
                            } catch (SecurityException e) {
                                Log.w( TAG, "kill failed", e );
                            }
                        }
                    }
                }
            }
            long afterMem = MemoryInfoUtil.getAvailMemory(context);
            Log.d(TAG, "----------- after memory info : " + afterMem);
            Log.i(TAG, "----------- count = " + count);
            Message msg = new Message();
            msg.what = STOP_ANIMATION;
            msg.arg1 = count;
            msg.arg2 = (int) afterMem;
            handler.sendMessage(msg);
        }
    }

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (mGridView != null) {
                View view = mGridView.getChildAt(3);
                if(view == null){
                    Log.d(TAG, "ICON_SPEED view is null");
                    return;
                }
                ImageView img = (ImageView) view
                        .findViewById(R.id.app_item_icon);
                switch (msg.what) {
                    case START_ANIMATION: {
                        // clear iconAnimation when click speed
                        if (cleanAnimation != null) {
                            view.clearAnimation();
                            img.setImageDrawable(getActivity().getResources()
                                    .getDrawable(
                                            R.drawable.app_speed));
                            img.startAnimation(cleanAnimation);
                        }
                        break;
                    }

                    case STOP_ANIMATION: {
                        int processCount = msg.arg1;
                        int availableMemory = msg.arg2;
                        img.clearAnimation();
                        if(mAdapter instanceof FourthPageAdapter) {
                            ((FourthPageAdapter) mAdapter).updateSpeedView();
                        }
                        Toast.makeText(getActivity(),
                                getActivity().getResources().getString(
                                        R.string.clean_toast_prompt,
                                        processCount, availableMemory),
                                Toast.LENGTH_LONG).show();
                        break;
                    }

                    case UPDATE_SPEED_VIEW: {
                        if(mAdapter instanceof FourthPageAdapter) {
                            ((FourthPageAdapter) mAdapter).updateSpeedView();
                        }
                        break;
                    }

                    case CLEAN_BACKGROUND_PROCESS: {
                        SecurityThread thread = new SecurityThread();
                        thread.start();
                        break;
                    }
                }
            }
        }
    };
}
