package com.sprd.simple.fragment;

import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.text.format.Time;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.sprd.common.FeatureOption;
import com.sprd.common.util.KeyCodeEventUtil;
import com.sprd.common.util.LunarCalendar;
import com.sprd.common.util.LunarCalendarConvertUtil;
import com.sprd.common.util.TextToSpeechUtils;
import com.sprd.simple.adapter.DefaultPageAdapter;
import com.sprd.simple.launcher.gridhome.R;
import com.sprd.simple.model.AppInfo;
import com.sprd.simple.util.PackageInfoUtil;
import com.sprd.common.util.UnreadInfoUtil;
import com.sprd.common.util.UnreadMessageContentObserver;
import com.sprd.simple.util.WeatherInfoUtil;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by SPRD on 2016/7/19.
 */
public class DefaultWorkspaceFragment extends LauncherFragment<AppInfo>
        implements AdapterView.OnClickListener, View.OnLongClickListener {

    private static final String TAG = "DefaultWorkspace";
    private final static int UPDATE_ADAPTER_FOR_HANDLER = 1000;
    private View mRootView = null;
    public LinearLayout mClockView = null;
    private LinearLayout mWeatherView;
    private UnreadMessageContentObserver mMmsSmsObserver;

    public static final boolean mIsWeatherAppExist = false;
    private final boolean mWeatherWidgetVoiceEnable = true;
    private ImageView mImageView;
    private TextView mTextView;
    private String mWeatherLocation;
    private String mWeatherType;
    private String mCurrentWeather;
    private String mTemperature;

    private TextView mLunarDateView;
    private boolean mIsRegisted = false;
    private BroadcastReceiver mTimeChangReceiver;

    @Override
    protected List<AppInfo> loadData() {
        return PackageInfoUtil.loadAppInfos(getActivity(), R.array.default_page_app_names, true);
    }

    public DefaultWorkspaceFragment() {
        super();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mRootView = super.onCreateView(inflater, container, savedInstanceState);

        mClockView = mRootView.findViewById(R.id.default_clock_view);
        mClockView.setVisibility(View.VISIBLE);

        initLunarDateView();

        if (mIsWeatherAppExist) {
            mWeatherView = mRootView.findViewById(R.id.ll_weather);
            mWeatherView.setVisibility(View.VISIBLE);
            mImageView = mWeatherView.findViewById(R.id.weather_view);
            mTextView = mWeatherView.findViewById(R.id.temp_view);
            SharedPreferences sp = getActivity().getSharedPreferences(WeatherInfoUtil.WEATHER_INFO_DATA, Context.MODE_PRIVATE);
            if (sp != null) {
                ArrayList<String> weatherInfoArrayList = new ArrayList<String>();
                weatherInfoArrayList.add(sp.getString(WeatherInfoUtil.WeatherInfo.LOCATION.ordinal()+"", ""));
                weatherInfoArrayList.add(sp.getString(WeatherInfoUtil.WeatherInfo.WEATHER.ordinal()+"", ""));
                weatherInfoArrayList.add(sp.getString(WeatherInfoUtil.WeatherInfo.TEMPERATURE.ordinal()+"", ""));
                refreshWeatherUI(mContext, weatherInfoArrayList);
            }
        }

        //clock view need not request focus
        /*mClockView.requestFocus();
        mClockView.setSelected(true);
        mClockView.setOnClickListener(this);
        mClockView.setOnLongClickListener(this);*/

        // init GridView
        mAdapter = new DefaultPageAdapter(mDatas, mContext, mGridView);
        mGridView.setAdapter(mAdapter);

        new UnreadInfoThread().start();

        //clock view need not request focus
        //updateFocusPostion();

        return mRootView;
    }

    public void updateFocusPostion() {
        if (mContext == null) {
            return;
        }
        //clock view need not request focus
        /*int focusPosition = ((Launcher)mContext).getFocusPosition();
        if (focusPosition == 0) {
            mGridView.setSelected(false);
            mGridView.clearFocus();
            mGridView.setFocusable(false);
            mGridView.setSelection(LauncherGridView.INVALID_POSITION);
            mClockView.setFocusable(true);
            mClockView.requestFocus();
            mClockView.setSelected(true);
        } else if (focusPosition == 1) {
            mClockView.setSelected(false);
            mClockView.clearFocus();
            mClockView.setFocusable(false);
            mGridView.setFocusable(true);
            mGridView.requestFocus();
        }*/
    }


    public void refreshWeatherUI(Context context, ArrayList<String> weatherInfoArrayList) {
        if (weatherInfoArrayList != null) {
            mWeatherLocation = weatherInfoArrayList.get(WeatherInfoUtil.WeatherInfo.LOCATION.ordinal());
            mWeatherType = weatherInfoArrayList.get(WeatherInfoUtil.WeatherInfo.WEATHER.ordinal());
            mTemperature = weatherInfoArrayList.get(WeatherInfoUtil.WeatherInfo.TEMPERATURE.ordinal());
            Log.d(TAG, " loc = " + mWeatherLocation + " weatherType = " + mWeatherType + " temp = " + mTemperature);
            if ((mWeatherType != null) && (!mWeatherType.isEmpty())) {
                switch(Integer.parseInt(mWeatherType)) {
                    case WeatherInfoUtil.WEATHER0:
                        mImageView.setImageDrawable(context.getResources().getDrawable(R.drawable.weather_1));
                        mCurrentWeather = mContext.getResources().getString(R.string.weather_0);
                        break;
                    case WeatherInfoUtil.WEATHER1:
                        mImageView.setImageDrawable(context.getResources().getDrawable(R.drawable.weather_1));
                        mCurrentWeather = mContext.getResources().getString(R.string.weather_1);
                        break;
                    default:
                        break;
                }
                mTextView.setText(mTemperature);
            } else {
                mImageView.setImageDrawable(context.getResources().getDrawable(R.drawable.weather_1));
                mCurrentWeather = mContext.getResources().getString(R.string.weather_0);
            }
        }
    }
    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(FeatureOption.LUNAR_SUPPORT && mIsRegisted){
            mContext.unregisterReceiver(mTimeChangReceiver);
        }
    }

    private void initLunarDateView(){
        if(FeatureOption.LUNAR_SUPPORT){
            if(LunarCalendarConvertUtil.isLunarSetting()) {
                mLunarDateView = mClockView.findViewById(R.id.format_lunar_date_view);
                mLunarDateView.setVisibility(View.VISIBLE);
                LunarCalendar.reloadLanguageResources(mContext);

                mTimeChangReceiver = new TimeChangeReceiver();
                IntentFilter filter = new IntentFilter();
                filter.addAction(Intent.ACTION_TIME_TICK);
                filter.addAction(Intent.ACTION_TIME_CHANGED);
                filter.addAction(Intent.ACTION_DATE_CHANGED);
                filter.addAction(Intent.ACTION_TIMEZONE_CHANGED);
                mContext.registerReceiver(mTimeChangReceiver, filter);
                mIsRegisted = true;
                updateLunarDateView();
            }else{
                LunarCalendar.clearLanguageResourcesRefs();
            }
        }
    }

    class TimeChangeReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            updateLunarDateView();
        }
    }

    private void updateLunarDateView(){
        Time time = new Time();
        time.set(System.currentTimeMillis());
        String lunarStr = LunarCalendarConvertUtil.buildLunarMonthDay(time, mContext)
                + "  " + LunarCalendarConvertUtil.bulidLunarYear(time, mContext);
        if(mLunarDateView != null) {
            mLunarDateView.setText(lunarStr);
        }
    }

    @Override
    protected void registerContentObservers() {
        mMmsSmsObserver = new UnreadMessageContentObserver(mContext, mHandler);
        mContext.getContentResolver().registerContentObserver(UnreadInfoUtil.MMSSMS_CONTENT_URI,
                true, mMmsSmsObserver);
    }

    @Override
    protected void unregisterContentObservers() {
        mContext.getContentResolver().unregisterContentObserver(mMmsSmsObserver);
    }

    @Override
    public void onClick(View v) {
        if ((v == mClockView) && mWeatherWidgetVoiceEnable) {
            SimpleDateFormat df = new SimpleDateFormat(mContext.getResources().getString(R.string.speak_spec));
            Date date = new Date();
            String currentTime = df.format(date);
            if (mIsWeatherAppExist) {
                TextToSpeechUtils.speak(currentTime + " " + mCurrentWeather + " " + mTemperature, getActivity().getApplicationContext());
            } else {
                TextToSpeechUtils.speak(currentTime, getActivity().getApplicationContext());
            }
        }
    }

    @Override
    public boolean onLongClick(View v) {
        if (v == mClockView) {
            return KeyCodeEventUtil.longPressKeyEventForMainActivity(mContext, KeyEvent.KEYCODE_DPAD_CENTER);
        }
        return false;
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
            Toast.makeText(mContext, R.string.activity_not_found, Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        mAdapter.setSelectPosition(position);
        AppInfo appInfo = getData(position);
        String appName = appInfo != null ? appInfo.getAppLabel() : "";
        if (isActived() && mGridView.isFocused() && !TextUtils.isEmpty(appName)) {
            TextToSpeechUtils.speak(appName, getActivity().getApplicationContext());
        }
    }

    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case UPDATE_ADAPTER_FOR_HANDLER:
                    break;
                case UnreadInfoUtil.MMSSMS_UNREAD_MESSAGE:
                    int messageCount = Integer.parseInt(String.valueOf(msg.obj));
                    mAdapter.setUnCount(messageCount);
                    Log.i(TAG, "Handler messageCount = " + messageCount);
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
            mAdapter.setUnCount(messageCount);
            Log.i(TAG, "UnreadInfoThread messageCount = " + messageCount);
            mHandler.sendEmptyMessage(UPDATE_ADAPTER_FOR_HANDLER);
        }
    }

}
