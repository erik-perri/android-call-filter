package com.novyr.callfilter.rules.exception;

import androidx.annotation.StringRes;

public class InvalidValueException extends Throwable {
    private final int mLabelResource;

    public InvalidValueException(@StringRes int labelResource) {
        mLabelResource = labelResource;
    }

    @StringRes
    public int getLabelResource() {
        return mLabelResource;
    }
}
