package com.sprd;

import android.widget.GridView;

/**
 * Created by SPRD on 9/19/17.
 */
public class PlatformHelper {

    public static boolean isTargetBuild() {
        return true;
    }

    public static boolean isLayoutRtl(GridView view) {
        return false;
    }
}
