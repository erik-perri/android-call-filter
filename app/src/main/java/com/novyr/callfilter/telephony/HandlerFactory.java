package com.novyr.callfilter.telephony;

import android.content.Context;
import android.os.Build;

public class HandlerFactory {
    public static HandlerInterface create(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            return new AndroidPieHandler(context);
        }

        return new AndroidLegacyHandler(context);
    }
}
