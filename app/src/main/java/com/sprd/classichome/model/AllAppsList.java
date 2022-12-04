/*
 * Copyright (C) 2008 The Android Open Source Project
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

package com.sprd.classichome.model;

import android.content.ComponentName;
import android.content.Context;
import android.os.UserHandle;

import com.sprd.classichome.AppItemInfo;
import com.sprd.classichome.util.ComponentKey;
import com.sprd.classichome.util.UtilitiesExt;

import java.util.ArrayList;
import java.util.HashMap;


/**
 * Stores the list of all applications for the all apps view.
 */
public class AllAppsList {

    /** The list off all apps. */
    public final HashMap<ComponentKey, AppItemInfo> data = new HashMap<>();

    /** The list of apps that have been added since the last notify() call. */
    public final ArrayList<AppItemInfo> added = new ArrayList<>();

    /** The list of apps that have been removed since the last notify() call. */
    public final ArrayList<ComponentKey> removed = new ArrayList<>();

    public void put(AppItemInfo info){
        data.put(info.getComponentKey(), info);
    }

    @SuppressWarnings("unused")
    public void put(ComponentKey cpk, AppItemInfo info){
        data.put(cpk, info);
    }

    public void remove(AppItemInfo info){
        data.remove(info.getComponentKey());
    }

    @SuppressWarnings("unused")
    public AppItemInfo get(ComponentKey cpk){
        return data.get(cpk);
    }

    public void clear() {
        data.clear();
        // TODO: do we clear these too?
        synchronized (added) {
            added.clear();
        }
        synchronized (removed) {
            removed.clear();
        }
    }

    @SuppressWarnings("unused")
    public int size() {
        return data.size();
    }

    /**
     * Add the icons for the supplied apk called packageName.
     */
    public void addPackage(Context context, String packageName, UserHandle user) {
        final ArrayList<AppItemInfo> activities = UtilitiesExt.findActivitiesForPackage(context, packageName, user);
        synchronized (added) {
            added.addAll(activities);
        }
    }

    public void removePackage(@SuppressWarnings("unused") Context context, String packageName, UserHandle user) {
        ComponentKey cpk = new ComponentKey(new ComponentName(packageName,""),user);
        synchronized (removed) {
            removed.add(cpk);
        }
    }

    public void updatePackage(Context context, String packageName, UserHandle user) {
        removePackage(context, packageName, user);
        addPackage(context, packageName, user);
    }

}


