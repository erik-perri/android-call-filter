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

import java.util.Date;
import java.util.List;

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
    public void getLogAfterInserted() throws InterruptedException {
        LogEntity entity = new LogEntity(new Date(), LogAction.BLOCKED, "5551111");

        mLogDao.insert(entity);

        List<LogEntity> entities = LiveDataTestUtil.getValue(mLogDao.findAll());

        assertEquals(1, entities.size());

        assertEquals(entity.getCreated().getTime(), entities.get(0).getCreated().getTime());
        assertEquals(entity.getAction(), entities.get(0).getAction());
        assertEquals(entity.getNumber(), entities.get(0).getNumber());
    }
}
