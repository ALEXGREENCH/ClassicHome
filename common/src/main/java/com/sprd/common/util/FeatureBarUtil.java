package com.sprd.common.util;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.sprd.android.support.featurebar.FeatureBarHelper;

/**
 * Created by Spreadtrum on 2017/9/30.
 */
public class FeatureBarUtil {

    public enum SoftKey {LFK, MDK, RTK}

    public static void showFeatureBar(FeatureBarHelper fbh) {
        if (fbh != null) {
            ViewGroup featureBar = fbh.getFeatureBar();
            if (featureBar != null) {
                featureBar.setVisibility(View.VISIBLE);
            }
        }
    }

    public static void hideFeatureBar(FeatureBarHelper fbh) {
        if (fbh != null) {
            ViewGroup featureBar = fbh.getFeatureBar();
            if (featureBar != null) {
                featureBar.setVisibility(View.GONE);
            }
        }
    }

    public static void showSoftKey(FeatureBarHelper fbh, SoftKey key) {
        TextView v = getTextView(fbh, key);
        if (v != null) {
            v.setVisibility(View.VISIBLE);
        }
    }

    public static void hideSoftKey(FeatureBarHelper fbh, SoftKey key) {
        TextView v = getTextView(fbh, key);
        if (v != null) {
            v.setVisibility(View.GONE);
        }
    }

    public static void setText(FeatureBarHelper fbh, SoftKey key, CharSequence text) {
        TextView v = getTextView(fbh, key);
        if (v != null) {
            v.setCompoundDrawables(null, null, null, null);
            v.setText(text);
        }
    }

    public static void setText(Context context, FeatureBarHelper fbh, SoftKey key, int resId) {
        if (context != null) {
            setText(fbh, key, context.getResources().getText(resId));
        }
    }

    public static void setTextColor(FeatureBarHelper fbh, SoftKey key, int color) {
        TextView v = getTextView(fbh, key);
        if (v != null) {
            v.setTextColor(color);
        }
    }

    public static void setTextColor(Context context, FeatureBarHelper fbh, SoftKey key, int resId) {
        if (context != null) {
            setTextColor(fbh, key, context.getResources().getColor(resId));
        }
    }

    public static void setIcon(FeatureBarHelper fbh, SoftKey key, Drawable icon, int iconSize) {
        TextView v = getTextView(fbh, key);
        if (v != null && icon != null) {
            if (iconSize > 0) {
                icon.setBounds(0, 0, iconSize, iconSize);
            } else {
                icon.setBounds(0, 0, icon.getMinimumWidth(), icon.getMinimumHeight());
            }
            v.setText("");
            Drawable left = null;
            Drawable right = null;
            Drawable top = null;
            switch (key) {
                case LFK:
                    left = icon;
                    break;
                case MDK:
                    top = icon;
                    break;
                case RTK:
                    right = icon;
                    break;
                default:
                    break;
            }
            v.setCompoundDrawables(left, top, right, null);
        }
    }

    public static void setIcon(FeatureBarHelper fbh, SoftKey key, Drawable icon) {
        setIcon(fbh, key, icon, 0);
    }

    public static void setIcon(Context context, FeatureBarHelper fbh, SoftKey key, int resId) {
        if (context != null) {
            setIcon(fbh, key, context.getResources().getDrawable(resId));
        }
    }

    public static void setBackgroundDrawable(FeatureBarHelper fbh, Drawable drawable) {
        if (fbh != null) {
            ViewGroup featureBar = fbh.getFeatureBar();
            if (featureBar != null) {
                featureBar.setBackground(drawable);
            }
        }
    }

    public static void setBackgroundDrawable(Context context, FeatureBarHelper fbh, int resId) {
        if (context != null) {
            setBackgroundDrawable(fbh, context.getResources().getDrawable(resId));
        }
    }

    public static void setBackgroundColor(FeatureBarHelper fbh, int color) {
        if (fbh != null) {
            ViewGroup featureBar = fbh.getFeatureBar();
            if (featureBar != null) {
                featureBar.setBackgroundColor(color);
            }
        }
    }

    public static void setBackgroundColor(Context context, FeatureBarHelper fbh, int resId) {
        if (context != null) {
            setBackgroundColor(fbh, context.getResources().getColor(resId));
        }
    }

    public static void setBackgroundAlpha(FeatureBarHelper fbh, int alpha) {
        Drawable bg = getBackground(fbh);
        if (bg != null) {
            bg.setAlpha(alpha);
        }
    }

    private static Drawable getBackground(FeatureBarHelper fbh) {
        if (fbh != null) {
            ViewGroup featureBar = fbh.getFeatureBar();
            if (featureBar != null) {
                return featureBar.getBackground();
            }
        }
        return null;
    }

    private static TextView getTextView(FeatureBarHelper fbh, SoftKey key) {
        if (fbh == null) {
            return null;
        }

        TextView v = null;
        switch (key) {
            case LFK:
                v = (TextView) fbh.getOptionsKeyView();
                break;
            case MDK:
                v = (TextView) fbh.getCenterKeyView();
                break;
            case RTK:
                v = (TextView) fbh.getBackKeyView();
                break;
            default:
                break;
        }

        return v;
    }
}
