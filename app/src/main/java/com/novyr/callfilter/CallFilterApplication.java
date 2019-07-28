package com.novyr.callfilter;

import android.app.Application;

import com.novyr.callfilter.db.CallFilterDatabase;
import com.novyr.callfilter.db.LogRepository;
import com.novyr.callfilter.db.WhitelistRepository;

public class CallFilterApplication extends Application {
    private CallFilterDatabase getDatabase() {
        return CallFilterDatabase.getDatabase(this);
    }

    public LogRepository getLogRepository() {
        return LogRepository.getInstance(getDatabase());
    }

    public WhitelistRepository getWhitelistRepository() {
        return WhitelistRepository.getInstance(getDatabase());
    }
}
