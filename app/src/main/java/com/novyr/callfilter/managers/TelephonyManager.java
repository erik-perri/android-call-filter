package com.novyr.callfilter.managers;

import android.content.Context;
import android.os.Build;

import com.novyr.callfilter.managers.telephony.AndroidLegacyHandler;
import com.novyr.callfilter.managers.telephony.AndroidPieHandler;
import com.novyr.callfilter.managers.telephony.HandlerInterface;

public class TelephonyManager {
    private static final String TAG = TelephonyManager.class.getName();

    public static HandlerInterface findHandler(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            return new AndroidPieHandler(context);
        }

        return new AndroidLegacyHandler(context);
    }
}
