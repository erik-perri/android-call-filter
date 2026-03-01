package com.novyr.callfilter.call;

import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.filters.LargeTest;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.rule.GrantPermissionRule;
import androidx.test.uiautomator.UiDevice;

import com.novyr.callfilter.db.entity.LogEntity;
import com.novyr.callfilter.db.entity.RuleEntity;
import com.novyr.callfilter.db.entity.enums.LogAction;
import com.novyr.callfilter.db.entity.enums.RuleAction;
import com.novyr.callfilter.db.entity.enums.RuleType;
import com.novyr.callfilter.ui.loglist.LogListActivity;
import com.novyr.callfilter.util.ApiLevelAssumptions;
import com.novyr.callfilter.util.CallSimulator;
import com.novyr.callfilter.util.DatabaseHelper;
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
public class CallFilterServiceTest {
    private static final int POLL_TIMEOUT_MS = 10000;
    private static final int POLL_INTERVAL_MS = 250;

    @Rule
    public GrantPermissionRule permissions = PermissionHelper.grantAllPermissions();

    @Rule
    public ActivityScenarioRule<LogListActivity> activityRule =
            new ActivityScenarioRule<>(LogListActivity.class);

    private DatabaseHelper dbHelper;

    @Before
    public void setUp() throws Exception {
        ApiLevelAssumptions.assumeQOrHigher();

        UiDevice device = UiDevice.getInstance(
                InstrumentationRegistry.getInstrumentation());
        PermissionHelper.grantCallScreeningRole(device);

        dbHelper = new DatabaseHelper();
        dbHelper.clearLogs();
    }

    @After
    public void tearDown() {
        if (dbHelper != null) {
            dbHelper.clearLogs();
            dbHelper.resetRules(
                    new RuleEntity(RuleType.UNMATCHED, RuleAction.ALLOW, null, true, 0)
            );
        }
    }

    @Test
    public void unrecognizedBlocked() throws Exception {
        dbHelper.resetRules(
                new RuleEntity(RuleType.UNRECOGNIZED, RuleAction.BLOCK, null, true, 4),
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
    public void allowed() throws Exception {
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

    private void cancelCallSilently(String number) {
        try {
            CallSimulator.cancelCall(number);
        } catch (Exception ignored) {
        }
    }
}
