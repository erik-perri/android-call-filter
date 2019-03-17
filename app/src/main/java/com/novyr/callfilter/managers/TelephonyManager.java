package com.novyr.callfilter.managers;

import android.annotation.SuppressLint;
import android.content.Context;
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
            if (mInterfaceTelephony == null || mMethodEndCall == null) {
                return false;
            }

            if (mMethodSilenceRinger != null) {
                mMethodSilenceRinger.invoke(mInterfaceTelephony);
            }

            return (Boolean) mMethodEndCall.invoke(mInterfaceTelephony);
        } catch (Exception e) {
            Log.d(TAG, "Failed to call endCall method", e);
        }
        return false;
    }
}
