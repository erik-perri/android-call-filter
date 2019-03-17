package com.novyr.callfilter.managers.telephony;

import android.annotation.SuppressLint;
import android.content.Context;
import android.telephony.TelephonyManager;
import android.util.Log;

import java.lang.reflect.Method;

public class AndroidLegacyHandler implements HandlerInterface {
    private static final String TAG = AndroidLegacyHandler.class.getName();

    private Object mInterfaceTelephony = null;
    private Method mMethodSilenceRinger = null;
    private Method mMethodEndCall = null;

    public AndroidLegacyHandler(Context context) {
        try {
            TelephonyManager manager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            if (manager == null) {
                throw new Exception("Failed to get TelephonyManager system service.");
            }

            @SuppressLint("PrivateApi")
            Method methodGetInterface = manager.getClass().getDeclaredMethod("getITelephony");
            methodGetInterface.setAccessible(true);

            mInterfaceTelephony = methodGetInterface.invoke(manager);
            if (mInterfaceTelephony != null) {
                mMethodEndCall = mInterfaceTelephony.getClass().getDeclaredMethod("endCall");
                mMethodSilenceRinger = mInterfaceTelephony.getClass().getDeclaredMethod("silenceRinger");
            }
        } catch (Exception e) {
            Log.d(TAG, "Failed to find telephony interface or methods", e);
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
            Log.d(TAG, "Failed to silence and end call", e);
        }
        return result;
    }
}
