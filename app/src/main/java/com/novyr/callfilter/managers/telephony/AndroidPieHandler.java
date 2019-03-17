package com.novyr.callfilter.managers.telephony;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v4.content.ContextCompat;
import android.telecom.TelecomManager;

@RequiresApi(api = Build.VERSION_CODES.P)
public class AndroidPieHandler implements HandlerInterface {
    private static final String TAG = AndroidPieHandler.class.getName();

    private TelecomManager mTelecomManager = null;

    public AndroidPieHandler(Context context) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ANSWER_PHONE_CALLS) == PackageManager.PERMISSION_GRANTED) {
            mTelecomManager = (TelecomManager) context.getSystemService(Context.TELECOM_SERVICE);
        }
    }

    @SuppressLint("MissingPermission")
    public boolean endCall() {
        return mTelecomManager != null && mTelecomManager.endCall();
    }
}
