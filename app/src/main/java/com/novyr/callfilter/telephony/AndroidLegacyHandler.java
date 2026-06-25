package com.novyr.callfilter.telephony;

import android.annotation.SuppressLint;
import android.content.Context;
import android.media.AudioManager;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.novyr.callfilter.BuildConfig;

import java.lang.reflect.Method;

public class AndroidLegacyHandler implements HandlerInterface {
    private static final String TAG = AndroidLegacyHandler.class.getSimpleName();

    private final Context mContext;
    private TelephonyManager mTelephonyManager = null;
    private AudioManager mAudioManager = null;
    private Object mInterfaceTelephony = null;
    private Method mMethodSilenceRinger = null;
    private Method mMethodEndCall = null;
    private Method mMethodAnswerRingingCall = null;

    AndroidLegacyHandler(Context context) {
        mContext = context.getApplicationContext();
        mAudioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);

        try {
            TelephonyManager manager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            if (manager == null) {
                throw new Exception("Failed to get TelephonyManager system service.");
            }
            mTelephonyManager = manager;

            @SuppressLint({"PrivateApi", "SoonBlockedPrivateApi"})
            Method methodGetInterface = manager.getClass().getDeclaredMethod("getITelephony");
            methodGetInterface.setAccessible(true);

            mInterfaceTelephony = methodGetInterface.invoke(manager);
            if (mInterfaceTelephony != null) {
                mMethodEndCall = mInterfaceTelephony.getClass().getDeclaredMethod("endCall");
                mMethodSilenceRinger = mInterfaceTelephony.getClass()
                                                          .getDeclaredMethod("silenceRinger");

                try {
                    mMethodAnswerRingingCall = mInterfaceTelephony.getClass()
                                                                  .getDeclaredMethod("answerRingingCall");
                } catch (NoSuchMethodException e) {
                    // Some OEM builds may not expose it; answerAndEnd degrades to a plain end.
                }
            }
        } catch (Exception e) {
            if (BuildConfig.DEBUG) {
                Log.e(TAG, "Failed to find telephony interface or methods", e);
            }
        }
    }

    public boolean endCall() {
        boolean result = false;
        try {
            if (mInterfaceTelephony == null || mMethodEndCall == null) {
                return false;
            }

            if (mMethodSilenceRinger != null) {
                mMethodSilenceRinger.invoke(mInterfaceTelephony);
            }

            result = (boolean) mMethodEndCall.invoke(mInterfaceTelephony);
        } catch (Exception e) {
            if (BuildConfig.DEBUG) {
                Log.e(TAG, "Failed to silence and end call", e);
            }
        }
        return result;
    }

    @Override
    public AnswerAndEndResult answerAndEnd() {
        if (mInterfaceTelephony == null || mMethodEndCall == null) {
            return AnswerAndEndResult.FAILED;
        }

        if (mMethodAnswerRingingCall == null) {
            // No way to answer on this device; at least stop the ringing.
            return endCall() ? AnswerAndEndResult.ENDED_WITHOUT_ANSWER : AnswerAndEndResult.FAILED;
        }

        return AnswerAndEndSequence.run(mAudioManager, new AnswerAndEndSequence.Telephony() {
            @Override
            public void attemptAnswer() throws Exception {
                mMethodAnswerRingingCall.invoke(mInterfaceTelephony);
            }

            @Override
            public boolean endCall() {
                return AndroidLegacyHandler.this.endCall();
            }

            @Override
            public int getCallState() {
                return mTelephonyManager != null
                        ? mTelephonyManager.getCallState()
                        : TelephonyManager.CALL_STATE_IDLE;
            }
        }, MicMuteGuard.guardFor(mContext));
    }
}
