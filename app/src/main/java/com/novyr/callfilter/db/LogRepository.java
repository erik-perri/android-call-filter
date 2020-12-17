package com.novyr.callfilter.db;

import androidx.lifecycle.LiveData;

import com.novyr.callfilter.db.dao.LogDao;
import com.novyr.callfilter.db.entity.LogEntity;

import java.util.List;

public class LogRepository {
    private static LogRepository sInstance;

    private final LogDao mDao;
    private final LiveData<List<LogEntity>> mEntities;

    private LogRepository(CallFilterDatabase database) {
        mDao = database.logDao();
        mEntities = mDao.findAll();
    }

    public static LogRepository getInstance(final CallFilterDatabase database) {
        if (sInstance == null) {
            synchronized (LogRepository.class) {
                if (sInstance == null) {
                    sInstance = new LogRepository(database);
                }
            }
        }
        return sInstance;
    }

    public LiveData<List<LogEntity>> findAll() {
        return mEntities;
    }

    public void insert(LogEntity entity) {
        CallFilterDatabase.databaseWriteExecutor.execute(() -> {
            mDao.insert(entity);
        });
    }

    public void deleteAll() {
        CallFilterDatabase.databaseWriteExecutor.execute(mDao::deleteAll);
    }

    public void delete(LogEntity entity) {
        CallFilterDatabase.databaseWriteExecutor.execute(() -> {
            mDao.delete(entity);
        });
    }
}
