package com.sprd.classichome;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.Activity;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.sprd.PlatformHelper;
import com.sprd.android.support.featurebar.FeatureBarHelper;
import com.sprd.common.util.FeatureBarUtil;
import com.sprd.common.util.FlashlightController;
import com.sprd.common.util.LogUtils;
import com.sprd.simple.launcher2.R;

public class FlashLightActivity extends Activity {
    private static final boolean DEBUG = LogUtils.DEBUG_ALL;
    private static final String TAG = "FlashLightActivity";
    private ImageView mImageSwitch;
    private View mContentView;
    private boolean mTurnOnScreen = false;
    private boolean mTurnOnLight = false;
    private FeatureBarHelper mFeatureBarHelper = null;
    private ActionBar mActionBar;
    private TextView mTitleTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (DEBUG) LogUtils.i(TAG, "onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.flashlight_layout);
        mImageSwitch = findViewById(R.id.flashlight_icon);
        mContentView = findViewById(R.id.content);
        mTurnOnLight = FlashlightController.turnOnFlashlight(false);
        FlashlightController.updateSettingsFlightState(this, mTurnOnLight);
        updateScreenLight();
        updateCenterLogo();
        updateFeatureBar();
        updateActionBar();
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        boolean returnValue = false;
        if (DEBUG) LogUtils.i(TAG, "keyCode = " + keyCode);
        if (event.isTracking() && !event.isCanceled()) {
            if (DEBUG) LogUtils.i(TAG, "onKeyUp  short press");
            switch (keyCode) {
                case KeyEvent.KEYCODE_MENU:
                    switchScreenLight();
                    returnValue = true;
                    break;
                case KeyEvent.KEYCODE_DPAD_CENTER:
                    switchFlashlight();
                    returnValue = true;
                    break;
                default:
                    returnValue = super.onKeyUp(keyCode, event);
                    break;
            }
        } else {
            if (DEBUG) LogUtils.i(TAG, "onKeyUp  long press");
        }
        return returnValue;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (event.getRepeatCount() == 0) {
            if (DEBUG) LogUtils.i(TAG, "getRepeatCount");
            event.startTracking();
            return true;
        }
        return false;
    }

    private void switchScreenLight() {
        if (DEBUG) LogUtils.i(TAG, "switchScreenLight mTurnOnScreen = " + mTurnOnScreen);
        mTurnOnScreen = !mTurnOnScreen;
        updateScreenLight();
        updateFeatureBar();
        updateActionBar();
    }

    private void switchFlashlight() {
        if (FlashlightController.switchFlashlight()) {
            mTurnOnLight = !mTurnOnLight;
            updateCenterLogo();
            updateFeatureBar();
            FlashlightController.updateSettingsFlightState(this, mTurnOnLight);
        }
    }

    private void updateScreenLight() {
        int bgColor = getResources().getColor(mTurnOnScreen ? R.color.flashlight_invert_bg_color : R.color.flashlight_default_bg_color);
        mContentView.setBackgroundColor(bgColor);
    }

    private void updateCenterLogo() {
        mImageSwitch.setImageResource(mTurnOnLight ? R.drawable.light_opened : R.drawable.light_closed);
    }

    private void updateFeatureBar() {
        if (PlatformHelper.isTargetBuild() && mFeatureBarHelper == null) {
            mFeatureBarHelper = new FeatureBarHelper(this);
            FeatureBarUtil.setText(this, mFeatureBarHelper, FeatureBarUtil.SoftKey.LFK, R.string.flash_invert);
        }

        int midTextId = mTurnOnLight ? R.string.flash_close : R.string.flash_open;
        FeatureBarUtil.setText(this, mFeatureBarHelper, FeatureBarUtil.SoftKey.MDK, midTextId);

        int textColorId = mTurnOnScreen ? R.color.flashlight_featurebar_invert_font_color : R.color.flashlight_featurebar_default_font_color;
        int bgSkColorId = mTurnOnScreen ? R.color.flashlight_featurebar_invert_bg_color : R.color.flashlight_featurebar_default_bg_color;
        FeatureBarUtil.setTextColor(this, mFeatureBarHelper, FeatureBarUtil.SoftKey.LFK, textColorId);
        FeatureBarUtil.setTextColor(this, mFeatureBarHelper, FeatureBarUtil.SoftKey.MDK, textColorId);
        FeatureBarUtil.setTextColor(this, mFeatureBarHelper, FeatureBarUtil.SoftKey.RTK, textColorId);
        FeatureBarUtil.setBackgroundColor(this, mFeatureBarHelper, bgSkColorId);
    }

    private void updateActionBar() {
        Drawable drawable;
        int titleColor;

        if (mActionBar == null) {
            mActionBar = getActionBar();
        }
        if (mTitleTextView == null) {
            @SuppressLint("DiscouragedApi")
            int titleId = Resources.getSystem().getIdentifier("action_bar_title", "id", "android");
            mTitleTextView = findViewById(titleId);
        }

        if (mTurnOnScreen) {
            drawable = getResources().getDrawable(R.drawable.light_actionbar_invert_bg);
            titleColor = getResources().getColor(R.color.flashlight_actionbar_invert_font_color);
        } else {
            drawable = getResources().getDrawable(R.drawable.light_actionbar_default_bg);
            titleColor = getResources().getColor(R.color.flashlight_actionbar_default_font_color);
        }
        if (mTitleTextView != null) {
            mTitleTextView.setTextColor(titleColor);
        }
        mActionBar.setBackgroundDrawable(drawable);
    }
}
