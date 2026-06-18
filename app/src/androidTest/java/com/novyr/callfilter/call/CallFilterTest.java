package com.novyr.callfilter.call;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import android.os.Build;

import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.filters.LargeTest;
import androidx.test.filters.SdkSuppress;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.rule.GrantPermissionRule;
import androidx.test.uiautomator.UiDevice;

import com.novyr.callfilter.CallReceiver;
import com.novyr.callfilter.db.entity.LogEntity;
import com.novyr.callfilter.db.entity.RuleEntity;
import com.novyr.callfilter.db.entity.enums.LogAction;
import com.novyr.callfilter.db.entity.enums.RuleAction;
import com.novyr.callfilter.db.entity.enums.RuleType;
import com.novyr.callfilter.ui.loglist.LogListActivity;
import com.novyr.callfilter.util.CallSimulator;
import com.novyr.callfilter.util.ContactHelper;
import com.novyr.callfilter.util.DatabaseHelper;
import com.novyr.callfilter.util.PermissionHelper;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.util.List;

@LargeTest
public class CallFilterTest {
    @Rule
    public GrantPermissionRule permissions = PermissionHelper.grantAllPermissions();

    @Rule
    public ActivityScenarioRule<LogListActivity> activityRule =
            new ActivityScenarioRule<>(LogListActivity.class);

    private DatabaseHelper dbHelper;
    private ContactHelper contactHelper;

    @Before
    public void setUp() throws Exception {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            UiDevice device = UiDevice.getInstance(
                    InstrumentationRegistry.getInstrumentation());
            PermissionHelper.grantCallScreeningRole(device);
        } else {
            CallReceiver.resetState();
        }
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

    @SdkSuppress(minSdkVersion = 28, maxSdkVersion = 28)
    @Test
    public void allowCall_upgradeSequence_producesOneLogEntry() throws Exception {
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
            CallSimulator.cancelCallSilently("5557777");
        }
    }

    @Test
    public void answerAndEnd_matchingRule_attemptsAnswerThenHangUp() throws Exception {
        dbHelper.resetRules(
                new RuleEntity(RuleType.UNRECOGNIZED, RuleAction.ANSWER_AND_END, null, true, 4),
                new RuleEntity(RuleType.UNMATCHED, RuleAction.ALLOW, null, true, 0)
        );

        CallSimulator.simulateIncomingCall("5551234");
        try {
            LogEntity log = dbHelper.pollForLogEntry();
            assertEquals("5551234", log.getNumber());
            assertAnswerAndEndOutcome(log.getAction());
        } finally {
            CallSimulator.cancelCallSilently("5551234");
        }
    }

    /**
     * On Q+ the screening service cannot answer, so it silences the call and hands it off to
     * CallReceiver via PendingAnswerStore. Duplicate RINGING broadcasts must not answer twice or
     * log twice — the atomic claim guarantees exactly one outcome is recorded.
     */
    @SdkSuppress(minSdkVersion = Build.VERSION_CODES.Q)
    @Test
    public void answerAndEnd_handoff_producesOneLogEntry() throws Exception {
        dbHelper.resetRules(
                new RuleEntity(RuleType.UNRECOGNIZED, RuleAction.ANSWER_AND_END, null, true, 4),
                new RuleEntity(RuleType.UNMATCHED, RuleAction.ALLOW, null, true, 0)
        );

        CallSimulator.simulateIncomingCall("5558888");
        try {
            List<LogEntity> entries = pollForStableLogCount();
            assertEquals("Answer handoff should log exactly one outcome", 1, entries.size());
            assertEquals("5558888", entries.get(0).getNumber());
            assertAnswerAndEndOutcome(entries.get(0).getAction());
        } finally {
            CallSimulator.cancelCallSilently("5558888");
        }
    }

    /**
     * The answer-then-hang-up path resolves three honest ways depending on the telephony stack:
     * the answer connects and we end the call ({@code ENDED_NO_VOICEMAIL}); the answer never
     * reaches off-hook so we fall back ({@code FELL_BACK_TO_BLOCK}); or the telephony call fails
     * outright ({@code FAILED}). All three prove the dispatch took the answer path rather than a
     * plain block or allow. Which one occurs — and whether voicemail is truly avoided — can only be
     * pinned down on real hardware with a live carrier, so the emulator assertion checks the path,
     * not the exact outcome.
     */
    private void assertAnswerAndEndOutcome(LogAction action) {
        assertTrue(
                "Expected an answer-then-hang-up outcome but was " + action,
                action == LogAction.ENDED_NO_VOICEMAIL
                        || action == LogAction.FELL_BACK_TO_BLOCK
                        || action == LogAction.FAILED
        );
    }

    /**
     * Polls until the log entry count has not changed for STABLE_MS consecutive milliseconds.
     * This avoids a fixed sleep while still catching late spurious entries (e.g. from duplicate
     * broadcasts on API 28).
     */
    private List<LogEntity> pollForStableLogCount() throws Exception {
        final int STABLE_MS = 2000;
        final int POLL_TIMEOUT_MS = 10000;
        final int POLL_INTERVAL_MS = 250;
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
}
