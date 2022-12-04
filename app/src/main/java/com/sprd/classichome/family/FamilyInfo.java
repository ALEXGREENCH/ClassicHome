package com.sprd.classichome.family;

/**
 * Created by SPRD on 2017/9/21.
 */

class FamilyInfo {
    @SuppressWarnings("FieldMayBeFinal")
    private int mFamilyId;
    @SuppressWarnings("FieldMayBeFinal")
    private String mFamilyName;
    @SuppressWarnings("FieldMayBeFinal")
    private String mFamilyNumber;

    FamilyInfo(int id, String name, String phoneNumber) {
        mFamilyId = id;
        mFamilyName = name;
        mFamilyNumber = phoneNumber;
    }

    String getFamilyName() {
        return mFamilyName;
    }

    String getFamilyNumber() {
        return mFamilyNumber;
    }

    int getFamilyId() {
        return mFamilyId;
    }
}
