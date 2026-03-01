package com.novyr.callfilter;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.novyr.callfilter.db.entity.LogEntity;
import com.novyr.callfilter.db.entity.enums.LogAction;
import com.novyr.callfilter.telephony.HandlerFactory;
import com.novyr.callfilter.telephony.HandlerInterface;

import java.util.Objects;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class CallReceiver extends BroadcastReceiver {
    private static final String TAG = CallReceiver.class.getSimpleName();

    /**
     * Time window to prevent processing the same call multiple times.
     * In some cases Android sends duplicate broadcasts for a single ringing event.
     */
    private static final long DUPLICATE_WINDOW_MS = 2000;
    private static long sLastHandledTimeMs = 0;
    private static String sLastHandledNumber = null;

    /**
     * Offload database and telephony operations to a background thread.
     * Static to ensure the thread pool persists for the duration of the process,
     * even if this specific Receiver instance is short-lived.
     */
    private static final Executor executor = Executors.newSingleThreadExecutor();

    @Override
    public void onReceive(Context context, Intent intent) {
        // API 29+ is handled by CallScreeningService
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            return;
        }

        String intentAction = intent.getAction();
        if (intentAction == null || !intentAction.equals(TelephonyManager.ACTION_PHONE_STATE_CHANGED)) {
            return;
        }

        if (!shouldHandleCall(intent)) {
            return;
        }

        // noinspection deprecation
        final String number = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER);

        executor.execute(() -> {
            HandlerInterface handler = HandlerFactory.create(context);
            RuleChecker checker = RuleCheckerFactory.create(context);

            LogAction action = LogAction.ALLOWED;
            if (!checker.allowCall(new CallDetails(number))) {
                // If the rule says block, attempt to end the call and log the result.
                action = handler.endCall()
                        ? LogAction.BLOCKED
                        : LogAction.FAILED;
            }

            CallFilterApplication application = (CallFilterApplication) context.getApplicationContext();
            application.getLogRepository().insert(new LogEntity(action, number));
        });
    }

    public static void resetState() {
        sLastHandledTimeMs = 0;
        sLastHandledNumber = null;
    }

    private boolean shouldHandleCall(Intent intent) {
        String state = intent.getStringExtra(TelephonyManager.EXTRA_STATE);
        if (!TelephonyManager.EXTRA_STATE_RINGING.equals(state)) {
            return false;
        }

        // On Android 9 (P), the system sends two broadcasts if READ_CALL_LOG is granted:
        // 1. One without the phone number (via READ_PHONE_STATE permission).
        // 2. One with the phone number (via READ_CALL_LOG permission).
        // We ignore the numberless broadcast to ensure the RuleChecker has data to work with.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            if (!intent.hasExtra(TelephonyManager.EXTRA_INCOMING_NUMBER)) {
                if (BuildConfig.DEBUG) {
                    Log.d(TAG, "Skipping restricted API 28 broadcast (no number)");
                }
                return false;
            }
        }

        // noinspection deprecation
        String number = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER);

        return !isDuplicateBroadcast(number);
    }
    private synchronized boolean isDuplicateBroadcast(String number) {
        long now = System.currentTimeMillis();

        if (now - sLastHandledTimeMs > DUPLICATE_WINDOW_MS) {
            sLastHandledTimeMs = now;
            sLastHandledNumber = number;
            return false;
        }

        // If previous broadcast was restricted (null number) but this one has it,
        // treat this as an update/enrichment rather than a duplicate.
        if (sLastHandledNumber == null && number != null) {
            sLastHandledTimeMs = now;
            sLastHandledNumber = number;
            return false;
        }

        // Receiving the same number within the time window is a duplicate.
        if (Objects.equals(sLastHandledNumber, number)) {
            return true;
        }

        sLastHandledTimeMs = now;
        sLastHandledNumber = number;
        return false;
    }
}
