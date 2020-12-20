package com.novyr.callfilter.db.entity.enums;

public enum LogAction {
    BLOCKED(0),
    ALLOWED(1),
    FAILED(2);

    private final int code;

    LogAction(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}
