package com.novyr.callfilter.db.entity.enums;

import androidx.annotation.Nullable;

import com.novyr.callfilter.permissions.Capability;

public enum RuleAction {
    BLOCK(1),
    ALLOW(2),
    // Answer the call, then immediately hang up. Because the call connects, the carrier does not
    // forward it to voicemail the way it does for BLOCK.
    ANSWER_AND_END(3);

    private final int mCode;

    RuleAction(int code) {
        mCode = code;
    }

    public int getCode() {
        return mCode;
    }

    /**
     * @return The optional capability this action needs to be carried out, or null if none. A
     * matched action whose capability is inactive degrades to BLOCK in the decision layer.
     */
    @Nullable
    public Capability getRequiredCapability() {
        return this == ANSWER_AND_END ? Capability.HANG_UP : null;
    }
}
