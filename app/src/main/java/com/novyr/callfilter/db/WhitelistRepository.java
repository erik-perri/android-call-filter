package com.novyr.callfilter.db;

import android.os.AsyncTask;

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
        new InsertAsyncTask(mDao).execute(entity);
    }

    public void delete(WhitelistEntity entity) {
        new DeleteAsyncTask(mDao).execute(entity);
    }

    private static class InsertAsyncTask extends AsyncTask<WhitelistEntity, Void, Void> {
        private final WhitelistDao mAsyncTaskDao;

        InsertAsyncTask(WhitelistDao dao) {
            mAsyncTaskDao = dao;
        }

        @Override
        protected Void doInBackground(final WhitelistEntity... params) {
            mAsyncTaskDao.insert(params[0]);
            return null;
        }
    }

    private static class DeleteAsyncTask extends AsyncTask<WhitelistEntity, Void, Void> {
        private final WhitelistDao mAsyncTaskDao;

        DeleteAsyncTask(WhitelistDao dao) {
            mAsyncTaskDao = dao;
        }

        @Override
        protected Void doInBackground(final WhitelistEntity... params) {
            mAsyncTaskDao.delete(params[0]);
            return null;
        }
    }
}
