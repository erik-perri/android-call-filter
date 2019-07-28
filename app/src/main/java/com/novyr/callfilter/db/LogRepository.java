package com.novyr.callfilter.db;

import android.os.AsyncTask;

import com.novyr.callfilter.db.dao.LogDao;
import com.novyr.callfilter.db.entity.LogEntity;

import java.util.List;

import androidx.lifecycle.LiveData;

public class LogRepository {
    private static LogRepository sInstance;

    private final LogDao mDao;
    private final LiveData<List<LogEntity>> mEntities;

    private LogRepository(CallFilterDatabase database) {
        mDao = database.logEntryDao();
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

    public void insert(LogEntity entry) {
        new InsertAsyncTask(mDao).execute(entry);
    }

    public void clear() {
        new ClearAsyncTask(mDao).execute();
    }

    public void delete(LogEntity log) {
        new DeleteAsyncTask(mDao).execute(log);
    }

    private static class InsertAsyncTask extends AsyncTask<LogEntity, Void, Void> {
        private final LogDao mAsyncTaskDao;

        InsertAsyncTask(LogDao dao) {
            mAsyncTaskDao = dao;
        }

        @Override
        protected Void doInBackground(final LogEntity... params) {
            mAsyncTaskDao.insert(params[0]);
            return null;
        }
    }

    private static class ClearAsyncTask extends AsyncTask<Void, Void, Void> {
        private final LogDao mAsyncTaskDao;

        ClearAsyncTask(LogDao dao) {
            mAsyncTaskDao = dao;
        }

        @Override
        protected Void doInBackground(final Void... params) {
            mAsyncTaskDao.deleteAll();
            return null;
        }
    }

    private static class DeleteAsyncTask extends AsyncTask<LogEntity, Void, Void> {
        private final LogDao mAsyncTaskDao;

        DeleteAsyncTask(LogDao dao) {
            mAsyncTaskDao = dao;
        }

        @Override
        protected Void doInBackground(final LogEntity... params) {
            mAsyncTaskDao.delete(params[0]);
            return null;
        }
    }
}
