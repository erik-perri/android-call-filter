package com.novyr.callfilter.telephony;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.telecom.TelecomManager;
import android.telephony.TelephonyManager;

import androidx.annotation.RequiresApi;
import androidx.core.content.ContextCompat;

@RequiresApi(api = Build.VERSION_CODES.P)
public class AndroidPieHandler implements HandlerInterface {
    private TelecomManager mTelecomManager = null;
    private TelephonyManager mTelephonyManager = null;

    AndroidPieHandler(Context context) {
        if (ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ANSWER_PHONE_CALLS
        ) == PackageManager.PERMISSION_GRANTED) {
            mTelecomManager = (TelecomManager) context.getSystemService(Context.TELECOM_SERVICE);
            mTelephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        }
    }

    @SuppressLint("MissingPermission")
    public boolean endCall() {
        return mTelecomManager != null && mTelecomManager.endCall();
    }

    @Override
    public AnswerAndEndResult answerAndEnd() {
        if (mTelecomManager == null) {
            return AnswerAndEndResult.FAILED;
        }

        return AnswerAndEndSequence.run(new AnswerAndEndSequence.Telephony() {
            @SuppressLint("MissingPermission")
            @Override
            public void attemptAnswer() {
                mTelecomManager.acceptRingingCall();
            }

            @Override
            public boolean endCall() {
                return AndroidPieHandler.this.endCall();
            }

            @SuppressWarnings("deprecation")
            @Override
            public int getCallState() {
                // Deprecated in API 31 but still the simplest cross-version connect signal. It
                // needs READ_PHONE_STATE there, which the hang-up capability guarantees before
                // this handler is asked to answer.
                return mTelephonyManager != null
                        ? mTelephonyManager.getCallState()
                        : TelephonyManager.CALL_STATE_IDLE;
            }
        });
    }
}
