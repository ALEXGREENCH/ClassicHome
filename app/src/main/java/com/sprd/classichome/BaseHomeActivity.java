package com.sprd.classichome;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Window;
import android.view.WindowManager;

import com.sprd.PlatformHelper;
import com.sprd.android.support.featurebar.FeatureBarHelper;
import com.sprd.common.util.FeatureBarUtil;
import com.sprd.common.util.KeyCodeEventUtil;
import com.sprd.common.util.LogUtils;
import com.sprd.simple.launcher2.R;

/**
 * Created by SPRD on 9/26/17.
 */
public abstract class BaseHomeActivity extends Activity {

    private static final String TAG = "BaseHomeActivity";

    protected Drawable mDefaultWindowBg;
    protected Drawable mWindowBg;
    protected FeatureBarHelper mFeatureBarHelper;

    private float mWindowBgAlpha;
    private float mSoftBarAlpha;
    private boolean mLongPressKey;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mDefaultWindowBg = getWindow().getDecorView().getBackground();
        mWindowBg = new ColorDrawable(Color.BLACK);
        mWindowBgAlpha = getResources().getInteger(R.integer.window_background_alpha) / 100f;
        mSoftBarAlpha = getResources().getInteger(R.integer.softbar_background_alpha) / 100f;
    }

    @SuppressWarnings("UnusedReturnValue")
    protected FeatureBarHelper setupFeatureBar(Activity activity) {
        if (PlatformHelper.isTargetBuild() && mFeatureBarHelper == null) {
            mFeatureBarHelper = new FeatureBarHelper(activity);
        }
        return mFeatureBarHelper;
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (!KeyCodeEventUtil.isLauncherNeedUseKeycode(keyCode)) {
            return super.onKeyUp(keyCode, event);
        }

        boolean result = false;
        if (event.isTracking() && !event.isCanceled()) {
            if (!mLongPressKey) {
                // Проверка, подходят ли кнопки под начало номера телефона
                result = KeyCodeEventUtil.pressKeyEventForMainActivity(this, keyCode, event);
            }
        }
        if (!result) {
            result = super.onKeyUp(keyCode, event);
        }

        if (LogUtils.DEBUG) LogUtils.d(TAG, "onKeyUp: result = " + result);
        return result;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (LogUtils.DEBUG) LogUtils.d(TAG, "onKeyDown: keyCode = " + keyCode);
        if (!KeyCodeEventUtil.isLauncherNeedUseKeycode(keyCode)) {
            return super.onKeyDown(keyCode, event);
        }

        int repeatCount = event.getRepeatCount();
        if (repeatCount == 0) {
            event.startTracking();
            mLongPressKey = false;
        } else if (repeatCount > 0) {
            mLongPressKey = true;
        }
        return true;
    }

    @Override
    public boolean onKeyLongPress(int keyCode, KeyEvent event) {
        if (!KeyCodeEventUtil.isLauncherNeedUseKeycode(keyCode)) {
            return super.onKeyLongPress(keyCode, event);
        }

        //noinspection SimplifiableConditionalExpression
        boolean result = KeyCodeEventUtil.longPressKeyEventForMainActivity(this, keyCode) ?
                true : super.onKeyLongPress(keyCode, event);

        if (LogUtils.DEBUG) LogUtils.d(TAG, "onKeyLongPress: result = " + result);
        return result;
    }

    protected void enableWallpaperShowing(boolean enable) {
        if (LogUtils.DEBUG) {
            LogUtils.d(TAG, "enableWallpaperShowing: " + enable);
        }
        updateWallpaperVisibility(enable);
    }

    private void updateWallpaperVisibility(boolean visible) {
        Window win = getWindow();

        int wpflags = visible ? WindowManager.LayoutParams.FLAG_SHOW_WALLPAPER : 0;
        int curflags = win.getAttributes().flags
                & WindowManager.LayoutParams.FLAG_SHOW_WALLPAPER;
        if (wpflags != curflags) {
            win.setFlags(wpflags, WindowManager.LayoutParams.FLAG_SHOW_WALLPAPER);
        }

        mWindowBg.setAlpha(Math.round(255 * mWindowBgAlpha));
        win.setBackgroundDrawable(visible ? mWindowBg : mDefaultWindowBg);

        FeatureBarUtil.setBackgroundAlpha(mFeatureBarHelper,
                Math.round(255 * (visible ? mSoftBarAlpha : 1.0f)));
    }
}
