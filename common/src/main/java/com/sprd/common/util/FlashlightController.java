package com.sprd.common.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.PrintStream;
import android.provider.Settings;
import android.content.Context;

/**
 * Manages the flashlight.
 */
public class FlashlightController {

    private static final String TAG = "FlashlightController";

    private static final String FLASH_STATUS = "flash_light";

    private static final String SWITCH_ON =
            SystemPropertiesUtils.get("ro.flashlight.on_value", "1");
    private static final String SWITCH_OFF =
            SystemPropertiesUtils.get("ro.flashlight.off_value", "0");
    private static final String FLASH_PATH =
            SystemPropertiesUtils.get("ro.flashlight.node", "/sys/class/flashlight/torch/enable");
    private static final int VALID_INDEX = SWITCH_ON.length();


    public static boolean turnOnFlashlight(boolean force) {
        if (force) {
            return writeFile(SWITCH_ON);
        } else {
            return isFlashlightOn() || turnOnFlashlight(true);
        }
    }

    public static boolean turnOffFlashlight(boolean force) {
        if (force) {
            return writeFile(SWITCH_OFF);
        } else {
            return (!isFlashlightOn()) || turnOffFlashlight(true);
        }
    }

    private static boolean isFlashlightOn() {
        return SWITCH_ON.equals(readFile());
    }


    public static void checkFlashlightStatus(Context context) {
        updateSettingsFlightState(context,isFlashlightOn());
    }

    public static void updateSettingsFlightState(Context context, boolean isOn) {
        try {
            Settings.System.putInt(context.getContentResolver(), FLASH_STATUS, isOn ? 1 : 0);
        } catch (Exception ignore){}
    }


    public static boolean switchFlashlight() {
        boolean ret;
        if (isFlashlightOn()) {
            if (LogUtils.DEBUG) LogUtils.i(TAG, "switchFlashlight, will close.");
            ret = turnOffFlashlight(true);
        } else {
            if (LogUtils.DEBUG) LogUtils.i(TAG, "switchFlashlight, will open.");
            ret = turnOnFlashlight(true);
        }
        if (LogUtils.DEBUG) LogUtils.d(TAG, "switchFlashlight, ret:" + ret);
        return ret;
    }

    private static String readFile() {
        String str = "";
        File flashFile = new File(FLASH_PATH);

        if (flashFile.exists()) {
            BufferedReader reader = null;
            try {
                reader = new BufferedReader(new FileReader(flashFile));
                String line;
                while ((line = reader.readLine()) != null) {
                    str = str + line;
                }

                int length = str.length();
                if(length >= VALID_INDEX){
                    str = str.substring(length - VALID_INDEX, length);
                }
            } catch (Exception e) {
                LogUtils.d(TAG, "Read file error!!!");
                str = "readError";
                e.printStackTrace();
            } finally {
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (Exception e2) {
                        e2.printStackTrace();
                    }
                }
            }
            LogUtils.d(TAG, "read value is " + str.trim());
        } else {
            LogUtils.d(TAG, "File is not exist");
        }
        return str.trim();
    }

    private static boolean writeFile(String str) {
        boolean flag = true;
        FileOutputStream out = null;
        PrintStream p = null;
        File flashFile = new File(FLASH_PATH);

        if (flashFile.exists()) {
            try {
                out = new FileOutputStream(FLASH_PATH);
                p = new PrintStream(out);
                p.print(str);
            } catch (Exception e) {
                flag = false;
                LogUtils.d(TAG, "Write file error!!!");
                e.printStackTrace();
            } finally {
                if (out != null) {
                    try {
                        out.close();
                    } catch (Exception e2) {
                        e2.printStackTrace();
                    }
                }
                if (p != null) {
                    try {
                        p.close();
                    } catch (Exception e2) {
                        e2.printStackTrace();
                    }
                }
            }
        } else {
            LogUtils.d(TAG, "File is not exist");
        }
        return flag;
    }
}
