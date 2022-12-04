package com.sprd.wallpaperpicker;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.WallpaperManager;
import android.os.Bundle;
import android.view.KeyEvent;
import android.widget.Toast;

import com.sprd.PlatformHelper;
import com.sprd.android.support.featurebar.FeatureBarHelper;
import com.sprd.common.util.FeatureBarUtil;
import com.sprd.common.util.LogUtils;

public class PreviewWallpaperActivity extends Activity {
    private static final boolean DEBUG = LogUtils.DEBUG_ALL;
    private static final String TAG = "PreviewWallpaperActivity";
    private static final int RESULT_CODE_FINISH = -1;
    private int mPosition = 0;
    private int mWallpaperCount;
    private FeatureBarHelper mFeatureBarHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (DEBUG) LogUtils.i(TAG, "onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.previewwallpaper_layout);

        if (getIntent().getExtras() != null) {
            mPosition = getIntent().getExtras().getInt("Position");
        }
        mWallpaperCount = WallpaperUtil.getWallpaperResCount(this);
        updateBackgroundImage();
        setSoftKey();
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        boolean returnValue = false;
        if (DEBUG) LogUtils.i(TAG, "keyCode = " + keyCode);
        if (event.isTracking() && !event.isCanceled()) {
            if (DEBUG) LogUtils.i(TAG, "onKeyUp  short press");
            switch (keyCode) {
                case KeyEvent.KEYCODE_DPAD_LEFT:
                    handleLeftKey();
                    returnValue = true;
                    break;
                case KeyEvent.KEYCODE_DPAD_RIGHT:
                    handleRightKey();
                    returnValue = true;
                    break;
                case KeyEvent.KEYCODE_DPAD_CENTER:
                    handleCenterKey();
                    returnValue = true;
                    break;
                case KeyEvent.KEYCODE_BACK:
                    handleBackKey();
                    returnValue = super.onKeyUp(keyCode, event);
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

    private void setSoftKey() {
        if (PlatformHelper.isTargetBuild() && mFeatureBarHelper == null) {
            mFeatureBarHelper = new FeatureBarHelper(this);
        }
        float softBarAlpha = getResources().getInteger(R.integer.wallpaper_softbar_background_alpha) / 100f;
        FeatureBarUtil.setBackgroundAlpha(mFeatureBarHelper,
                Math.round(255 * softBarAlpha));
        FeatureBarUtil.hideSoftKey(mFeatureBarHelper, FeatureBarUtil.SoftKey.LFK);
        FeatureBarUtil.setTextColor(this, mFeatureBarHelper, FeatureBarUtil.SoftKey.MDK, R.color.softbar_font_color);
        FeatureBarUtil.setTextColor(this, mFeatureBarHelper, FeatureBarUtil.SoftKey.RTK, R.color.softbar_font_color);
    }

    private void handleRightKey() {
        mPosition = (mPosition + 1) % mWallpaperCount;
        updateBackgroundImage();
    }

    private void handleLeftKey() {
        mPosition = mPosition > 0 ? mPosition - 1 : mWallpaperCount - 1;
        updateBackgroundImage();
    }

    private void handleBackKey() {
        setResult(mPosition);
    }

    private void handleCenterKey() {
        if (setWallpaper()) {
            String message = getString(R.string.set_wallpaper_success);
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        }
        setResult(RESULT_CODE_FINISH);
        finish();
    }

    private void updateBackgroundImage() {
        if (DEBUG) LogUtils.i(TAG, "updateBackgroundImage mPosition = " + mPosition);
        this.getWindow().getDecorView().setBackgroundResource(WallpaperUtil.getWallpaperRes(this, mPosition));
    }

    @SuppressLint("MissingPermission")
    private boolean setWallpaper() {
        WallpaperManager wallpaperManager = (WallpaperManager) getSystemService(WALLPAPER_SERVICE);
        try {
            wallpaperManager.setResource(WallpaperUtil.getWallpaperRes(this, mPosition));
            return true;
        } catch (Exception e) {
            LogUtils.w(TAG, "setWallpaper fail", e);
        }
        return false;
    }
}
