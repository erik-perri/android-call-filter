package com.novyr.callfilter.db;

import androidx.lifecycle.LiveData;

import com.novyr.callfilter.db.dao.RuleDao;
import com.novyr.callfilter.db.entity.RuleEntity;

import java.util.List;

public class RuleRepository {
    private static RuleRepository sInstance;

    private final RuleDao mDao;
    private final LiveData<List<RuleEntity>> mEntities;

    private RuleRepository(CallFilterDatabase database) {
        mDao = database.ruleDao();
        mEntities = mDao.findAll();
    }

    public static RuleRepository getInstance(final CallFilterDatabase database) {
        if (sInstance == null) {
            synchronized (RuleRepository.class) {
                if (sInstance == null) {
                    sInstance = new RuleRepository(database);
                }
            }
        }
        return sInstance;
    }

    public LiveData<List<RuleEntity>> findAll() {
        return mEntities;
    }

    public List<RuleEntity> findEnabled() {
        return mDao.findEnabled();
    }

    public void insert(RuleEntity entity) {
        CallFilterDatabase.databaseWriteExecutor.execute(() -> mDao.insert(entity));
    }

    public void delete(RuleEntity entity) {
        CallFilterDatabase.databaseWriteExecutor.execute(() -> mDao.delete(entity));
    }

    public void update(RuleEntity entity) {
        CallFilterDatabase.databaseWriteExecutor.execute(() -> mDao.update(entity));
    }

    public LiveData<Integer> highestOrder() {
        return mDao.highestOrder();
    }
}
