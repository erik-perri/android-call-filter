package com.novyr.callfilter.call;

import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.filters.LargeTest;
import androidx.test.rule.GrantPermissionRule;

import com.novyr.callfilter.CallReceiver;
import com.novyr.callfilter.db.entity.LogEntity;
import com.novyr.callfilter.db.entity.RuleEntity;
import com.novyr.callfilter.db.entity.enums.LogAction;
import com.novyr.callfilter.db.entity.enums.RuleAction;
import com.novyr.callfilter.db.entity.enums.RuleType;
import com.novyr.callfilter.ui.loglist.LogListActivity;
import com.novyr.callfilter.util.ApiLevelAssumptions;
import com.novyr.callfilter.util.CallSimulator;
import com.novyr.callfilter.util.DatabaseHelper;
import com.novyr.callfilter.util.ContactHelper;
import com.novyr.callfilter.util.PermissionHelper;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;

@LargeTest
public class CallReceiverTest {
    private static final int POLL_TIMEOUT_MS = 10000;
    private static final int POLL_INTERVAL_MS = 250;

    @Rule
    public GrantPermissionRule permissions = PermissionHelper.grantAllPermissions();

    @Rule
    public ActivityScenarioRule<LogListActivity> activityRule =
            new ActivityScenarioRule<>(LogListActivity.class);

    private DatabaseHelper dbHelper;
    private ContactHelper contactHelper;

    @Before
    public void setUp() {
        ApiLevelAssumptions.assumePreQ();
        CallReceiver.resetState();
        dbHelper = new DatabaseHelper();
        dbHelper.clearLogs();
        contactHelper = new ContactHelper();
    }

    @After
    public void tearDown() {
        if (contactHelper != null) {
            contactHelper.cleanupContacts();
        }
        if (dbHelper != null) {
            dbHelper.clearLogs();
            dbHelper.resetRules(
                    new RuleEntity(RuleType.UNMATCHED, RuleAction.ALLOW, null, true, 0)
            );
        }
    }

    @Test
    public void noMatchingRulesAllowed() throws Exception {
        dbHelper.resetRules(
                new RuleEntity(RuleType.UNMATCHED, RuleAction.ALLOW, null, true, 0)
        );

        CallSimulator.simulateIncomingCall("5551234");
        try {
            LogEntity log = pollForLogEntry();
            assertEquals("5551234", log.getNumber());
            assertEquals(LogAction.ALLOWED, log.getAction());
        } finally {
            cancelCallSilently("5551234");
        }
    }

    @Test
    public void unrecognizedBlocked() throws Exception {
        dbHelper.resetRules(
                new RuleEntity(RuleType.UNRECOGNIZED, RuleAction.BLOCK, null, true, 4),
                new RuleEntity(RuleType.UNMATCHED, RuleAction.ALLOW, null, true, 0)
        );

        CallSimulator.simulateIncomingCall("5559999");
        try {
            LogEntity log = pollForLogEntry();
            assertEquals("5559999", log.getNumber());
            assertEquals(LogAction.BLOCKED, log.getAction());
        } finally {
            cancelCallSilently("5559999");
        }
    }

    @Test
    public void privateBlocked() throws Exception {
        dbHelper.resetRules(
                new RuleEntity(RuleType.PRIVATE, RuleAction.BLOCK, null, true, 4),
                new RuleEntity(RuleType.UNMATCHED, RuleAction.ALLOW, null, true, 0)
        );

        CallSimulator.simulatePrivateCall();
        try {
            LogEntity log = pollForLogEntry();
            assertNull(log.getNumber());
            assertEquals(LogAction.BLOCKED, log.getAction());
        } finally {
            cancelCallSilently("#");
        }
    }

    @Test
    public void privateAllowed() throws Exception {
        dbHelper.resetRules(
                new RuleEntity(RuleType.UNMATCHED, RuleAction.ALLOW, null, true, 0)
        );

        CallSimulator.simulatePrivateCall();
        try {
            LogEntity log = pollForLogEntry();
            assertNull(log.getNumber());
            assertEquals(LogAction.ALLOWED, log.getAction());
        } finally {
            cancelCallSilently("#");
        }
    }

    @Test
    public void contactAllowed() throws Exception {
        dbHelper.resetRules(
                new RuleEntity(RuleType.UNRECOGNIZED, RuleAction.BLOCK, null, true, 4),
                new RuleEntity(RuleType.UNMATCHED, RuleAction.ALLOW, null, true, 0)
        );
        contactHelper.insertContact("Test Contact", "5553456");

        CallSimulator.simulateIncomingCall("5553456");
        try {
            LogEntity log = pollForLogEntry();
            assertEquals("5553456", log.getNumber());
            assertEquals(LogAction.ALLOWED, log.getAction());
        } finally {
            cancelCallSilently("5553456");
        }
    }

    @Test
    public void areaCodeBlocked() throws Exception {
        dbHelper.resetRules(
                new RuleEntity(RuleType.AREA_CODE, RuleAction.BLOCK, "800", true, 4),
                new RuleEntity(RuleType.UNMATCHED, RuleAction.ALLOW, null, true, 0)
        );

        CallSimulator.simulateIncomingCall("8005551234");
        try {
            LogEntity log = pollForLogEntry();
            assertEquals("8005551234", log.getNumber());
            assertEquals(LogAction.BLOCKED, log.getAction());
        } finally {
            cancelCallSilently("8005551234");
        }
    }

    @Test
    public void patternBlocked() throws Exception {
        dbHelper.resetRules(
                new RuleEntity(RuleType.MATCH, RuleAction.BLOCK, "555*", true, 4),
                new RuleEntity(RuleType.UNMATCHED, RuleAction.ALLOW, null, true, 0)
        );

        CallSimulator.simulateIncomingCall("5551234");
        try {
            LogEntity log = pollForLogEntry();
            assertEquals("5551234", log.getNumber());
            assertEquals(LogAction.BLOCKED, log.getAction());
        } finally {
            cancelCallSilently("5551234");
        }
    }

    @Test
    public void disabledRuleIgnored() throws Exception {
        dbHelper.resetRules(
                new RuleEntity(RuleType.UNRECOGNIZED, RuleAction.BLOCK, null, false, 4),
                new RuleEntity(RuleType.UNMATCHED, RuleAction.ALLOW, null, true, 0)
        );

        CallSimulator.simulateIncomingCall("5559999");
        try {
            LogEntity log = pollForLogEntry();
            assertEquals("5559999", log.getNumber());
            assertEquals(LogAction.ALLOWED, log.getAction());
        } finally {
            cancelCallSilently("5559999");
        }
    }

    @Test
    public void upgradeSequenceProducesOneEntry() throws Exception {
        ApiLevelAssumptions.assumePieOrHigher();
        dbHelper.resetRules(
                new RuleEntity(RuleType.UNMATCHED, RuleAction.ALLOW, null, true, 0)
        );

        CallSimulator.simulateIncomingCall("5557777");
        try {
            List<LogEntity> entries = pollForStableLogCount();
            assertEquals("Expected exactly one log entry for the upgrade sequence", 1, entries.size());
            assertEquals("5557777", entries.get(0).getNumber());
            assertEquals(LogAction.ALLOWED, entries.get(0).getAction());
        } finally {
            cancelCallSilently("5557777");
        }
    }

    private LogEntity pollForLogEntry() throws Exception {
        long deadline = System.currentTimeMillis() + POLL_TIMEOUT_MS;
        while (System.currentTimeMillis() < deadline) {
            List<LogEntity> entries = dbHelper.getLogEntries();
            if (entries != null && !entries.isEmpty()) {
                return entries.get(0);
            }
            Thread.sleep(POLL_INTERVAL_MS);
        }
        List<LogEntity> entries = dbHelper.getLogEntries();
        assertFalse("No log entry appeared within timeout", entries == null || entries.isEmpty());
        return entries.get(0);
    }

    /**
     * Polls until the log entry count has not changed for STABLE_MS consecutive milliseconds.
     * This avoids a fixed sleep while still catching late spurious entries (e.g. from duplicate
     * broadcasts on API 28).
     */
    private List<LogEntity> pollForStableLogCount() throws Exception {
        final int STABLE_MS = 2000;
        long stableSince = System.currentTimeMillis();
        int lastCount = dbHelper.getLogEntries().size();
        long deadline = System.currentTimeMillis() + POLL_TIMEOUT_MS;

        while (System.currentTimeMillis() < deadline) {
            Thread.sleep(POLL_INTERVAL_MS);
            int currentCount = dbHelper.getLogEntries().size();
            if (currentCount != lastCount) {
                lastCount = currentCount;
                stableSince = System.currentTimeMillis();
            } else if (System.currentTimeMillis() - stableSince >= STABLE_MS) {
                return dbHelper.getLogEntries();
            }
        }

        return dbHelper.getLogEntries();
    }

    private void cancelCallSilently(String number) {
        try {
            CallSimulator.cancelCall(number);
        } catch (Exception ignored) {
        }
    }
}
