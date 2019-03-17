package com.novyr.callfilter;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import com.novyr.callfilter.managers.TelephonyManager;
import com.novyr.callfilter.managers.telephony.HandlerInterface;

public class CallReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (Build.VERSION.SDK_INT >= CallFilterApplication.Q) {
            // If we are on Q+ we use the CallScreeningService API instead
            return;
        }

        String intentAction = intent.getAction();
        if (intentAction == null || !intentAction.equals("android.intent.action.PHONE_STATE")) {
            return;
        }

        String state = intent.getStringExtra(android.telephony.TelephonyManager.EXTRA_STATE);
        if (state == null || !state.equals(android.telephony.TelephonyManager.EXTRA_STATE_RINGING)) {
            return;
        }

        String number = intent.getStringExtra(android.telephony.TelephonyManager.EXTRA_INCOMING_NUMBER);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P && number == null) {
            // Since we request both READ_CALL_LOG and READ_PHONE_STATE permissions our onReceive
            // will get called twice, the first has a null phone number.
            // https://developer.android.com/reference/android/telephony/TelephonyManager#ACTION_PHONE_STATE_CHANGED
            return;
        }

        HandlerInterface handler = TelephonyManager.findHandler(context);
        CallLogger.Action action = CallLogger.Action.ALLOWED;
        if (CallFilterApplication.shouldBlockCall(context, number)) {
            if (handler.endCall()) {
                action = CallLogger.Action.BLOCKED;
            } else {
                action = CallLogger.Action.FAILED;
            }
        }

        CallLogger recorder = new CallLogger();
        recorder.record(context, action, number);
    }
}
