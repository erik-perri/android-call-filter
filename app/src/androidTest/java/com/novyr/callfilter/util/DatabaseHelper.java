package com.novyr.callfilter.util;

import android.content.Context;

import com.novyr.callfilter.db.CallFilterDatabase;
import com.novyr.callfilter.db.dao.LogDao;
import com.novyr.callfilter.db.dao.RuleDao;
import com.novyr.callfilter.db.entity.LogEntity;
import com.novyr.callfilter.db.entity.RuleEntity;
import com.novyr.callfilter.db.entity.enums.LogAction;
import com.novyr.callfilter.db.entity.enums.RuleAction;
import com.novyr.callfilter.db.entity.enums.RuleType;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.test.platform.app.InstrumentationRegistry;

public class DatabaseHelper {
    private final CallFilterDatabase db;
    private final LogDao logDao;
    private final RuleDao ruleDao;

    public DatabaseHelper() {
        Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        db = CallFilterDatabase.getDatabase(context);
        logDao = db.logDao();
        ruleDao = db.ruleDao();
    }

    public void insertRule(RuleType type, RuleAction action, String value, boolean enabled, int order) {
        ruleDao.insert(new RuleEntity(type, action, value, enabled, order));
    }

    public void resetRules(RuleEntity... rules) {
        ruleDao.deleteAll();
        for (RuleEntity rule : rules) {
            ruleDao.insert(rule);
        }
    }

    public void clearLogs() {
        logDao.deleteAll();
    }

    public void insertLog(LogAction action, String number) {
        logDao.insert(new LogEntity(action, number));
    }

    public List<LogEntity> getLogEntries() throws InterruptedException {
        return getValueFromLiveData(logDao.findAll());
    }

    private static <T> T getValueFromLiveData(LiveData<T> liveData) throws InterruptedException {
        AtomicReference<T> result = new AtomicReference<>();
        CountDownLatch latch = new CountDownLatch(1);
        Observer<T> observer = value -> {
            result.set(value);
            latch.countDown();
        };
        InstrumentationRegistry.getInstrumentation().runOnMainSync(
                () -> liveData.observeForever(observer)
        );
        try {
            latch.await(5, TimeUnit.SECONDS);
        } finally {
            InstrumentationRegistry.getInstrumentation().runOnMainSync(
                    () -> liveData.removeObserver(observer)
            );
        }
        return result.get();
    }
}
