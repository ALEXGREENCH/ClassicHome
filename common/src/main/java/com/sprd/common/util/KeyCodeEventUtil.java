package com.sprd.common.util;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.net.Uri;
import android.view.KeyEvent;
import android.widget.Toast;

import org.json.JSONArray;

/**
 * Created by SPRD on 12/26/17.
 */
public class KeyCodeEventUtil {
    private static final String TAG = "KeyCodeEventUtil";

    private static final String VOICE_MAIL_BROADCAST = "android.intent.action.VoiceMail";
    private static final String SOS_BROADCAST = "android.intent.action.SOS";
    private static final String PHONE_ACCOUNT_SCHEME_TEL = "tel:";
    private static final int KEYCODE_OFFSET = 7;
    private static final int KEYCODE_OFFSET_LONG_PRESS = 9;
    private static final boolean ENABLE_FLASHLIGHT_BY_CENTER_KEY = SystemPropertiesUtils.getBoolean("ro.home.flashlight.centerkey", false);

    private static String[] spData = new String[2];
    private static String[] mStrArray = new String[2];

    public static boolean isLauncherNeedUseKeycode(int keyCode) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_ENTER:
            case KeyEvent.KEYCODE_BACK:
            case KeyEvent.KEYCODE_CALL:
            case KeyEvent.KEYCODE_0:
            case KeyEvent.KEYCODE_1:
            case KeyEvent.KEYCODE_2:
            case KeyEvent.KEYCODE_3:
            case KeyEvent.KEYCODE_4:
            case KeyEvent.KEYCODE_5:
            case KeyEvent.KEYCODE_6:
            case KeyEvent.KEYCODE_7:
            case KeyEvent.KEYCODE_8:
            case KeyEvent.KEYCODE_9:
            case KeyEvent.KEYCODE_STAR:
            case KeyEvent.KEYCODE_POUND:
            case KeyEvent.KEYCODE_DPAD_UP:
            case KeyEvent.KEYCODE_DPAD_DOWN:
            case KeyEvent.KEYCODE_DPAD_LEFT:
            case KeyEvent.KEYCODE_DPAD_RIGHT:
            case KeyEvent.KEYCODE_DPAD_CENTER:
            case KeyEvent.KEYCODE_MENU:
                return true;
            default:
                return false;
        }
    }

    public static boolean pressKeyEventForMainActivity(Context context,
                                                       int keyCode, KeyEvent event) {
        boolean result = true;

        switch (keyCode) {
            case KeyEvent.KEYCODE_0:
            case KeyEvent.KEYCODE_1:
            case KeyEvent.KEYCODE_2:
            case KeyEvent.KEYCODE_3:
            case KeyEvent.KEYCODE_4:
            case KeyEvent.KEYCODE_5:
            case KeyEvent.KEYCODE_6:
            case KeyEvent.KEYCODE_7:
            case KeyEvent.KEYCODE_8:
            case KeyEvent.KEYCODE_9:
                startDialActivityByKeyCode(context, (keyCode - KEYCODE_OFFSET) + "");
                break;
            case KeyEvent.KEYCODE_STAR:
                startDialActivityByKeyCode(context, HomeConstants.KEYCODE_STAR);
                break;
            case KeyEvent.KEYCODE_POUND:
                startDialActivityByKeyCode(context, HomeConstants.KEYCODE_POUND);
                break;
            case KeyEvent.KEYCODE_CALL:
                Utilities.startActivity(context, Utilities.CALL_LOG);
                break;
            default:
                result = false;
                break;
        }
        return result;
    }

    private static void startDialActivityByKeyCode(Context context, String keyCode) {
        Intent it = new Intent(Intent.ACTION_DIAL, Uri.parse(PHONE_ACCOUNT_SCHEME_TEL + keyCode));
        Utilities.startActivity(context, it);
    }

    private static void startCallActivityByPosition(Context context, int position) {
        getFamilyNumber(context, position, spData.length);
        Intent intent = new Intent(Intent.ACTION_CALL);
        Uri data = Uri.parse(PHONE_ACCOUNT_SCHEME_TEL + mStrArray[1]);
        intent.setData(data);
        Utilities.startActivity(context, intent);
    }


    private static void expandNotificationsPanel(Context context) {
        StatusBarUtils.expandNotificationsPanel(context);
    }

    private static void startSos(Context context) {
        Utilities.sendBroadcast(context, new Intent(SOS_BROADCAST), true);
    }

    private static void startVoiceMail(Context context) {
        Utilities.sendBroadcast(context, new Intent(VOICE_MAIL_BROADCAST), true);
    }

    private static void startFlashlight(Context context) {
        if (FlashlightController.switchFlashlight()) {
            FlashlightController.checkFlashlightStatus(context);
        }
    }

    private static void startSilentMode(Context context) {
        AudioManager audioManager = (AudioManager) context
                .getSystemService(Context.AUDIO_SERVICE);
        if (audioManager.getRingerMode() == AudioManager.RINGER_MODE_SILENT) {
            audioManager.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
        } else {
            audioManager.setRingerMode(AudioManager.RINGER_MODE_SILENT);
        }
    }

    private static void onLongPressCenterKey(Context context) {
        if (ENABLE_FLASHLIGHT_BY_CENTER_KEY) {
            startFlashlight(context);
        } else {
            startSos(context);
        }
    }

    private static void onLongPress0Key(Context context) {
        // if the flashlight is turned on/off by center key, then launching the sos feature by zero key.
        if (ENABLE_FLASHLIGHT_BY_CENTER_KEY) {
            startSos(context);
        } else {
            startFlashlight(context);
        }
    }

    private static void showToast(Context context, int resId, Object... formatArgs) {
        String message = context.getString(resId, formatArgs);
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }

    public static boolean longPressKeyEventForMainActivity(Context context, int keyCode) {
        boolean result = true;
        // get keycode from long press
        if (LogUtils.DEBUG) LogUtils.d(TAG, "longPressKeyEvent keyCode = " + keyCode);

        switch (keyCode) {
            case KeyEvent.KEYCODE_1:
                startVoiceMail(context);
                break;
            /*case KeyEvent.KEYCODE_2:// family number 1
            case KeyEvent.KEYCODE_3:// family number 2
            case KeyEvent.KEYCODE_4: {// family number 3
                // init DB
                SharedPreferences sp = context.getSharedPreferences(HomeConstants.FAMILY_NUMBER_DATABASE,
                        Context.MODE_PRIVATE);
                int position = keyCode - KEYCODE_OFFSET_LONG_PRESS;
                if (sp.contains(position + "")) {
                    startCallActivityByPosition(context, position);
                } else {
                    showToast(context, R.string.family_num_not_set, position + 1);
                }
                break;
            }*/
            case KeyEvent.KEYCODE_0:
                onLongPress0Key(context);
                break;
            case KeyEvent.KEYCODE_DPAD_CENTER:
                onLongPressCenterKey(context);
                break;
            case KeyEvent.KEYCODE_POUND:
                startSilentMode(context);
                break;
            case KeyEvent.KEYCODE_MENU:
                expandNotificationsPanel(context);
                break;
            default:
                result = false;
                break;
        }
        return result;
    }

    /**
     * get the data from SharedPreference
     *
     * @param context
     * @param position
     * @param arrayLength
     * @return
     */
    private static String[] getFamilyNumber(Context context, int position,
                                            int arrayLength) {
        try {
            SharedPreferences sharedPreferences = context.getSharedPreferences(
                    HomeConstants.FAMILY_NUMBER_DATABASE, Context.MODE_PRIVATE);
            mStrArray = new String[arrayLength];
            JSONArray jsonArray = new JSONArray(sharedPreferences.getString(
                    position + "", ""));
            for (int i = 0; i < jsonArray.length(); i++) {
                mStrArray[i] = jsonArray.getString(i);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return mStrArray;
    }
}
