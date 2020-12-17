package com.novyr.callfilter.db;

import androidx.lifecycle.LiveData;

import com.novyr.callfilter.db.dao.WhitelistDao;
import com.novyr.callfilter.db.entity.WhitelistEntity;

import java.util.List;

public class WhitelistRepository {
    private static WhitelistRepository sInstance;

    private final WhitelistDao mDao;
    private final LiveData<List<WhitelistEntity>> mEntities;

    private WhitelistRepository(CallFilterDatabase database) {
        mDao = database.whitelistDao();
        mEntities = mDao.findAll();
    }

    public static WhitelistRepository getInstance(final CallFilterDatabase database) {
        if (sInstance == null) {
            synchronized (WhitelistRepository.class) {
                if (sInstance == null) {
                    sInstance = new WhitelistRepository(database);
                }
            }
        }
        return sInstance;
    }

    public LiveData<List<WhitelistEntity>> findAll() {
        return mEntities;
    }

    public WhitelistEntity findByNumber(String number) {
        return mDao.findAll(number);
    }

    public void insert(WhitelistEntity entity) {
        CallFilterDatabase.databaseWriteExecutor.execute(() -> {
            mDao.insert(entity);
        });
    }

    public void delete(WhitelistEntity entity) {
        CallFilterDatabase.databaseWriteExecutor.execute(() -> {
            mDao.delete(entity);
        });
    }
}
