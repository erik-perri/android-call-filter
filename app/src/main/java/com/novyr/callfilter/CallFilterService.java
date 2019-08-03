package com.novyr.callfilter;

import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.telecom.Call;
import android.telecom.CallScreeningService;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import com.novyr.callfilter.db.LogRepository;
import com.novyr.callfilter.db.entity.LogEntity;
import com.novyr.callfilter.db.entity.enums.Action;

import java.util.Date;

@RequiresApi(api = Build.VERSION_CODES.Q)
public class CallFilterService extends CallScreeningService {
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

        Uri handle = details.getHandle();
        String number = null;
        if (handle != null) {
            String scheme = handle.getScheme();
            if (scheme != null && scheme.equals("tel")) {
                number = handle.getSchemeSpecificPart();
            }
        }

        new ScreenAsyncTask().execute(new ScreenTaskParams(getApplicationContext(), this, details, number));
    }

    private static class ScreenAsyncTask extends AsyncTask<ScreenTaskParams, Void, Void> {
        @Override
        protected Void doInBackground(ScreenTaskParams... params) {
            ScreenTaskParams taskParams = params[0];
            if (taskParams == null) {
                return null;
            }

            CallResponse.Builder response = new CallResponse.Builder();
            response.setDisallowCall(false);
            response.setRejectCall(false);
            response.setSkipCallLog(false);
            response.setSkipNotification(false);

            CallChecker checker = new CallChecker(taskParams.context);
            Action action = Action.ALLOWED;

            if (checker.shouldBlockCall(taskParams.number)) {
                action = Action.BLOCKED;
                response.setDisallowCall(true);
                response.setRejectCall(true);
                response.setSkipNotification(true);

                // TODO Doesn't work?
                response.setSkipCallLog(false);
            }

            LogRepository repository = ((CallFilterApplication) taskParams.context.getApplicationContext()).getLogRepository();
            repository.insert(new LogEntity(new Date(), action, taskParams.number));

            taskParams.service.respondToCall(taskParams.details, response.build());
            return null;
        }
    }

    private class ScreenTaskParams {
        @NonNull
        final Context context;

        @NonNull
        final CallFilterService service;

        @NonNull
        final Call.Details details;

        final String number;

        ScreenTaskParams(
                @NonNull Context context,
                @NonNull CallFilterService service,
                @NonNull Call.Details details,
                String number
        ) {
            this.context = context;
            this.service = service;
            this.details = details;
            this.number = number;
        }

    }
}
