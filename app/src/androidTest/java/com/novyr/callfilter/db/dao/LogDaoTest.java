package com.novyr.callfilter.db.dao;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
import androidx.room.Room;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.filters.MediumTest;

import com.novyr.callfilter.db.CallFilterDatabase;
import com.novyr.callfilter.db.LiveDataTestUtil;
import com.novyr.callfilter.db.entity.LogEntity;
import com.novyr.callfilter.db.entity.enums.LogAction;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import static org.junit.Assert.assertEquals;

@MediumTest
public class LogDaoTest {

    @Rule
    public InstantTaskExecutorRule instantTaskExecutorRule = new InstantTaskExecutorRule();
    private CallFilterDatabase mDatabase;
    private LogDao mLogDao;

    @Before
    public void initDb() {
        // using an in-memory database because the information stored here disappears when the
        // process is killed
        mDatabase = Room.inMemoryDatabaseBuilder(
                ApplicationProvider.getApplicationContext(),
                CallFilterDatabase.class
        )
                        // allowing main thread queries, just for testing
                        .allowMainThreadQueries()
                        .build();

        mLogDao = mDatabase.logDao();
    }

    @After
    public void closeDb() {
        mDatabase.close();
    }

    @Test
    public void insertSavesData() throws InterruptedException {
        LogEntity[] entities = createEntities(1);
        mLogDao.insert(entities[0]);

        List<LogEntity> fetched = LiveDataTestUtil.getValue(mLogDao.findAll());
        assertEquals(1, fetched.size());
    }

    @Test
    public void deleteRemovesData() throws InterruptedException {
        LogEntity[] entities = createEntities(1);
        mLogDao.insert(entities[0]);

        List<LogEntity> fetched = LiveDataTestUtil.getValue(mLogDao.findAll());
        assertEquals(1, fetched.size());

        mLogDao.delete(fetched.get(0));

        assertEquals(0, LiveDataTestUtil.getValue(mLogDao.findAll()).size());
    }

    @Test
    public void deleteAllClearsData() throws InterruptedException {
        LogEntity[] entities = createEntities(10);
        for (LogEntity entity : entities) {
            mLogDao.insert(entity);
        }

        assertEquals(10, LiveDataTestUtil.getValue(mLogDao.findAll()).size());

        mLogDao.deleteAll();

        assertEquals(0, LiveDataTestUtil.getValue(mLogDao.findAll()).size());
    }

    @Test
    public void findAllRetrievesData() throws InterruptedException {
        LogEntity[] entities = createEntities(10);
        for (LogEntity entity : entities) {
            mLogDao.insert(entity);
        }

        List<LogEntity> fetched = LiveDataTestUtil.getValue(mLogDao.findAll());
        assertEquals(10, fetched.size());

        // Find all should retrieve in the reverse order they are added, so the first fetched
        // is the last entity
        assertEquals(entities[entities.length - 1].getNumber(), fetched.get(0).getNumber());
        assertEquals(entities[0].getNumber(), fetched.get(fetched.size() - 1).getNumber());
    }

    private LogEntity[] createEntities(int count) {
        LinkedList<LogEntity> entities = new LinkedList<>();
        Random random = new Random();

        for (int i = 0; i < count; i++) {
            int number = random.nextInt(8999999) + 1000000;
            entities.add(new LogEntity(
                    random.nextInt(1) > 0 ? LogAction.BLOCKED : LogAction.ALLOWED,
                    String.valueOf(number)
            ));
        }

        return entities.toArray(new LogEntity[0]);
    }
}
