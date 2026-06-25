package com.novyr.callfilter.telephony;

import android.content.Context;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.util.Log;

import com.novyr.callfilter.BuildConfig;

/**
 * Guards against leaking the process-global, sticky microphone mute that {@link AnswerAndEndSequence}
 * sets while a screened call is briefly connected.
 *
 * That mute is normally restored in the sequence's {@code finally}, but that code never runs if
 * the process is killed during the connected window. The low-memory killer and force-stop both
 * deliver {@code SIGKILL}, which has no interceptable callback, so the mute cannot be undone at death
 * time. Left set, the device stays muted into the user's next real call and does not self-heal,
 * because the mute logic short-circuits when the mic already reads as muted. So before muting we
 * persist a marker (synchronously, since the process may die imminently) and clear it once the mute
 * is restored.
 */
public final class MicMuteGuard {
    private static final String TAG = MicMuteGuard.class.getSimpleName();
    private static final String PREFS = "mic_mute_guard";
    private static final String KEY_MUTED = "muted_by_us";

    private MicMuteGuard() {
    }

    static void arm(Context context) {
        // We commit instead of apply since the mute follows immediately and the process may be killed
        // before an asynchronous write reaches disk, which would leave a real mute with no marker to 
        // undo it.
        prefs(context).edit().putBoolean(KEY_MUTED, true).commit();
    }

    static void disarm(Context context) {
        prefs(context).edit().remove(KEY_MUTED).apply();
    }

    /**
     * Build a {@link AnswerAndEndSequence.MuteGuard} bound to this context so the sequence can arm
     * and disarm the marker without depending on Android types directly (keeps it unit-testable).
     */
    static AnswerAndEndSequence.MuteGuard guardFor(Context context) {
        final Context appContext = context.getApplicationContext();
        return new AnswerAndEndSequence.MuteGuard() {
            @Override
            public void arm() {
                MicMuteGuard.arm(appContext);
            }

            @Override
            public void disarm() {
                MicMuteGuard.disarm(appContext);
            }
        };
    }

    /**
     * If a previous process left the mic muted (its marker survived), force-clear the mute.
     */
    public static void recoverIfLeaked(Context context) {
        if (!prefs(context).getBoolean(KEY_MUTED, false)) {
            return;
        }

        AudioManager audioManager = (AudioManager) context.getApplicationContext()
                .getSystemService(Context.AUDIO_SERVICE);
        if (audioManager == null) {
            return;
        }

        try {
            audioManager.setMicrophoneMute(false);
            disarm(context);
        } catch (Exception e) {
            if (BuildConfig.DEBUG) {
                Log.w(TAG, "Failed to restore leaked microphone mute", e);
            }
        }
    }

    private static SharedPreferences prefs(Context context) {
        return context.getApplicationContext().getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }
}
