package com.novyr.callfilter;

import android.net.Uri;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.telecom.Call;
import android.telecom.CallScreeningService;

@RequiresApi(api = Build.VERSION_CODES.Q)
public class CallFilterService extends CallScreeningService {
    @Override
    public void onScreenCall(@SuppressWarnings("NullableProblems") Call.Details details) {
        CallResponse.Builder response = new CallResponse.Builder();
        response.setDisallowCall(false);
        response.setRejectCall(false);
        response.setSkipCallLog(false);
        response.setSkipNotification(false);

        Uri handle = details.getHandle();
        String number = null;
        if (handle != null) {
            String scheme = handle.getScheme();
            if (scheme != null && scheme.equals("tel")) {
                number = handle.getSchemeSpecificPart();
            }
        }

        CallLogger.Action action = CallLogger.Action.ALLOWED;
        if (CallFilterApplication.shouldBlockCall(getApplicationContext(), number)) {
            action = CallLogger.Action.BLOCKED;
            response.setDisallowCall(true);
            response.setRejectCall(true);
            response.setSkipNotification(true);

            // TODO Doesn't work?
            response.setSkipCallLog(false);
        }

        respondToCall(details, response.build());

        CallLogger recorder = new CallLogger();
        recorder.record(getApplicationContext(), action, number);
    }
}
