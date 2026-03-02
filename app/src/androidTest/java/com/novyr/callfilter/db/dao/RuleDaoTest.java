package com.novyr.callfilter.db.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
import androidx.room.Room;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.filters.MediumTest;

import com.novyr.callfilter.db.CallFilterDatabase;
import com.novyr.callfilter.db.LiveDataTestUtil;
import com.novyr.callfilter.db.entity.RuleEntity;
import com.novyr.callfilter.db.entity.enums.RuleAction;
import com.novyr.callfilter.db.entity.enums.RuleType;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.util.List;

@MediumTest
public class RuleDaoTest {

    @Rule
    public InstantTaskExecutorRule instantTaskExecutorRule = new InstantTaskExecutorRule();

    private CallFilterDatabase mDatabase;
    private RuleDao mRuleDao;

    @Before
    public void initDb() {
        mDatabase = Room.inMemoryDatabaseBuilder(
                ApplicationProvider.getApplicationContext(),
                CallFilterDatabase.class
        )
                .allowMainThreadQueries()
                .build();

        mRuleDao = mDatabase.ruleDao();
    }

    @After
    public void closeDb() {
        mDatabase.close();
    }

    @Test
    public void insert_singleRule_savesToDatabase() throws InterruptedException {
        RuleEntity rule = new RuleEntity(RuleType.MATCH, RuleAction.BLOCK, "555*", true, 0);
        mRuleDao.insert(rule);

        List<RuleEntity> fetched = LiveDataTestUtil.getValue(mRuleDao.findAll());
        assertNotNull(fetched);
        assertEquals(1, fetched.size());
        assertEquals(RuleType.MATCH, fetched.get(0).getType());
        assertEquals("555*", fetched.get(0).getValue());
    }

    @Test
    public void update_changeAction_modifiesRule() throws InterruptedException {
        RuleEntity rule = new RuleEntity(RuleType.UNMATCHED, RuleAction.ALLOW, null, true, 0);
        mRuleDao.insert(rule);

        List<RuleEntity> fetched = LiveDataTestUtil.getValue(mRuleDao.findAll());
        assertNotNull(fetched);
        RuleEntity inserted = fetched.get(0);
        inserted.setAction(RuleAction.BLOCK);
        mRuleDao.update(inserted);

        List<RuleEntity> updated = LiveDataTestUtil.getValue(mRuleDao.findAll());
        assertNotNull(updated);
        assertEquals(RuleAction.BLOCK, updated.get(0).getAction());
    }

    @Test
    public void delete_singleRule_removesFromDatabase() throws InterruptedException {
        RuleEntity rule = new RuleEntity(RuleType.PRIVATE, RuleAction.BLOCK, null, true, 0);
        mRuleDao.insert(rule);

        List<RuleEntity> fetched = LiveDataTestUtil.getValue(mRuleDao.findAll());
        assertNotNull(fetched);
        assertEquals(1, fetched.size());

        mRuleDao.delete(fetched.get(0));

        List<RuleEntity> afterDelete = LiveDataTestUtil.getValue(mRuleDao.findAll());
        assertNotNull(afterDelete);
        assertEquals(0, afterDelete.size());
    }

    @Test
    public void deleteAll_multipleRules_clearsDatabase() throws InterruptedException {
        for (int i = 0; i < 5; i++) {
            mRuleDao.insert(new RuleEntity(RuleType.MATCH, RuleAction.BLOCK, "555" + i, true, i));
        }

        List<RuleEntity> fetched = LiveDataTestUtil.getValue(mRuleDao.findAll());
        assertNotNull(fetched);
        assertEquals(5, fetched.size());

        mRuleDao.deleteAll();

        List<RuleEntity> afterDelete = LiveDataTestUtil.getValue(mRuleDao.findAll());
        assertNotNull(afterDelete);
        assertEquals(0, afterDelete.size());
    }

    @Test
    public void findEnabled_mixedEnabledState_excludesDisabled() {
        mRuleDao.insert(new RuleEntity(RuleType.RECOGNIZED, RuleAction.ALLOW, null, true, 4));
        mRuleDao.insert(new RuleEntity(RuleType.UNRECOGNIZED, RuleAction.BLOCK, null, true, 3));
        mRuleDao.insert(new RuleEntity(RuleType.PRIVATE, RuleAction.BLOCK, null, true, 2));
        mRuleDao.insert(new RuleEntity(RuleType.MATCH, RuleAction.BLOCK, "800*", false, 1));
        mRuleDao.insert(new RuleEntity(RuleType.UNMATCHED, RuleAction.ALLOW, null, false, 0));

        List<RuleEntity> enabled = mRuleDao.findEnabled();
        assertEquals(3, enabled.size());
    }

    @Test
    public void findAll_variousOrders_returnsByOrderDesc() throws InterruptedException {
        mRuleDao.insert(new RuleEntity(RuleType.UNMATCHED, RuleAction.ALLOW, null, true, 0));
        mRuleDao.insert(new RuleEntity(RuleType.RECOGNIZED, RuleAction.ALLOW, null, true, 2));
        mRuleDao.insert(new RuleEntity(RuleType.PRIVATE, RuleAction.BLOCK, null, true, 4));

        List<RuleEntity> fetched = LiveDataTestUtil.getValue(mRuleDao.findAll());
        assertNotNull(fetched);
        assertEquals(3, fetched.size());
        assertEquals(4, fetched.get(0).getOrder());
        assertEquals(2, fetched.get(1).getOrder());
        assertEquals(0, fetched.get(2).getOrder());
    }

    @Test
    public void highestOrder_multipleRules_returnsMax() throws InterruptedException {
        mRuleDao.insert(new RuleEntity(RuleType.UNMATCHED, RuleAction.ALLOW, null, true, 0));
        mRuleDao.insert(new RuleEntity(RuleType.RECOGNIZED, RuleAction.ALLOW, null, true, 2));
        mRuleDao.insert(new RuleEntity(RuleType.PRIVATE, RuleAction.BLOCK, null, true, 4));

        Integer highest = LiveDataTestUtil.getValue(mRuleDao.highestOrder());
        assertNotNull(highest);
        assertEquals(4, (int) highest);
    }
}
