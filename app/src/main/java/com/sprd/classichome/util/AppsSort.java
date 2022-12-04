package com.sprd.classichome.util;

import com.sprd.classichome.AppItemInfo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

/**
 * Created by SPREADTRUM on 17-11-3.
 */

public final class AppsSort {
    @SuppressWarnings("unused")
    private final static String TAG = "AppsSort";

    public enum SortType {NAME, POSITION}

    @SuppressWarnings("Convert2Lambda")
    private final static Comparator<AppItemInfo> POSITION_METHODS = new Comparator<AppItemInfo>() {
        @SuppressWarnings("FinalMethodInFinalClass")
        public final int compare(final AppItemInfo a, final AppItemInfo b) {
            if (a == null || b == null) {
                throw new RuntimeException("Comparator AppItemInfo should not be null!");
            }
            //noinspection ComparatorMethodParameterNotUsed
            return (a.position < b.position) ? -1 : 1;
        }
    };

    private final static Comparator<AppItemInfo> NAME_METHODS = new NameComparator();

    public static void sort(final ArrayList<AppItemInfo> srcList, final SortType type) {
        switch (type) {
            case NAME:
                Collections.sort(srcList, NAME_METHODS);
                break;
            case POSITION:
                Collections.sort(srcList, POSITION_METHODS);
                break;
            default:
                break;
        }
    }

    public static void verifyPosition(final ArrayList<AppItemInfo> srcList) {
        ArrayList<AppItemInfo> posList = new ArrayList<>();
        ArrayList<AppItemInfo> tmpList = new ArrayList<>();

        for (AppItemInfo info : srcList) {
            if (info.position >= AppItemInfo.MIN_POSITION && info.position != AppItemInfo.POSITION_INVALID) {
                posList.add(info);
            } else {
                tmpList.add(info);
            }
        }
        sort(posList, SortType.POSITION);

        for (int i = 0; i < posList.size(); i++) {
            AppItemInfo appInfo = posList.get(i);
            int index = appInfo.position - 1;
            if (index < tmpList.size()) {
                tmpList.add(index, appInfo);
            } else {
                tmpList.add(appInfo);
            }
        }
        srcList.clear();
        srcList.addAll(tmpList);
    }
}
