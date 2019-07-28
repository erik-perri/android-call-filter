package com.novyr.callfilter;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;

import com.novyr.callfilter.db.entity.LogEntity;
import com.novyr.callfilter.db.entity.enums.Action;
import com.novyr.callfilter.managers.TelephonyManager;
import com.novyr.callfilter.managers.telephony.HandlerInterface;

import java.util.Date;

import androidx.annotation.NonNull;

public class CallReceiver extends BroadcastReceiver {
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

        //noinspection deprecation
        String number = intent.getStringExtra(android.telephony.TelephonyManager.EXTRA_INCOMING_NUMBER);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P && number == null) {
            // Since we request both READ_CALL_LOG and READ_PHONE_STATE permissions our onReceive
            // will get called twice, the first has a null phone number.
            // https://developer.android.com/reference/android/telephony/TelephonyManager#ACTION_PHONE_STATE_CHANGED
            return;
        }

        new ReceiveAsyncTask().execute(new ReceiveTaskParams(context, number));
    }

    private static class ReceiveAsyncTask extends AsyncTask<ReceiveTaskParams, Void, Void> {
        @Override
        protected Void doInBackground(ReceiveTaskParams... params) {
            ReceiveTaskParams taskParams = params[0];
            if (taskParams == null) {
                return null;
            }

            HandlerInterface handler = TelephonyManager.findHandler(taskParams.context);
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
