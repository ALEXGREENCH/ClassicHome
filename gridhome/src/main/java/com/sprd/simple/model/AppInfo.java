package com.sprd.simple.model;

import android.graphics.drawable.Drawable;

public class AppInfo {
    private String appLabel;
    private int appIcon;
    private Drawable appDrawableIcon;
    private String appPkg;
    private String appClassName;
    private int background;

    public AppInfo() {

    }

    public String getAppLabel() {
        return appLabel;
    }

    public void setAppLabel(String appLabel) {
        this.appLabel = appLabel;
    }

    public int getAppIcon() {
        return appIcon;
    }

    public void setAppIcon(int appIcon) {
        this.appIcon = appIcon;
    }

    public String getAppPkg() {
        return appPkg;
    }

    public void setAppPkg(String appPkg) {
        this.appPkg = appPkg;
    }

    public Drawable getAppDrawableIcon() {
        return appDrawableIcon;
    }

    public void setAppDrawableIcon(Drawable appDrawableIcon) {
        this.appDrawableIcon = appDrawableIcon;
    }

    public String getAppClassName() {
        return appClassName;
    }

    public void setAppClassName(String appClassName) {
        this.appClassName = appClassName;
    }

    public int getBackground() {
        return background;
    }

    public void setBackground(int background) {
        this.background = background;
    }
}
