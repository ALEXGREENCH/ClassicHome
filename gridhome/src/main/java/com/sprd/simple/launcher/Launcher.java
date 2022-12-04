package com.sprd.simple.launcher;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.sprd.common.util.KeyCodeEventUtil;
import com.sprd.common.util.StatusBarUtils;
import com.sprd.common.util.TextToSpeechUtils;
import com.sprd.simple.adapter.BaseGridAdapter;
import com.sprd.simple.fragment.DefaultWorkspaceFragment;
import com.sprd.simple.fragment.FamilyWorkspaceFragment;
import com.sprd.simple.fragment.FourthWorkspaceFragment;
import com.sprd.simple.fragment.LauncherFragment;
import com.sprd.simple.fragment.ThirdWorkspaceFragment;
import com.sprd.simple.launcher.gridhome.R;
import com.sprd.simple.model.AppInfo;
import com.sprd.simple.model.LauncherGridView;
import com.sprd.simple.util.WeatherInfoUtil;
import com.sprd.simple.view.CircleDotIndicator;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;

/**
 * Created by SPDR on 2016/7/18.
 */
public class Launcher extends FragmentActivity implements ViewPager.OnPageChangeListener {
    private static final String TAG = "Launcher";
    public static final int sFIRST_WORKSPACE = 0;
    public static final int sDEFAULT_WORKSPACE = 1;
    public static final int sTHIRD_WORKSPACE = 2;
    public static final int sFOURTH_WORKSPACE = 3;

    private static final int PERMISSION_REQUEST = 4;
    private static final int PERMISSION_ALL_ALLOWED = 5;
    private static final int PERMISSION_ALL_DENIED = 6;
    private static final int MINI_SDK_RETURN_VALUE = -1;
    public static boolean permissionFlag = false;

    public static final String CURRENT_POSITION = "currentPosition";
    public static final String FOCUS_POSITION = "focusPosition";

    private static final int mHomePosition = sDEFAULT_WORKSPACE;
    private int mCurrPosition = mHomePosition;
    // add the control for animation
    public static boolean animAble = false;
    // whether display the yellow background
    public static boolean backGround = false;

    private CircleDotIndicator mCircleDotIndicator;

    private boolean mLongPressKey;

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the three primary sections of the app. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every loaded
     * fragment in memory. If this becomes too memory intensive, it may be best
     * to switch to a {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    AppSectionsPagerAdapter mAppSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will display the three primary sections of the
     * app, one at a time.
     */
    ViewPager mViewPager;
    private Context mContext = null;
    private static final String BROADCAST_WEATHER_UPDATE_ACTION = "com.sprd.weather.update";
    //private int mfocusPosition = 0;
    private View mRootView;
    boolean mIsBackToHome = false;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        initRootView();
        mContext = this;

        TextToSpeechUtils.getInstance(mContext);

        if (savedInstanceState != null) {
            int currPositionTemp = savedInstanceState.getInt(CURRENT_POSITION, -1);
            int focusPositionTemp = savedInstanceState.getInt(FOCUS_POSITION, -1);
            mCurrPosition = currPositionTemp > -1 ? currPositionTemp : mCurrPosition;
            //mfocusPosition = focusPositionTemp > -1 ? focusPositionTemp : mfocusPosition;
        }

        initViewPager();
        initCircleDotIndicator();
        IntentFilter filter = new IntentFilter(BROADCAST_WEATHER_UPDATE_ACTION);
        mContext.registerReceiver(mBroadcastReceiver, filter);
    }

    private void initRootView() {
        mRootView = View.inflate(this, R.layout.activity_main, null);
        mRootView.setPadding(0, StatusBarUtils.getStatusBarHeight(this), 0, 0);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        setContentView(mRootView);
    }

    private void initCircleDotIndicator() {
        mCircleDotIndicator = (CircleDotIndicator)findViewById(R.id.circle_dot_indicator);
        mCircleDotIndicator.setCircleDotCount(mAppSectionsPagerAdapter.getCount());
        mCircleDotIndicator.setChildEnable(mViewPager.getCurrentItem());
    }

    public void initViewPager() {
        mViewPager = (ViewPager) findViewById(R.id.pager);
        FragmentManager fm = getSupportFragmentManager();
        int mViewPagerId = mViewPager.getId();
        ArrayList<LauncherFragment> fragments = getFragments(mViewPagerId, fm);

        mAppSectionsPagerAdapter = new AppSectionsPagerAdapter(
                fm,fragments);
        mViewPager.setAdapter(mAppSectionsPagerAdapter);
        mViewPager.setCurrentItem(mHomePosition);
        mViewPager.setOnPageChangeListener(this);
    }

    public String getFragmentTag(int viewId, int position) {
        try {
            Class cls = Class.forName("android.support.v4.app.FragmentPagerAdapter");
            Method method = cls.getDeclaredMethod("makeFragmentName", new Class[]{int.class, long.class});
            method.setAccessible(true);
            return (String)method.invoke(cls, viewId, position);
        } catch (ClassNotFoundException e) {
            Log.e(TAG,"ClassNotFoundException: " + e.getMessage());
        } catch (NoSuchMethodException e) {
            Log.e(TAG,"NoSuchMethodException: " + e.getMessage());
        } catch (IllegalAccessException e) {
            Log.e(TAG,"IllegalAccessException: " + e.getMessage());
        } catch (InvocationTargetException e) {
            Log.e(TAG,"InvocationTargetException: " + e.getMessage());
        }
        return null;
    }

    public ArrayList<LauncherFragment> getFragments(int viewId, FragmentManager fm) {
        ArrayList<LauncherFragment> fragments = new ArrayList<LauncherFragment>();

        LauncherFragment familyWorkspaceFragment =
                (LauncherFragment) fm.findFragmentByTag(getFragmentTag(viewId,sFIRST_WORKSPACE));
        LauncherFragment defaultWorkspaceFragment =
                (LauncherFragment) fm.findFragmentByTag(getFragmentTag(viewId,sDEFAULT_WORKSPACE));
        LauncherFragment thirdWorkspaceFragment =
                (LauncherFragment) fm.findFragmentByTag(getFragmentTag(viewId,sTHIRD_WORKSPACE));
        LauncherFragment fourthWorkspaceFragment =
                (LauncherFragment) fm.findFragmentByTag(getFragmentTag(viewId,sFOURTH_WORKSPACE));
        if (familyWorkspaceFragment == null) {
            familyWorkspaceFragment = new FamilyWorkspaceFragment();
        }
        if (defaultWorkspaceFragment == null) {
            defaultWorkspaceFragment = new DefaultWorkspaceFragment();
        }
        if (thirdWorkspaceFragment == null) {
            thirdWorkspaceFragment = new ThirdWorkspaceFragment();
        }
        if (fourthWorkspaceFragment == null) {
            fourthWorkspaceFragment = new FourthWorkspaceFragment();
        }
        fragments.add(familyWorkspaceFragment);
        fragments.add(defaultWorkspaceFragment);
        fragments.add(thirdWorkspaceFragment);
        fragments.add(fourthWorkspaceFragment);

        return fragments;
    }
    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action == BROADCAST_WEATHER_UPDATE_ACTION) {
                ArrayList<String> weatherInfoArrayList = new ArrayList<String>();
                weatherInfoArrayList = intent.getStringArrayListExtra(WeatherInfoUtil.WEATHER_INFO_DATA);
                SharedPreferences sp = mContext.getSharedPreferences(WeatherInfoUtil.WEATHER_INFO_DATA, Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sp.edit();
                editor.putString(WeatherInfoUtil.WeatherInfo.LOCATION.ordinal()+"", weatherInfoArrayList.get(WeatherInfoUtil.WeatherInfo.LOCATION.ordinal()));
                editor.putString(WeatherInfoUtil.WeatherInfo.WEATHER.ordinal()+"", weatherInfoArrayList.get(WeatherInfoUtil.WeatherInfo.WEATHER.ordinal()));
                editor.putString(WeatherInfoUtil.WeatherInfo.TEMPERATURE.ordinal()+"", weatherInfoArrayList.get(WeatherInfoUtil.WeatherInfo.TEMPERATURE.ordinal()));
                editor.commit();
                editor.clear();

                if ((mAppSectionsPagerAdapter.getItem(sDEFAULT_WORKSPACE) != null) && (DefaultWorkspaceFragment.mIsWeatherAppExist)) {
                    ((DefaultWorkspaceFragment)mAppSectionsPagerAdapter.getItem(sDEFAULT_WORKSPACE)).refreshWeatherUI(mContext, weatherInfoArrayList);
                }
            }
        }
    };
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (Intent.ACTION_MAIN.equals(intent != null ? intent.getAction() : null)) {
            if (mViewPager != null) {
                mIsBackToHome = true;
                mViewPager.setCurrentItem(mHomePosition);
            }
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putInt(CURRENT_POSITION, mCurrPosition);
        //outState.putInt(FOCUS_POSITION, mfocusPosition);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestart() {
        super.onRestart();
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        //add Intent for SLT
        Intent intent = new Intent("CURRENT_APP_IS_EXIT");
        sendBroadcast(intent);
        Log.d(TAG, "send broadcast for slt");

        LauncherFragment launcherFragment = mAppSectionsPagerAdapter != null ?
                mAppSectionsPagerAdapter.getItem(mCurrPosition) : null;
        if (launcherFragment != null) {
            launcherFragment.setActived(true);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            unregisterReceiver(mBroadcastReceiver);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if(!KeyCodeEventUtil.isLauncherNeedUseKeycode(keyCode)){
            return super.onKeyUp(keyCode, event);
        }

        boolean result = false;
        if (event.isTracking() && !event.isCanceled()) {
            if (!mLongPressKey) {
                Log.i(TAG, "onKeyUp  short press");
                switch (keyCode) {
                    case KeyEvent.KEYCODE_MENU:
                        pageLeft();
                        result = true;
                        break;
                    case KeyEvent.KEYCODE_BACK:
                        pageRight();
                        result = true;
                        break;
                    default:
                        result = KeyCodeEventUtil.pressKeyEventForMainActivity(mContext, keyCode, event);
                        break;
                }
            }
        }

        if (!result) {
            result = super.onKeyUp(keyCode, event);
        }
        return result;
    }

    private void pageLeft() {
        if (mViewPager != null && mCurrPosition > 0) {
            mViewPager.setCurrentItem(mCurrPosition - 1, true);
        }
    }
    private void pageRight() {
        if (mViewPager != null && mCurrPosition < (mViewPager.getAdapter().getCount() - 1)) {
            mViewPager.setCurrentItem(mCurrPosition + 1, true);
        }
    }
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(!KeyCodeEventUtil.isLauncherNeedUseKeycode(keyCode)){
            return super.onKeyDown(keyCode, event);
        }
        //clock view need not request focus
        /*LauncherFragment currentFragment = mAppSectionsPagerAdapter.getItem(mCurrPosition);
        if (currentFragment instanceof DefaultWorkspaceFragment) {
            DefaultWorkspaceFragment defaultWorkspaceFragment = (DefaultWorkspaceFragment) currentFragment;
            LinearLayout clockView = defaultWorkspaceFragment.mClockView;
            LauncherGridView gridView = defaultWorkspaceFragment.getGridView();
            if (clockView != null && gridView != null) {
                switch (keyCode) {
                    case KeyEvent.KEYCODE_DPAD_UP:
                        if (gridView.isFocused()) {
                            gridView.clearFocus();
                            gridView.setFocusable(false);
                            gridView.setSelection(LauncherGridView.INVALID_POSITION);
                            clockView.requestFocus();
                            clockView.setFocusable(true);
                            clockView.setSelected(true);
                            clockView.setNextFocusDownId(R.id.default_grid_view);
                            mfocusPosition = 0;
                            return true;
                        } else if (clockView.isSelected() || clockView.isFocused()) {
                            return false;
                        }
                    case KeyEvent.KEYCODE_DPAD_DOWN:
                        if (clockView.isSelected()) {
                            gridView.setFocusable(true);
                            gridView.requestFocus();
                            gridView.setSelection(defaultWorkspaceFragment.getAdapter().getSelectPosition());
                            gridView.setNextFocusUpId(R.id.clock_view);
                            clockView.setSelected(false);
                            clockView.setFocusable(false);
                            clockView.clearFocus();
                            mfocusPosition = 1;
                            defaultWorkspaceFragment.setActived(true);
                            String appName = getAppName(defaultWorkspaceFragment);
                            LauncherGridView launcherGridView = defaultWorkspaceFragment.getGridView();
                            if (!TextUtils.isEmpty(appName) && (launcherGridView != null && launcherGridView.isFocused())) {
                                TextToSpeechUtils.read(appName, getApplicationContext());
                            }
                            return true;
                        } else {
                            return false;
                        }
                    default:
                        break;
                }
            }
        }*/

        int repeatCount = event.getRepeatCount();
        if (repeatCount == 0) {
            event.startTracking();
            mLongPressKey = false;
        } else if (repeatCount > 0) {
            mLongPressKey = true;
        }
        return true;
    }

    private String getAppName(LauncherFragment launcherFragment) {
        String appName = "";
        if (launcherFragment != null && !(launcherFragment instanceof FamilyWorkspaceFragment)) {
            BaseGridAdapter adapter = launcherFragment.getAdapter();
            int selectedPosition = adapter != null ? adapter.getSelectPosition() : -1;
            AppInfo appInfo = (AppInfo) launcherFragment.getData(selectedPosition);
            appName = appInfo != null ? appInfo.getAppLabel() : "";
        }
        return appName;
    }

    @Override
    public boolean onKeyLongPress(int keyCode, KeyEvent event) {
        if (KeyCodeEventUtil.longPressKeyEventForMainActivity(this, keyCode)) {
            return true;
        } else {
            return super.onKeyLongPress(keyCode, event);
        }
    }

    /*public int getFocusPosition() {
        return mfocusPosition;
    }*/

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {
        int oldPosition = mCurrPosition;

        mCircleDotIndicator.setChildEnable(position);
        mCurrPosition = position;

        activatedFragment();
        readItem(oldPosition);
    }

    private void activatedFragment() {
        for (int i = 0; i < mAppSectionsPagerAdapter.getCount(); i++) {
            LauncherFragment lf = mAppSectionsPagerAdapter.getItem(i);
            if (lf != null) {
                if (i == mCurrPosition) {
                    Log.d(TAG, "onPageSelected [ " + i + "] setActived(true)");
                    lf.setActived(true);
                } else {
                    lf.setActived(false);
                }
            }
        }
    }

    private void readItem(int oldPosition) {
        LauncherFragment lf = mAppSectionsPagerAdapter.getItem(mCurrPosition);
        if (lf != null) {
            LauncherGridView gridView = lf.getGridView();
            String appName = getAppName(lf);
            if (!TextUtils.isEmpty(appName) && gridView != null && gridView.isFocused()) {
                if (isAllowRead(oldPosition, mCurrPosition)) {
                    TextToSpeechUtils.speak(appName, getApplicationContext());
                }
            }
        }
    }

    private boolean isAllowRead(int oldPosition, int position) {
        boolean allowRead = false;
        if (position != mHomePosition){
            allowRead = true;
        }else{
            if (isAdjacentPage(oldPosition, mHomePosition) && !mIsBackToHome){
               allowRead = true;
            } else {
                if (mIsBackToHome){
                    mIsBackToHome = false;
                }
                allowRead = false;
            }
        }
        return allowRead;
    }

    private boolean isAdjacentPage(int oldPosition, int position){
        if (Math.abs(oldPosition - position) == 1) {
            return true;
        }
        return false;
    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the primary sections of the app.
     */
    public class AppSectionsPagerAdapter extends FragmentPagerAdapter {
        private ArrayList<LauncherFragment> fragmentLists;

        public AppSectionsPagerAdapter(FragmentManager fm,ArrayList<LauncherFragment> fragments) {
            super(fm);
            this.fragmentLists = fragments;
        }

        @Override
        public LauncherFragment getItem(int i) {
            if (fragmentLists != null && i > -1 &&
                    i < fragmentLists.size()) {
                return fragmentLists.get(i);
            }
            return null;
        }

        @Override
        public int getCount() {
            if (fragmentLists != null) {
                return fragmentLists.size();
            }
            return 0;
        }
    }
}
