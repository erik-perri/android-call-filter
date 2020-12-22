package com.novyr.callfilter.db.entity.enums;

import android.os.Build;

import androidx.annotation.StringRes;

import com.novyr.callfilter.R;

public enum RuleType {
    UNMATCHED(1, R.string.rule_type_unmatched),
    RECOGNIZED(2, R.string.rule_type_recognized),
    UNRECOGNIZED(3, R.string.rule_type_unrecognized),
    PRIVATE(4, R.string.rule_type_private),
    AREA_CODE(5, R.string.rule_type_area_code),
    MATCH(6, R.string.rule_type_match),
    VERIFICATION_FAILED(7, R.string.rule_type_verification_failed, Build.VERSION_CODES.R),
    VERIFICATION_PASSED(8, R.string.rule_type_verification_passed, Build.VERSION_CODES.R);

    private final int mCode;
    private final int mDisplayNameResource;
    private final int mMinSdkVersion;

    RuleType(int code, @StringRes int displayNameResource) {
        mCode = code;
        mDisplayNameResource = displayNameResource;
        mMinSdkVersion = 0;
    }

    RuleType(int code, @StringRes int displayNameResource, int minSdkVersion) {
        mCode = code;
        mDisplayNameResource = displayNameResource;
        mMinSdkVersion = minSdkVersion;
    }

    public int getCode() {
        return mCode;
    }

    public int getDisplayNameResource() {
        return mDisplayNameResource;
    }

    public int getMinSdkVersion() {
        return mMinSdkVersion;
    }
}
