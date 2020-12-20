package com.novyr.callfilter.db;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.novyr.callfilter.db.dao.LogDao;
import com.novyr.callfilter.db.entity.LogEntity;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Database(entities = {LogEntity.class}, version = 2)
public abstract class CallFilterDatabase extends RoomDatabase {
    private static volatile CallFilterDatabase INSTANCE;
    // TODO 4 is from the docs example but seems like a lot for our needs
    private static final int NUMBER_OF_THREADS = 4;
    static final ExecutorService databaseWriteExecutor =
            Executors.newFixedThreadPool(NUMBER_OF_THREADS);

    public static CallFilterDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (CallFilterDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room
                            .databaseBuilder(
                                    context.getApplicationContext(),
                                    CallFilterDatabase.class,
                                    "call_filter"
                            )
                            .build();
                }
            }
        }
        return INSTANCE;
    }

    public abstract LogDao logDao();
}
