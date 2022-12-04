/*
 * Copyright (C) 2007 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.sprd.classichome;

import android.content.ComponentName;
import android.graphics.drawable.Drawable;
import android.os.UserHandle;

import com.sprd.classichome.util.ComponentKey;
import com.sprd.common.util.Utilities;

/**
 * Represents a launchable application. An application is made of a name (or title), an intent
 * and an icon.
 */
public class AppItemInfo {

    public static final int POSITION_INVALID = Integer.MAX_VALUE;

    public static final String GROUP_MAIN_MENU = "mainmenu";

    public static final String GROUP_EXTRA = "extra";

    public static final String GROUP_HIDE = "hide";

    public static final String DEFAULT_GROUP = GROUP_MAIN_MENU;

    public static final int MIN_POSITION = 1;
    /**
     * The application name.
     */
    public CharSequence title;

    /**
     * The application icon.
     */
    public Drawable icon;

    /**
     * The application package name.
     */
    final public String pkgName;

    /**
     * The application class name.
     */
    final public String clsName;

    /**
     * The application group.
     */
    public String group;

    /**
     * The application group.
     */
    public int position;

    /**
     * The application user.
     */
    final public UserHandle user;

    /**
     * Indicates whether the icon comes from an application's resource (if false)
     * or from a custom Bitmap (if true.)
     */
    public boolean iconCustomized;

    /**
     * When set to true, indicates that the icon has been resized.
     */
    @SuppressWarnings("unused")
    boolean filtered;


    public AppItemInfo(CharSequence title, Drawable icon, String pkg, String cls) {
        this(title, icon, pkg, cls, DEFAULT_GROUP, POSITION_INVALID);
    }

    public AppItemInfo(CharSequence title, Drawable icon, String pkg, String cls, String group, int position) {
        this.title = title;
        this.icon = icon;
        this.pkgName = pkg == null ? "" : pkg;
        this.clsName = cls == null ? "" : cls;
        this.group = isGroupValid(group) ? group : DEFAULT_GROUP;
        this.position = position < MIN_POSITION ? POSITION_INVALID : position;
        this.user = Utilities.getNoEmptyUser(null);
    }

    public boolean isGroupValid(String group) {
        //noinspection RedundantIfStatement
        if (GROUP_MAIN_MENU.equals(group)
                || GROUP_EXTRA.equals(group)
                || GROUP_HIDE.equals(group)) {
            return true;
        }
        return false;
    }

    public ComponentKey getComponentKey() {
        return new ComponentKey(new ComponentName(pkgName, clsName), user);
    }

    public ComponentName getTargetComponent() {
        return new ComponentName(pkgName, clsName);
    }
}
