package com.sprd.classichome.mainmenu;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.TextView;

import com.sprd.classichome.AppItemInfo;
import com.sprd.classichome.BaseHomeActivity;
import com.sprd.classichome.util.UtilitiesExt;
import com.sprd.common.util.FeatureBarUtil;
import com.sprd.common.util.KeyCodeEventUtil;
import com.sprd.common.util.LogUtils;
import com.sprd.common.util.MissCallContentObserver;
import com.sprd.common.util.TextToSpeechUtils;
import com.sprd.common.util.UnreadInfoUtil;
import com.sprd.common.util.UnreadMessageContentObserver;
import com.sprd.common.util.Utilities;
import com.sprd.simple.launcher2.R;

public class MainMenuActivity extends BaseHomeActivity implements
        AdapterView.OnItemSelectedListener, AdapterView.OnItemClickListener,
        AdapterView.OnItemLongClickListener {
    private static final String TAG = "MainMenuAdapter";
    private static final int DEFAULT_SELECT_INDEX = 4;

    private TextView mTitle;
    private GridView mGridView;
    private UnreadMessageContentObserver mMmsSmsObserver;
    private MissCallContentObserver mCallObserver;
    private MainMenuAdapter mAdapter;
    private final static int UPDATE_ADAPTER_FOR_HANDLER = 1000;
    Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupViews();
        enableWallpaperShowing(getResources().getBoolean(R.bool.main_menu_with_wallpaper));
        mContext = this;
        new UnreadInfoThread().start();
        registerContentObservers();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterContentObservers();
    }

    private void setupViews() {
        setContentView(R.layout.main_menu_activity);
        mTitle = findViewById(R.id.title);
        mGridView = findViewById(R.id.all_apps_container);
        if (mGridView != null) {
            mAdapter = new MainMenuAdapter(mGridView);
            mGridView.setAdapter(mAdapter);
            mGridView.setOnItemSelectedListener(this);
            mGridView.setOnItemClickListener(this);
            mGridView.setOnItemLongClickListener(this);
            mGridView.post(() -> mGridView.setSelection(DEFAULT_SELECT_INDEX));
        }
        setSoftKey();
    }

    protected void setSoftKey() {
        setupFeatureBar(this);
        FeatureBarUtil.hideSoftKey(mFeatureBarHelper, FeatureBarUtil.SoftKey.LFK);

        FeatureBarUtil.setTextColor(this, mFeatureBarHelper, FeatureBarUtil.SoftKey.MDK, R.color.classichome_softbar_font_color);
        FeatureBarUtil.setTextColor(this, mFeatureBarHelper, FeatureBarUtil.SoftKey.RTK, R.color.classichome_softbar_font_color);
    }

    private void updateTitle(int position, @SuppressWarnings("SameParameterValue") boolean read) {
        if (mGridView != null) {
            AppItemInfo itemInfo = (AppItemInfo) mGridView.getItemAtPosition(position);
            if (itemInfo != null) {
                setTitle(itemInfo.title);
                if (read) {
                    TextToSpeechUtils.speak(itemInfo.title.toString(), this);
                }
            }
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        updateTitle(i, true);
    }

    @Override
    public void setTitle(CharSequence title) {
        if (mTitle != null) {
            mTitle.setText(title);
        }
        super.setTitle(title);
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        if (view != null && view.getTag() != null) {
            Intent intent = Utilities.constructLauncherIntent(this,
                    ((AppItemInfo) view.getTag()).pkgName,
                    ((AppItemInfo) view.getTag()).clsName);
            Utilities.startActivity(this, intent);
        }
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
        return KeyCodeEventUtil.longPressKeyEventForMainActivity(this, KeyEvent.KEYCODE_DPAD_CENTER);
    }

    @Override
    public void onBackPressed() {
        UtilitiesExt.goHome(this);
    }

    protected void registerContentObservers() {
        mMmsSmsObserver = new UnreadMessageContentObserver(mContext, mHandler);
        mContext.getContentResolver().registerContentObserver(UnreadInfoUtil.MMSSMS_CONTENT_URI,
                true, mMmsSmsObserver);
        mCallObserver = new MissCallContentObserver(mContext, mHandler);
        mContext.getContentResolver().registerContentObserver(UnreadInfoUtil.CALLS_CONTENT_URI, true, mCallObserver);

    }

    protected void unregisterContentObservers() {
        mContext.getContentResolver().unregisterContentObserver(mMmsSmsObserver);
        mContext.getContentResolver().unregisterContentObserver(mCallObserver);
    }

    @SuppressWarnings("deprecation")
    @SuppressLint("HandlerLeak")
    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            //noinspection RedundantSuppression
            switch (msg.what) {
                case UPDATE_ADAPTER_FOR_HANDLER:
                    //noinspection DuplicateBranchesInSwitch
                    break;
                case UnreadInfoUtil.MMSSMS_UNREAD_MESSAGE:
                    int messageCount = Integer.parseInt(String.valueOf(msg.obj));
                    mAdapter.setMsgUnCount(messageCount);
                    if (LogUtils.DEBUG) LogUtils.d(TAG, "Handler messageCount = " + messageCount);
                    break;
                case UnreadInfoUtil.MISS_CALL_MESSAGE:
                    int callCount = Integer.parseInt(String.valueOf(msg.obj));
                    mAdapter.setCallLogUnCount(callCount);
                    if (LogUtils.DEBUG) LogUtils.d(TAG, "Handler callCount = " + callCount);
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
            int messageCount = UnreadInfoUtil.getUnreadMessageCount(mContext);
            mAdapter.setMsgUnCount(messageCount);
            int callLogCount = UnreadInfoUtil.getMissedCallCount(mContext);
            mAdapter.setCallLogUnCount(callLogCount);
            if (LogUtils.DEBUG) LogUtils.d(TAG, "UnreadInfoThread messageCount = " + messageCount);
            mHandler.sendEmptyMessage(UPDATE_ADAPTER_FOR_HANDLER);
        }
    }
}
