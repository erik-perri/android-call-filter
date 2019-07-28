package com.novyr.callfilter.db;

import android.content.Context;

import com.novyr.callfilter.db.dao.LogDao;
import com.novyr.callfilter.db.dao.WhitelistDao;
import com.novyr.callfilter.db.entity.LogEntity;
import com.novyr.callfilter.db.entity.WhitelistEntity;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(entities = {LogEntity.class, WhitelistEntity.class}, version = 1)
public abstract class CallFilterDatabase extends RoomDatabase {
    private static volatile CallFilterDatabase INSTANCE;

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

    public abstract WhitelistDao whitelistDao();
}
