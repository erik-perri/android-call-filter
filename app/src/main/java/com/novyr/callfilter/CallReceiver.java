package com.novyr.callfilter;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.novyr.callfilter.db.entity.LogEntity;
import com.novyr.callfilter.db.entity.enums.LogAction;
import com.novyr.callfilter.telephony.AnswerAndEndResult;
import com.novyr.callfilter.telephony.HandlerFactory;
import com.novyr.callfilter.telephony.HandlerInterface;
import com.novyr.callfilter.telephony.PendingAnswerStore;

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
        String intentAction = intent.getAction();
        if (intentAction == null || !intentAction.equals(TelephonyManager.ACTION_PHONE_STATE_CHANGED)) {
            return;
        }

        // On API 29+ rule evaluation lives in CallFilterService (a CallScreeningService). The only
        // job left for this receiver there is the answer-then-hang-up handoff, since a screening
        // service cannot answer a call itself.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            handleAnswerHandoff(context, intent);
            return;
        }

        if (!shouldHandleCall(intent)) {
            return;
        }

        // noinspection deprecation
        final String number = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER);

        // goAsync keeps the process alive while we work; the answer-then-hang-up dance can take
        // several seconds, well past the return from onReceive.
        final PendingResult pendingResult = goAsync();
        executor.execute(() -> {
            try {
                HandlerInterface handler = HandlerFactory.create(context);
                RuleChecker checker = RuleCheckerFactory.create(context);

                CallDecision decision = checker.checkAction(new CallDetails(number));
                LogAction action = performAction(decision, handler);

                CallFilterApplication application = (CallFilterApplication) context.getApplicationContext();
                application.getLogRepository().insert(new LogEntity(action, number));
            } finally {
                pendingResult.finish();
            }
        });
    }

    /**
     * On API 29+, CallFilterService marks calls that should be answered-then-ended via
     * {@link PendingAnswerStore}. When such a call reaches the ringing state we claim the mark and
     * perform the answer + hang up. Claiming is atomic, so duplicate RINGING broadcasts cannot
     * answer twice; {@code goAsync()} keeps the process alive across the answer-hold-end window.
     */
    private void handleAnswerHandoff(Context context, Intent intent) {
        String state = intent.getStringExtra(TelephonyManager.EXTRA_STATE);
        if (!TelephonyManager.EXTRA_STATE_RINGING.equals(state)) {
            return;
        }

        // noinspection deprecation
        String incomingNumber = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER);
        PendingAnswerStore.Claim claim = PendingAnswerStore.claim(incomingNumber);
        if (claim == null) {
            return;
        }

        final PendingResult pendingResult = goAsync();
        executor.execute(() -> {
            try {
                HandlerInterface handler = HandlerFactory.create(context);
                LogAction action = toLogAction(handler.answerAndEnd());

                CallFilterApplication application = (CallFilterApplication) context.getApplicationContext();
                application.getLogRepository().insert(new LogEntity(action, claim.getNumber()));
            } finally {
                pendingResult.finish();
            }
        });
    }

    private LogAction performAction(CallDecision decision, HandlerInterface handler) {
        switch (decision.getAction()) {
            case BLOCK:
                if (!handler.endCall()) {
                    return LogAction.FAILED;
                }
                return decision.fellBackToBlock()
                        ? LogAction.FELL_BACK_TO_BLOCK
                        : LogAction.BLOCKED;
            case ANSWER_AND_END:
                return toLogAction(handler.answerAndEnd());
            default:
                return LogAction.ALLOWED;
        }
    }

    private static LogAction toLogAction(AnswerAndEndResult result) {
        switch (result) {
            case ANSWERED_AND_ENDED:
                return LogAction.ENDED_NO_VOICEMAIL;
            case ENDED_WITHOUT_ANSWER:
                return LogAction.FELL_BACK_TO_BLOCK;
            default:
                return LogAction.FAILED;
        }
    }

    public static void resetState() {
        sLastHandledTimeMs = 0;
        sLastHandledNumber = null;
        PendingAnswerStore.reset();
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
