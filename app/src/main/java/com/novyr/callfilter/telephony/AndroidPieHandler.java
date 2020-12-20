package com.novyr.callfilter.telephony;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.telecom.TelecomManager;

import androidx.annotation.RequiresApi;
import androidx.core.content.ContextCompat;

@RequiresApi(api = Build.VERSION_CODES.P)
public class AndroidPieHandler implements HandlerInterface {
    private TelecomManager mTelecomManager = null;

    AndroidPieHandler(Context context) {
        if (ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ANSWER_PHONE_CALLS
        ) == PackageManager.PERMISSION_GRANTED) {
            mTelecomManager = (TelecomManager) context.getSystemService(Context.TELECOM_SERVICE);
        }
    }

    @SuppressLint("MissingPermission")
    public boolean endCall() {
        return mTelecomManager != null && mTelecomManager.endCall();
    }
}
