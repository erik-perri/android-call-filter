package com.novyr.callfilter.managers;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v4.content.ContextCompat;
import android.telecom.TelecomManager;
import android.util.Log;

import java.lang.reflect.Method;

public class TelephonyManager {
    private static final String TAG = TelephonyManager.class.getName();
    private Context mContext;
    private Object mInterfaceTelephony = null;
    private Method mMethodSilenceRinger = null;
    private Method mMethodEndCall = null;

    public TelephonyManager(Context context) {
        try {
            android.telephony.TelephonyManager manager = (android.telephony.TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            if (manager == null) {
                throw new Exception("Failed to get TelephonyManager system service.");
            }

            @SuppressLint("PrivateApi") Method methodGetInterface = manager.getClass().getDeclaredMethod("getITelephony");
            methodGetInterface.setAccessible(true);

            mContext = context;
            mInterfaceTelephony = methodGetInterface.invoke(manager);
            mMethodEndCall = mInterfaceTelephony.getClass().getDeclaredMethod("endCall");
            mMethodSilenceRinger = mInterfaceTelephony.getClass().getDeclaredMethod("silenceRinger");
        } catch (Exception e) {
            Log.d(TAG, "Failed to find telephony interface or methods", e);
        }
    }

    public boolean endCall() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                return endCallP();
            }

            return endCallLegacy();
        } catch (Exception e) {
            Log.d(TAG, "Failed to silence and end call", e);
        }
        return false;
    }

    private boolean endCallLegacy() throws Exception {
        if (mInterfaceTelephony == null || mMethodEndCall == null) {
            return false;
        }

        if (mMethodSilenceRinger != null) {
            mMethodSilenceRinger.invoke(mInterfaceTelephony);
        }

        return (Boolean) mMethodEndCall.invoke(mInterfaceTelephony);
    }

    // https://stackoverflow.com/a/51121175
    @RequiresApi(api = Build.VERSION_CODES.P)
    private boolean endCallP() {
        final TelecomManager telecomManager = (TelecomManager) mContext.getSystemService(Context.TELECOM_SERVICE);
        if (telecomManager != null && ContextCompat.checkSelfPermission(mContext, Manifest.permission.ANSWER_PHONE_CALLS) == PackageManager.PERMISSION_GRANTED) {
            telecomManager.endCall();
            return true;
        }

        return false;
    }

}
