package com.novyr.callfilter.db.entity.enums;

import androidx.annotation.StringRes;

import com.novyr.callfilter.R;

public enum RuleType {
    UNMATCHED(1, R.string.rule_type_unmatched),
    RECOGNIZED(2, R.string.rule_type_recognized),
    UNRECOGNIZED(3, R.string.rule_type_unrecognized),
    PRIVATE(4, R.string.rule_type_private),
    AREA_CODE(5, R.string.rule_type_area_code),
    MATCH(6, R.string.rule_type_match);

    private final int mCode;
    private final int mDisplayNameResource;

    RuleType(int code, @StringRes int displayNameResource) {
        mCode = code;
        mDisplayNameResource = displayNameResource;
    }

    public int getCode() {
        return mCode;
    }

    public int getDisplayNameResource() {
        return mDisplayNameResource;
    }
}
