package com.sprd.simple.util;

import android.content.Context;

/**
 * Created by SPDR on 2016/10/25.
 */

public class DimensUtil {
    public static int dip2px(Context context, float dpValue) {
    final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }
}
