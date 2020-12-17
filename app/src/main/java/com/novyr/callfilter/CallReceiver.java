package com.novyr.callfilter;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;

import com.novyr.callfilter.db.entity.LogEntity;
import com.novyr.callfilter.db.entity.enums.Action;
import com.novyr.callfilter.telephony.HandlerFactory;
import com.novyr.callfilter.telephony.HandlerInterface;

import java.util.Date;

public class CallReceiver extends BroadcastReceiver {
    private static final String TAG = CallReceiver.class.getSimpleName();

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
        new ReceiveAsyncTask().execute(new ReceiveTaskParams(
                context,
                intent.getStringExtra(android.telephony.TelephonyManager.EXTRA_INCOMING_NUMBER)
        ));
    }

    private boolean shouldHandleCall(Intent intent) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // Since we request both READ_CALL_LOG and READ_PHONE_STATE permissions we will get called twice, one of
            // the calls missing the EXTRA_INCOMING_NUMBER data.
            // https://developer.android.com/reference/android/telephony/TelephonyManager#ACTION_PHONE_STATE_CHANGED
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

    private static class ReceiveAsyncTask extends AsyncTask<ReceiveTaskParams, Void, Void> {
        @Override
        protected Void doInBackground(ReceiveTaskParams... params) {
            ReceiveTaskParams taskParams = params[0];
            if (taskParams == null) {
                return null;
            }

            HandlerInterface handler = HandlerFactory.create(taskParams.context);
            CallChecker checker = new CallChecker(taskParams.context);
            Action action = Action.ALLOWED;
            if (checker.shouldBlockCall(taskParams.number)) {
                if (handler.endCall()) {
                    action = Action.BLOCKED;
                } else {
                    action = Action.FAILED;
                }
            }

            CallFilterApplication application = (CallFilterApplication) taskParams.context.getApplicationContext();
            application.getLogRepository().insert(new LogEntity(new Date(), action, taskParams.number));

            return null;
        }
    }

    private class ReceiveTaskParams {
        @NonNull
        final Context context;

        final String number;

        ReceiveTaskParams(@NonNull Context context, String number) {
            this.context = context;
            this.number = number;
        }
    }
}
