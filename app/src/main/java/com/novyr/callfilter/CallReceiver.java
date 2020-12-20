package com.novyr.callfilter;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import com.novyr.callfilter.db.entity.LogEntity;
import com.novyr.callfilter.db.entity.enums.LogAction;
import com.novyr.callfilter.telephony.HandlerFactory;
import com.novyr.callfilter.telephony.HandlerInterface;

import java.util.Date;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class CallReceiver extends BroadcastReceiver {
    private static final String TAG = CallReceiver.class.getSimpleName();
    private final Executor executor = Executors.newSingleThreadExecutor();

    @Override
    public void onReceive(Context context, Intent intent) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
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

        if (BuildConfig.DEBUG) {
            Log.i(TAG, "Call received");
            Bundle bundle = intent.getExtras();
            if (bundle != null) {
                Log.d(TAG, " - Intent extras:");
                for (String key : bundle.keySet()) {
                    Object value = bundle.get(key);
                    Log.d(TAG, String.format(
                            "   - %-16s %-16s (%s)",
                            key,
                            value != null ? value.toString() : "NULL",
                            value != null ? value.getClass().getName() : ""
                    ));
                }
            }
        }

        if (!shouldHandleCall(intent)) {
            if (BuildConfig.DEBUG) {
                Log.i(TAG, " - Skipping call");
            }
            return;
        }

        if (BuildConfig.DEBUG) {
            Log.i(TAG, " - Handling call");
        }

        // noinspection deprecation
        String number = intent.getStringExtra(android.telephony.TelephonyManager.EXTRA_INCOMING_NUMBER);

        executor.execute(() -> {
            HandlerInterface handler = HandlerFactory.create(context);
            RuleChecker checker = RuleCheckerFactory.create(context);

            LogAction action = LogAction.ALLOWED;
            if (!checker.allowCall(number)) {
                if (handler.endCall()) {
                    action = LogAction.BLOCKED;
                } else {
                    action = LogAction.FAILED;
                }
            }

            CallFilterApplication application = (CallFilterApplication) context.getApplicationContext();
            application.getLogRepository().insert(new LogEntity(new Date(), action, number));
        });
    }

    private boolean shouldHandleCall(Intent intent) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // Since we request both READ_CALL_LOG and READ_PHONE_STATE permissions we will get called twice, one of
            // the calls missing the EXTRA_INCOMING_NUMBER data.
            // https://developer.android.com/reference/android/telephony/TelephonyManager#ACTION_PHONE_STATE_CHANGED
            // noinspection deprecation
            return intent.hasExtra(android.telephony.TelephonyManager.EXTRA_INCOMING_NUMBER);
        }

        // In Lollipop (API v21 and v22) we get called twice.  The first seems to always have a subscription value of 1,
        // in the emulator at least.  If we are on Kitkat or below we can continue without checking.
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.KITKAT) {
            return true;
        }

        Bundle bundle = intent.getExtras();
        Object value = bundle != null ? bundle.get("subscription") : null;
        if (value == null) {
            return true;
        }

        String expectedId = "1";
        String id = value.toString();

        return id == null || id.equals(expectedId);
    }
}
