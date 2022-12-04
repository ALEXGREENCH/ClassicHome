package com.sprd.classichome.util;

/**
 * Copyright (C) 2015 The Android Open Source Project
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

import android.content.ComponentName;
import android.content.Context;
import android.os.Process;
import android.os.UserHandle;
import android.os.UserManager;

import com.sprd.common.util.Utilities;

import java.util.Arrays;

public class ComponentKey {

    public final ComponentName componentName;
    public final UserHandle user;

    private final int mHashCode;

    public ComponentKey(ComponentName componentName, UserHandle user) {

        this.componentName = componentName;
        this.user = Utilities.getNoEmptyUser(user);
        mHashCode = Arrays.hashCode(new Object[]{componentName, user});

    }

    /**
     * Creates a new component key from an encoded component key string in the form of
     * [flattenedComponentString#userId].  If the userId is not present, then it defaults
     * to the current user.
     */
    @SuppressWarnings("unused")
    public ComponentKey(Context context, String componentKeyStr) {
        UserManager userManager = (UserManager) context.getSystemService(Context.USER_SERVICE);
        int userDelimiterIndex = componentKeyStr.indexOf("#");
        if (userDelimiterIndex != -1) {
            String componentStr = componentKeyStr.substring(0, userDelimiterIndex);
            //noinspection WrapperTypeMayBePrimitive
            Long componentUser = Long.valueOf(componentKeyStr.substring(userDelimiterIndex + 1));
            componentName = ComponentName.unflattenFromString(componentStr);
            user = userManager == null ? null : userManager.getUserForSerialNumber(componentUser);
        } else {
            // No user provided, default to the current user
            componentName = ComponentName.unflattenFromString(componentKeyStr);
            user = Process.myUserHandle();
        }
        mHashCode = Arrays.hashCode(new Object[]{componentName, user});
    }

    @Override
    public int hashCode() {
        return mHashCode;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof ComponentKey) {
            ComponentKey other = (ComponentKey) o;
            return other.componentName.equals(componentName) && other.user.equals(user);
        }
        return super.equals(o);
    }

    /**
     * Encodes a component key as a string of the form [flattenedComponentString#userId].
     */
    @SuppressWarnings("NullableProblems")
    @Override
    public String toString() {
        return componentName.flattenToString() + "#" + user;
    }

    @SuppressWarnings("unused")
    public String flattenToString(Context context) {
        UserManager userManager = (UserManager) context.getSystemService(Context.USER_SERVICE);
        String flattened = componentName.flattenToShortString();
        if (user != null && userManager != null) {
            flattened += "#" + userManager.getSerialNumberForUser(user);
        }
        return flattened;
    }
}