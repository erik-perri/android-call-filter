package com.novyr.callfilter;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import com.novyr.callfilter.managers.TelephonyManager;
import com.novyr.callfilter.models.LogEntry;

public class CallReceiver extends BroadcastReceiver {
    public static final String BROADCAST_REFRESH = "com.novyr.callfilter.refresh";
    private static final String TAG = CallReceiver.class.getName();
    private final Intent mBroadcastRefresh;

    public CallReceiver() {
        super();

        mBroadcastRefresh = new Intent(BROADCAST_REFRESH);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String intentAction = intent.getAction();
        if (intentAction == null || !intentAction.equals("android.intent.action.PHONE_STATE")) {
            return;
        }

        String state = intent.getStringExtra(android.telephony.TelephonyManager.EXTRA_STATE);

        if (state.equals(android.telephony.TelephonyManager.EXTRA_STATE_RINGING)) {
            String number = intent.getStringExtra(android.telephony.TelephonyManager.EXTRA_INCOMING_NUMBER);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && number == null) {
                return;
            }

            TelephonyManager manager = new TelephonyManager(context);
            String action = "allowed";
            if (CallFilterApplication.shouldBlockCall(context, number)) {
                if (manager.silenceAndEndCall()) {
                    action = "blocked";
                } else {
                    action = "failed";
                }
            }

            LogEntry log = new LogEntry(action, number);
            log.save();

            context.sendBroadcast(mBroadcastRefresh);
        }
    }
}
