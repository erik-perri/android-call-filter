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

import java.lang.reflect.Field;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
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

        // Drain the write executor so any pending onCreate callbacks (e.g. createDefaultRules)
        // complete before tests manipulate data.
        drainWriteExecutor();
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

    public List<RuleEntity> getRuleEntries() throws InterruptedException {
        return getValueFromLiveData(ruleDao.findAll());
    }

    private void drainWriteExecutor() {
        try {
            Field field = CallFilterDatabase.class.getDeclaredField("databaseWriteExecutor");
            field.setAccessible(true);
            ExecutorService executor = (ExecutorService) field.get(null);
            if (executor != null) {
                executor.submit(() -> {}).get(5, TimeUnit.SECONDS);
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to drain database write executor", e);
        }
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
