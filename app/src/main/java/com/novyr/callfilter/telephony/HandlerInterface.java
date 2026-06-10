package com.novyr.callfilter.telephony;

public interface HandlerInterface {
    /**
     * @return Whether the call was successfully ended
     */
    boolean endCall();

    /**
     * Answer the ringing call, then immediately end it so the caller is dropped instead of being
     * forwarded to voicemail.
     */
    AnswerAndEndResult answerAndEnd();
}
