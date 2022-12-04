package com.sprd.classichome.mainmenu;

import android.content.ComponentName;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.sprd.classichome.AppItemInfo;
import com.sprd.classichome.HomeApplication;
import com.sprd.classichome.model.HomeMonitorCallbacks;
import com.sprd.classichome.model.LauncherModel;
import com.sprd.common.FeatureOption;
import com.sprd.common.util.LogUtils;
import com.sprd.common.util.UnreadCountStyleUtil;
import com.sprd.simple.launcher2.R;

import java.util.ArrayList;

/**
 * Created by SPREADTRUM on 17-9-22.
 */

public class MainMenuAdapter extends BaseAdapter {
    private static final String TAG = "MainMenuAdapter";
    Context mContext;
    GridView mGridView;
    private int mUnreadMsgCount = 0;
    private int mUnreadCallLogCount = 0;
    private static final ComponentName MSG_CPNAME = new ComponentName("com.android.mms",
            "com.android.mms.ui.ConversationList");
    private static final ComponentName CALL_LOG_CPNAME = new ComponentName("com.android.dialer",
            "com.android.dialer.calllog.CallLogActivity");

    @SuppressWarnings("FieldMayBeFinal")
    private LayoutInflater mInflater;
    private ArrayList<AppItemInfo> mMainMenuApps;

    @SuppressWarnings("FieldMayBeFinal")
    private HomeMonitorCallbacks mCallback = new HomeMonitorCallbacks() {
        @Override
        public void notifyAppsUpdated() {
            mMainMenuApps = LauncherModel.getMainMenuAppsList();
            notifyDataSetChanged();
        }
    };

    public MainMenuAdapter(GridView gridView) {
        mContext = gridView.getContext();
        mGridView = gridView;
        mMainMenuApps = LauncherModel.getMainMenuAppsList();
        mInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        HomeApplication.getInstance().setHomeCallback(mCallback);
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        ImageView icon;
        int width = mGridView.getColumnWidth();
        int height = mGridView.getMeasuredHeight() / mGridView.getNumColumns();

        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.main_menu_item, null);

        }

        AppItemInfo info = mMainMenuApps.get(position);
        icon = convertView.findViewById(R.id.app_item_icon);
        if ((icon != null) && !FeatureOption.SPRD_REMOVE_APP_ICON_TRANSPADDING_SUPPORT) {
            FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT,
                    FrameLayout.LayoutParams.MATCH_PARENT);
            icon.setLayoutParams(layoutParams);
        }
        //noinspection ConstantConditions
        icon.setImageDrawable(info.icon);
        convertView.setTag(info);
        convertView.setLayoutParams(new GridView.LayoutParams(width, height));

        TextView text = convertView.findViewById(R.id.unread_info);
        setUnreadTextView(text, info.getTargetComponent());
        return convertView;
    }

    public final int getCount() {
        return mMainMenuApps.size();
    }

    public final Object getItem(int position) {
        return mMainMenuApps.get(position);
    }

    public final long getItemId(int position) {
        return position;
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        HomeApplication.getInstance().removeHomeCallback(mCallback);
    }

    public void setMsgUnCount(int unCount) {
        mUnreadMsgCount = unCount;
    }

    public void setCallLogUnCount(int unCount) {
        mUnreadCallLogCount = unCount;
    }

    private void setUnreadTextView(TextView text, ComponentName componentName) {
        int count = 0;

        if (MSG_CPNAME.equals(componentName)) {
            count = mUnreadMsgCount;
        } else if (CALL_LOG_CPNAME.equals(componentName)) {
            count = mUnreadCallLogCount;
        }
        if (count > 0) {
            UnreadCountStyleUtil.setReadCountStyle(text, count);
        } else {
            text.setVisibility(View.INVISIBLE);
        }
        if (LogUtils.DEBUG)
            LogUtils.d(TAG, "count = " + count + "; componentName: " + componentName);
    }

}
