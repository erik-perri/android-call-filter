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
import com.novyr.callfilter.util.ContactHelper;
import com.novyr.callfilter.util.DatabaseHelper;
import com.novyr.callfilter.util.PermissionHelper;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

@LargeTest
public class CallFilterServiceTest {
    @Rule
    public GrantPermissionRule permissions = PermissionHelper.grantAllPermissions();

    @Rule
    public ActivityScenarioRule<LogListActivity> activityRule =
            new ActivityScenarioRule<>(LogListActivity.class);

    private DatabaseHelper dbHelper;
    private ContactHelper contactHelper;

    @Before
    public void setUp() throws Exception {
        ApiLevelAssumptions.assumeQOrHigher();

        UiDevice device = UiDevice.getInstance(
                InstrumentationRegistry.getInstrumentation());
        PermissionHelper.grantCallScreeningRole(device);

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
    public void allowCall_unrecognizedNumber_callBlocked() throws Exception {
        dbHelper.resetRules(
                new RuleEntity(RuleType.UNRECOGNIZED, RuleAction.BLOCK, null, true, 4),
                new RuleEntity(RuleType.UNMATCHED, RuleAction.ALLOW, null, true, 0)
        );

        CallSimulator.simulateIncomingCall("5551234");
        try {
            LogEntity log = dbHelper.pollForLogEntry();
            assertEquals("5551234", log.getNumber());
            assertEquals(LogAction.BLOCKED, log.getAction());
        } finally {
            CallSimulator.cancelCallSilently("5551234");
        }
    }

    @Test
    public void allowCall_privateBlockRule_callBlocked() throws Exception {
        dbHelper.resetRules(
                new RuleEntity(RuleType.PRIVATE, RuleAction.BLOCK, null, true, 4),
                new RuleEntity(RuleType.UNMATCHED, RuleAction.ALLOW, null, true, 0)
        );

        CallSimulator.simulatePrivateCall();
        try {
            LogEntity log = dbHelper.pollForLogEntry();
            assertNull(log.getNumber());
            assertEquals(LogAction.BLOCKED, log.getAction());
        } finally {
            CallSimulator.cancelCallSilently("#");
        }
    }

    @Test
    public void allowCall_noPrivateRule_callAllowed() throws Exception {
        dbHelper.resetRules(
                new RuleEntity(RuleType.UNMATCHED, RuleAction.ALLOW, null, true, 0)
        );

        CallSimulator.simulatePrivateCall();
        try {
            LogEntity log = dbHelper.pollForLogEntry();
            assertNull(log.getNumber());
            assertEquals(LogAction.ALLOWED, log.getAction());
        } finally {
            CallSimulator.cancelCallSilently("#");
        }
    }

    @Test
    public void allowCall_noMatchingRules_callAllowed() throws Exception {
        dbHelper.resetRules(
                new RuleEntity(RuleType.UNMATCHED, RuleAction.ALLOW, null, true, 0)
        );

        CallSimulator.simulateIncomingCall("5551234");
        try {
            LogEntity log = dbHelper.pollForLogEntry();
            assertEquals("5551234", log.getNumber());
            assertEquals(LogAction.ALLOWED, log.getAction());
        } finally {
            CallSimulator.cancelCallSilently("5551234");
        }
    }

    @Test
    public void allowCall_contactNumber_callAllowed() throws Exception {
        dbHelper.resetRules(
                new RuleEntity(RuleType.UNRECOGNIZED, RuleAction.BLOCK, null, true, 4),
                new RuleEntity(RuleType.UNMATCHED, RuleAction.ALLOW, null, true, 0)
        );
        contactHelper.insertContact("Test Contact", "5553456");

        CallSimulator.simulateIncomingCall("5553456");
        try {
            LogEntity log = dbHelper.pollForLogEntry();
            assertEquals("5553456", log.getNumber());
            assertEquals(LogAction.ALLOWED, log.getAction());
        } finally {
            CallSimulator.cancelCallSilently("5553456");
        }
    }

    @Test
    public void allowCall_areaCodeRule_callBlocked() throws Exception {
        dbHelper.resetRules(
                new RuleEntity(RuleType.AREA_CODE, RuleAction.BLOCK, "800", true, 4),
                new RuleEntity(RuleType.UNMATCHED, RuleAction.ALLOW, null, true, 0)
        );

        CallSimulator.simulateIncomingCall("8005551234");
        try {
            LogEntity log = dbHelper.pollForLogEntry();
            assertEquals("8005551234", log.getNumber());
            assertEquals(LogAction.BLOCKED, log.getAction());
        } finally {
            CallSimulator.cancelCallSilently("8005551234");
        }
    }

    @Test
    public void allowCall_patternRule_callBlocked() throws Exception {
        dbHelper.resetRules(
                new RuleEntity(RuleType.MATCH, RuleAction.BLOCK, "555*", true, 4),
                new RuleEntity(RuleType.UNMATCHED, RuleAction.ALLOW, null, true, 0)
        );

        CallSimulator.simulateIncomingCall("5551234");
        try {
            LogEntity log = dbHelper.pollForLogEntry();
            assertEquals("5551234", log.getNumber());
            assertEquals(LogAction.BLOCKED, log.getAction());
        } finally {
            CallSimulator.cancelCallSilently("5551234");
        }
    }

    @Test
    public void allowCall_disabledRule_callAllowed() throws Exception {
        dbHelper.resetRules(
                new RuleEntity(RuleType.UNRECOGNIZED, RuleAction.BLOCK, null, false, 4),
                new RuleEntity(RuleType.UNMATCHED, RuleAction.ALLOW, null, true, 0)
        );

        CallSimulator.simulateIncomingCall("5559999");
        try {
            LogEntity log = dbHelper.pollForLogEntry();
            assertEquals("5559999", log.getNumber());
            assertEquals(LogAction.ALLOWED, log.getAction());
        } finally {
            CallSimulator.cancelCallSilently("5559999");
        }
    }
}
