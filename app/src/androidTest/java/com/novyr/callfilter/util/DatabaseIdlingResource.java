package com.novyr.callfilter.util;

import androidx.test.espresso.IdlingResource;

import com.novyr.callfilter.db.CallFilterDatabase;

import java.lang.reflect.Field;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;

public class DatabaseIdlingResource implements IdlingResource {
    private static final String NAME = "DatabaseIdlingResource";
    private volatile ResourceCallback callback;
    private final AtomicBoolean isIdle = new AtomicBoolean(true);

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public boolean isIdleNow() {
        ExecutorService executor = getDatabaseExecutor();
        if (executor == null) {
            return true;
        }

        isIdle.set(true);
        executor.execute(() -> {
            // If this runs, the executor has drained its queue up to this point
            if (!isIdle.get()) {
                return;
            }
            ResourceCallback cb = callback;
            if (cb != null) {
                cb.onTransitionToIdle();
            }
        });

        return isIdle.get();
    }

    @Override
    public void registerIdleTransitionCallback(ResourceCallback callback) {
        this.callback = callback;
    }

    private static ExecutorService getDatabaseExecutor() {
        try {
            Field field = CallFilterDatabase.class.getDeclaredField("databaseWriteExecutor");
            field.setAccessible(true);
            return (ExecutorService) field.get(null);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            return null;
        }
    }
}
