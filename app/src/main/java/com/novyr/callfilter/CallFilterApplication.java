package com.novyr.callfilter;

import android.app.Application;

import com.novyr.callfilter.db.CallFilterDatabase;
import com.novyr.callfilter.db.LogRepository;
import com.novyr.callfilter.db.RuleRepository;
import com.novyr.callfilter.telephony.MicMuteGuard;

public class CallFilterApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        CallReceiver.executor.execute(() -> MicMuteGuard.recoverIfLeaked(this));
    }

    private CallFilterDatabase getDatabase() {
        return CallFilterDatabase.getDatabase(this);
    }

    public LogRepository getLogRepository() {
        return LogRepository.getInstance(getDatabase());
    }

    public RuleRepository getRuleRepository() {
        return RuleRepository.getInstance(getDatabase());
    }
}
