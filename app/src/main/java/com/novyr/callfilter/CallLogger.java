package com.novyr.callfilter;

import android.content.Context;
import android.content.Intent;

import com.novyr.callfilter.activities.LogViewerActivity;
import com.novyr.callfilter.models.LogEntry;

import java.util.Date;

class CallLogger {
    private final Intent mBroadcastRefresh;

    public enum Action {
        ALLOWED, BLOCKED, FAILED
    }

    CallLogger() {
        mBroadcastRefresh = new Intent(LogViewerActivity.BROADCAST_REFRESH);
    }

    void record(Context context, Action action, String number) {
        LogEntry log = new LogEntry(new Date(), action.toString().toLowerCase(), number);
        log.save();

        context.sendBroadcast(mBroadcastRefresh);
    }
}
