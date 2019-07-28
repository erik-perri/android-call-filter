package com.novyr.callfilter.db.entity.enums;

public enum Action {
    BLOCKED(0),
    ALLOWED(1),
    FAILED(2);

    private final int code;

    Action(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}
