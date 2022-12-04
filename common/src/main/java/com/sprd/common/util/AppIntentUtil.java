package com.sprd.common.util;

import android.content.Intent;

/**
 * Created by SPRD on 12/26/17.
 */
public class AppIntentUtil {

    public static void intentSetFlag(Intent intent) {
        intent.setAction(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
    }
}
