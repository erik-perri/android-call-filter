package com.novyr.callfilter.telephony;

import android.os.SystemClock;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.novyr.callfilter.BuildConfig;

/**
 * Drives the timing of the answer-then-hang-up dance, shared by the per-API handlers.
 *
 * <p>Validated on real hardware (Android 11 / Samsung, 2026-05-29): a single early answer is
 * silently dropped while the OEM in-call services are still binding, so the answer must be
 * re-issued until the call actually reaches OFFHOOK (~13 attempts / ~3.25s on the test device).
 * Once connected we hold the line briefly so the carrier unambiguously sees the call as answered
 * (and therefore does not forward it to voicemail), then end it.
 */
final class AnswerAndEndSequence {
    private static final String TAG = AnswerAndEndSequence.class.getSimpleName();

    /** Max time to keep re-issuing the answer / waiting for OFFHOOK before giving up. */
    private static final long OFFHOOK_TIMEOUT_MS = 5000;

    /** How often to re-issue the answer and re-check the call state. */
    private static final long ANSWER_RETRY_INTERVAL_MS = 250;

    /** How long to stay connected before hanging up; tune on hardware if carriers disagree. */
    private static final long CONNECTED_HOLD_MS = 1000;

    interface Telephony {
        /** Issue one answer attempt; failures are tolerated and retried. */
        void attemptAnswer() throws Exception;

        /** @return Whether the call was successfully ended */
        boolean endCall();

        /** @return The current TelephonyManager.CALL_STATE_* value */
        int getCallState();
    }

    private AnswerAndEndSequence() {
    }

    static AnswerAndEndResult run(Telephony telephony) {
        boolean connected = false;
        try {
            connected = answerUntilOffhook(telephony);
            if (connected) {
                Thread.sleep(CONNECTED_HOLD_MS);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // End the call even if it never connected, otherwise it would keep ringing (silenced on
        // Q+) until the carrier gives up and voicemail picks it up anyway.
        if (!telephony.endCall()) {
            return AnswerAndEndResult.FAILED;
        }

        return connected
                ? AnswerAndEndResult.ANSWERED_AND_ENDED
                : AnswerAndEndResult.ENDED_WITHOUT_ANSWER;
    }

    private static boolean answerUntilOffhook(Telephony telephony) throws InterruptedException {
        long deadline = SystemClock.elapsedRealtime() + OFFHOOK_TIMEOUT_MS;
        int attempts = 0;

        while (SystemClock.elapsedRealtime() < deadline) {
            if (telephony.getCallState() == TelephonyManager.CALL_STATE_OFFHOOK) {
                if (BuildConfig.DEBUG) {
                    Log.d(TAG, String.format("Call connected after %d answer attempt(s)", attempts));
                }
                return true;
            }

            try {
                telephony.attemptAnswer();
                attempts++;
            } catch (Exception e) {
                if (BuildConfig.DEBUG) {
                    Log.w(TAG, String.format("Answer attempt %d failed", attempts), e);
                }
            }

            Thread.sleep(ANSWER_RETRY_INTERVAL_MS);
        }

        if (BuildConfig.DEBUG) {
            Log.w(TAG, String.format("Call never connected after %d answer attempt(s)", attempts));
        }
        return false;
    }
}
