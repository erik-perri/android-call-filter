package com.novyr.callfilter.telephony;

import android.media.AudioManager;
import android.os.SystemClock;
import android.telephony.TelephonyManager;
import android.util.Log;

import androidx.annotation.Nullable;

import com.novyr.callfilter.BuildConfig;

/**
 * Drives the timing of the answer-then-hang-up dance, shared by the per-API handlers.
 *
 * <p>Validated on real hardware (Android 11 / Samsung, 2026-05-29): a single early answer is
 * silently dropped while the OEM in-call services are still binding, so the answer must be
 * re-issued until the call actually reaches OFFHOOK (~13 attempts / ~3.25s on the test device).
 * Once connected we hold the line briefly so the carrier unambiguously sees the call as answered
 * (and therefore does not forward it to voicemail), then end it.
 *
 * <p>The microphone is muted for the duration so the caller cannot hear the room during the
 * connected moment, then restored.
 */
final class AnswerAndEndSequence {
    private static final String TAG = AnswerAndEndSequence.class.getSimpleName();

    /** Max time to keep re-issuing the answer / waiting for OFFHOOK before giving up. */
    private static final long OFFHOOK_TIMEOUT_MS = 7500;

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

    /**
     * Persists/clears a marker around the microphone mute so a sequence whose process is killed
     * mid-call can be detected and undone at the next process start.
     */
    interface MuteGuard {
        void arm();

        void disarm();

        MuteGuard NO_OP = new MuteGuard() {
            @Override
            public void arm() {
            }

            @Override
            public void disarm() {
            }
        };
    }

    private AnswerAndEndSequence() {
    }

    static AnswerAndEndResult run(
            @Nullable AudioManager audioManager,
            Telephony telephony,
            MuteGuard muteGuard
    ) {
        boolean muted = muteMicrophone(audioManager, muteGuard);

        try {
            boolean connected = false;
            try {
                connected = answerUntilOffhook(telephony);
                if (connected) {
                    Thread.sleep(CONNECTED_HOLD_MS);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            // End the call even if it never connected, otherwise it would keep ringing (silenced
            // on Q+) until the carrier gives up and voicemail picks it up anyway.
            if (!telephony.endCall()) {
                return AnswerAndEndResult.FAILED;
            }

            return connected
                    ? AnswerAndEndResult.ANSWERED_AND_ENDED
                    : AnswerAndEndResult.ENDED_WITHOUT_ANSWER;
        } finally {
            if (muted) {
                unmuteMicrophone(audioManager, muteGuard);
            }
        }
    }

    /**
     * Mute the microphone before answering so there is no unmuted gap once the call connects.
     * Muting is global, sticky state (MODIFY_AUDIO_SETTINGS), so the caller of this method must
     * restore it when this returns true, and we leave it alone when it is not ours to manage.
     *
     * @return Whether the microphone was muted by us and must be restored.
     */
    private static boolean muteMicrophone(@Nullable AudioManager audioManager, MuteGuard muteGuard) {
        if (audioManager == null) {
            return false;
        }

        try {
            // A call is already active (this one is call waiting); muting now would silence the
            // user's live conversation while we deal with the new call.
            if (audioManager.getMode() == AudioManager.MODE_IN_CALL) {
                return false;
            }

            // Already muted by the user or another app; leave it that way.
            if (audioManager.isMicrophoneMute()) {
                return false;
            }

            // Persist the marker before touching the mute so a kill during the connected window is
            // recoverable on the next call; drop it again if the mute itself fails.
            muteGuard.arm();
            audioManager.setMicrophoneMute(true);
            return true;
        } catch (Exception e) {
            // Some OEMs reject non-dialer mic muting; the answer dance must still proceed.
            muteGuard.disarm();
            if (BuildConfig.DEBUG) {
                Log.w(TAG, "Failed to mute microphone", e);
            }
            return false;
        }
    }

    private static void unmuteMicrophone(AudioManager audioManager, MuteGuard muteGuard) {
        try {
            audioManager.setMicrophoneMute(false);
            // Only clear the marker once the mute is actually gone; if this throws, leave it set so
            // the next call's recovery retries instead of stranding the user muted.
            muteGuard.disarm();
        } catch (Exception e) {
            if (BuildConfig.DEBUG) {
                Log.w(TAG, "Failed to restore microphone", e);
            }
        }
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
