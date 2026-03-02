package com.novyr.callfilter.ui.loglist;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.longClick;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.contrib.RecyclerViewActions.actionOnItemAtPosition;
import static androidx.test.espresso.intent.Intents.intended;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent;
import static androidx.test.espresso.matcher.RootMatchers.isDialog;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withContentDescription;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.not;

import android.os.Build;

import androidx.test.espresso.IdlingRegistry;
import androidx.test.espresso.intent.Intents;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.filters.MediumTest;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.rule.GrantPermissionRule;
import androidx.test.uiautomator.UiDevice;

import com.novyr.callfilter.R;
import com.novyr.callfilter.db.entity.enums.LogAction;
import com.novyr.callfilter.ui.rulelist.RuleListActivity;
import com.novyr.callfilter.util.DatabaseHelper;
import com.novyr.callfilter.util.DatabaseIdlingResource;
import com.novyr.callfilter.util.PermissionHelper;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

@MediumTest
public class LogListActivityTest {
    private final DatabaseHelper dbHelper = new DatabaseHelper();
    private final DatabaseIdlingResource idlingResource = new DatabaseIdlingResource();

    @Rule
    public GrantPermissionRule permissions = PermissionHelper.grantAllPermissions();

    @Rule
    public ActivityScenarioRule<LogListActivity> activityRule =
            new ActivityScenarioRule<>(LogListActivity.class);

    @Before
    public void setUp() throws Exception {
        IdlingRegistry.getInstance().register(idlingResource);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            UiDevice device = UiDevice.getInstance(
                    InstrumentationRegistry.getInstrumentation());
            PermissionHelper.grantCallScreeningRole(device);
        }

        dbHelper.clearLogs();
    }

    @After
    public void tearDown() {
        dbHelper.clearLogs();
        IdlingRegistry.getInstance().unregister(idlingResource);
    }

    @Test
    public void logList_noLogs_showsEmptyView() {
        onView(withId(R.id.empty_view))
                .check(matches(isDisplayed()));
        onView(withId(R.id.log_list))
                .check(matches(not(isDisplayed())));
    }

    @Test
    public void logList_logsExist_showsRecyclerView() {
        dbHelper.insertLog(LogAction.BLOCKED, "5550001");
        dbHelper.insertLog(LogAction.ALLOWED, "5550002");
        dbHelper.insertLog(LogAction.BLOCKED, "5550003");

        waitForIdle();

        onView(withId(R.id.log_list))
                .check(matches(isDisplayed()));
        onView(withId(R.id.empty_view))
                .check(matches(not(isDisplayed())));
    }

    @Test
    public void logEntry_hasNumber_showsNumberInMessage() {
        dbHelper.insertLog(LogAction.BLOCKED, "5551234");

        waitForIdle();

        onView(withId(R.id.log_list))
                .check(matches(isDisplayed()));
        onView(withId(R.id.log_list_message))
                .check(matches(withText(org.hamcrest.Matchers.containsString("555-1234"))));
    }

    @Test
    public void logEntry_displayed_showsTimestamp() {
        dbHelper.insertLog(LogAction.BLOCKED, "5550001");

        waitForIdle();

        onView(withId(R.id.log_list_created))
                .check(matches(not(withText(""))));
    }

    @Test
    public void settingsMenu_clicked_launchesRuleListActivity() {
        Intents.init();
        try {
            onView(withContentDescription("More options")).perform(click());
            onView(withText(R.string.action_settings))
                    .perform(click());
            intended(hasComponent(RuleListActivity.class.getName()));
        } finally {
            Intents.release();
        }
    }

    @Test
    public void clearLogsDialog_confirmed_showsEmptyView() {
        dbHelper.insertLog(LogAction.BLOCKED, "5550001");

        waitForIdle();

        onView(withContentDescription("More options")).perform(click());
        onView(withText(R.string.action_clear_logs))
                .perform(click());

        onView(withText(R.string.dialog_clear_logs_message))
                .inRoot(isDialog())
                .check(matches(isDisplayed()));

        onView(withText(R.string.yes))
                .inRoot(isDialog())
                .check(matches(isDisplayed()))
                .perform(click());

        onView(withId(R.id.empty_view))
                .check(matches(isDisplayed()));
    }

    @Test
    public void clearLogsDialog_cancelled_logsRemainVisible() {
        dbHelper.insertLog(LogAction.BLOCKED, "5550001");

        waitForIdle();

        onView(withContentDescription("More options")).perform(click());
        onView(withText(R.string.action_clear_logs))
                .perform(click());

        onView(withText(R.string.no))
                .inRoot(isDialog())
                .perform(click());

        onView(withId(R.id.log_list))
                .check(matches(isDisplayed()));
    }

    @Test
    public void logEntry_removeFromContextMenu_removesEntry() {
        dbHelper.insertLog(LogAction.BLOCKED, "5550001");

        waitForIdle();

        onView(withId(R.id.log_list))
                .perform(actionOnItemAtPosition(0, longClick()));

        onView(withText(R.string.context_menu_log_remove))
                .perform(click());

        onView(withId(R.id.empty_view))
                .check(matches(isDisplayed()));
    }

    private void waitForIdle() {
        dbHelper.waitForIdle(activityRule.getScenario());
    }
}
