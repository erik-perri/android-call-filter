package com.novyr.callfilter.db.entity.enums;

public enum LogAction {
    BLOCKED(0),
    ALLOWED(1),
    FAILED(2),
    // The call was answered and immediately ended so it would not reach voicemail.
    ENDED_NO_VOICEMAIL(3),
    // A rule asked for answer-then-hang-up but the call could only be blocked (capability
    // inactive, or the answer never connected). The caller may still have reached voicemail.
    FELL_BACK_TO_BLOCK(4);

    private final int code;

    LogAction(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}
