package com.novyr.callfilter;

import com.novyr.callfilter.db.entity.enums.RuleAction;

/**
 * The outcome of evaluating the rules for a call: the action to perform, already resolved to one
 * this device can currently carry out. When a matched rule asked for ANSWER_AND_END but the
 * hang-up capability is inactive (permission revoked or never granted), the action degrades to
 * BLOCK and {@link #fellBackToBlock()} is set so the log can record the difference.
 */
public final class CallDecision {
    private final RuleAction mAction;
    private final boolean mFellBackToBlock;

    CallDecision(RuleAction action, boolean fellBackToBlock) {
        mAction = action;
        mFellBackToBlock = fellBackToBlock;
    }

    public RuleAction getAction() {
        return mAction;
    }

    public boolean fellBackToBlock() {
        return mFellBackToBlock;
    }
}
