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
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.test.platform.app.InstrumentationRegistry;

import static org.junit.Assert.assertFalse;

public class DatabaseHelper {
    private static final int POLL_TIMEOUT_MS = 10000;
    private static final int POLL_INTERVAL_MS = 250;

    private final CallFilterDatabase db;
    private final LogDao logDao;
    private final RuleDao ruleDao;

    public DatabaseHelper() {
        Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        db = CallFilterDatabase.getDatabase(context);
        logDao = db.logDao();
        ruleDao = db.ruleDao();

        // Force the database to open so any onCreate callbacks (e.g. createDefaultRules)
        // are queued on the write executor before we drain it. Room defers opening until
        // the first DAO call, so without this the drain would complete before the callback
        // is even submitted, causing a race with test data setup.
        ruleDao.deleteAll();

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

    public LogEntity pollForLogEntry() throws Exception {
        long deadline = System.currentTimeMillis() + POLL_TIMEOUT_MS;
        while (System.currentTimeMillis() < deadline) {
            List<LogEntity> entries = getLogEntries();
            if (entries != null && !entries.isEmpty()) {
                return entries.get(0);
            }
            Thread.sleep(POLL_INTERVAL_MS);
        }
        List<LogEntity> entries = getLogEntries();
        assertFalse("No log entry appeared within timeout", entries == null || entries.isEmpty());
        return entries.get(0);
    }


    private void drainWriteExecutor() {
        try {
            Field field = CallFilterDatabase.class.getDeclaredField("databaseWriteExecutor");
            field.setAccessible(true);
            ExecutorService executor = (ExecutorService) field.get(null);
            if (executor != null) {
                int poolSize = (executor instanceof ThreadPoolExecutor)
                        ? ((ThreadPoolExecutor) executor).getMaximumPoolSize()
                        : 1;

                // Occupy every thread simultaneously so we know all prior tasks have finished.
                // Each thread blocks on the latch until all threads are parked, proving no
                // previously-queued work (e.g. createDefaultRules) is still running.
                CountDownLatch arrived = new CountDownLatch(poolSize);
                CountDownLatch release = new CountDownLatch(1);

                List<Future<?>> futures = new ArrayList<>();
                for (int i = 0; i < poolSize; i++) {
                    futures.add(executor.submit(() -> {
                        arrived.countDown();
                        try {
                            release.await(5, TimeUnit.SECONDS);
                        } catch (InterruptedException ignored) {
                        }
                    }));
                }

                // Wait until every thread has entered our task (meaning prior work is done)
                arrived.await(5, TimeUnit.SECONDS);
                // Release all threads
                release.countDown();

                for (Future<?> future : futures) {
                    future.get(5, TimeUnit.SECONDS);
                }
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
