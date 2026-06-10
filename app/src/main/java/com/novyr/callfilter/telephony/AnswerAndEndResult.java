package com.novyr.callfilter.telephony;

public enum AnswerAndEndResult {
    /** The call connected and was then ended; the caller was dropped without reaching voicemail. */
    ANSWERED_AND_ENDED,

    /**
     * The call never connected within the timeout and was ended as a plain reject. The carrier
     * will treat it like a block, so the caller may still reach voicemail.
     */
    ENDED_WITHOUT_ANSWER,

    /** The call could not be ended at all. */
    FAILED,
}
