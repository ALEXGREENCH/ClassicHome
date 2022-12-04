/*
 * Copyright (C) 2016 The Android Open Source Project
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
package com.sprd.classichome.util;

import com.sprd.classichome.AppItemInfo;

import java.text.Collator;
import java.util.Comparator;

/**
 * Extension of {@link Collator} with special handling for digits. Used for comparing
 * user visible labels.
 */
public class NameComparator implements Comparator<AppItemInfo> {

    private final Collator mCollator = Collator.getInstance();

    @Override
    public int compare(AppItemInfo appA, AppItemInfo appB) {
        // Ensure that we de-prioritize any titles that don't start with a
        // linguistic letter or digit

        String titleA = appA.title == null ? "" : appA.title.toString();
        String titleB = appB.title == null ? "" : appB.title.toString();

        boolean aStartsWithLetter = (titleA.length() > 0) &&
                Character.isLetterOrDigit(titleA.codePointAt(0));
        boolean bStartsWithLetter = (titleB.length() > 0) &&
                Character.isLetterOrDigit(titleB.codePointAt(0));
        if (aStartsWithLetter && !bStartsWithLetter) {
            return -1;
        } else if (!aStartsWithLetter && bStartsWithLetter) {
            return 1;
        }

        // Order by the title in the current locale
        return mCollator.compare(titleA, titleB);
    }
}
