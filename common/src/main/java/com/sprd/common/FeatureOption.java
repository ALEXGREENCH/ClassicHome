package com.sprd.common;

import com.sprd.common.util.SystemPropertiesUtils;

/**
 * Created by SPREADTRUM on 18-1-31.
 */

public class FeatureOption {
        public static final String TAG = "FeatureOption";

        //whether support lunar.
        public static boolean LUNAR_SUPPORT = SystemPropertiesUtils.getBoolean("ro.simplehome.lunar", false);

        //whether remove application icon's transparent region.
        public static final boolean SPRD_REMOVE_APP_ICON_TRANSPADDING_SUPPORT = true;

        //whether show carrier text.
        public static final boolean SPRD_SHOW_CARRIER_SUPPORT = false;
}