package com.novyr.callfilter;

import android.app.Application;

import com.novyr.callfilter.db.CallFilterDatabase;
import com.novyr.callfilter.db.LogRepository;

public class CallFilterApplication extends Application {
    private CallFilterDatabase getDatabase() {
        return CallFilterDatabase.getDatabase(this);
    }

    public LogRepository getLogRepository() {
        return LogRepository.getInstance(getDatabase());
    }
}
