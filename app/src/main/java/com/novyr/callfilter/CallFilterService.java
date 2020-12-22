package com.novyr.callfilter;

import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.telecom.Call;
import android.telecom.CallScreeningService;
import android.telecom.Connection;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import com.novyr.callfilter.db.LogRepository;
import com.novyr.callfilter.db.entity.LogEntity;
import com.novyr.callfilter.db.entity.enums.LogAction;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

@RequiresApi(api = Build.VERSION_CODES.Q)
public class CallFilterService extends CallScreeningService {
    private static final String TAG = CallFilterService.class.getSimpleName();
    private final Executor executor = Executors.newSingleThreadExecutor();

    @Override
    public void onScreenCall(@NonNull Call.Details details) {
        CallResponse.Builder response = new CallResponse.Builder();
        response.setDisallowCall(false);
        response.setRejectCall(false);
        response.setSkipCallLog(false);
        response.setSkipNotification(false);

        if (details.getCallDirection() != Call.Details.DIRECTION_INCOMING) {
            respondToCall(details, response.build());
            return;
        }

        Context context = getApplicationContext();
        String number = getNumberFromDetails(details);

        executor.execute(() -> {
            RuleChecker checker = RuleCheckerFactory.create(context);
            LogAction action = LogAction.ALLOWED;

            int verificationStatus = Connection.VERIFICATION_STATUS_NOT_VERIFIED;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                verificationStatus = details.getCallerNumberVerificationStatus();
            }

            if (!checker.allowCall(new CallDetails(number, verificationStatus))) {
                action = LogAction.BLOCKED;
                response.setDisallowCall(true);
                response.setRejectCall(true);
                response.setSkipNotification(true);

                // TODO Figure out why this doesn't seem to work with Google's phone app in the
                //      emulator. It works as expected with Samsung's dialer on a real phone.
                response.setSkipCallLog(false);
            }

            LogRepository repository = ((CallFilterApplication) context.getApplicationContext()).getLogRepository();
            repository.insert(new LogEntity(action, number));

            respondToCall(details, response.build());
        });
    }

    private String getNumberFromDetails(@NonNull Call.Details details) {
        Uri handle = details.getHandle();
        if (handle == null) {
            Log.e(TAG, "No handle on incoming call");
            return null;
        }

        String scheme = handle.getScheme();
        if (scheme != null && scheme.equals("tel")) {
            return handle.getSchemeSpecificPart();
        }

        Log.e(TAG, "Unhandled scheme");
        return null;
    }
}
