package com.novyr.callfilter.db.entity.enums;

public enum RuleAction {
    BLOCK(1),
    ALLOW(2);

    private final int mCode;

    RuleAction(int code) {
        mCode = code;
    }

    public int getCode() {
        return mCode;
    }
}
