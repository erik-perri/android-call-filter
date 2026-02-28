package com.novyr.callfilter;

import android.os.Build;

import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.filters.MediumTest;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.rule.GrantPermissionRule;
import androidx.test.uiautomator.UiDevice;
import androidx.test.uiautomator.UiObject;
import androidx.test.uiautomator.UiSelector;

import com.novyr.callfilter.ui.loglist.LogListActivity;
import com.novyr.callfilter.util.PermissionHelper;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.not;

@MediumTest
public class SmokeTest {
    @Rule
    public GrantPermissionRule permissions = PermissionHelper.grantAllPermissions();

    @Rule
    public ActivityScenarioRule<LogListActivity> activity =
            new ActivityScenarioRule<>(LogListActivity.class);

    @Before
    public void dismissCallScreeningDialog() throws Exception {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            return;
        }

        UiDevice device = UiDevice.getInstance(
                InstrumentationRegistry.getInstrumentation());

        PermissionHelper.grantCallScreeningRole(device);
    }

    @Test
    public void appLaunches_emptyStateIsVisible() {
        // On a fresh launch with no log entries, the empty view should be visible
        onView(withId(R.id.empty_view))
                .check(matches(isDisplayed()));
        onView(withId(R.id.empty_view))
                .check(matches(withText(R.string.warning_empty_log_list)));

        // And the log list should not be visible
        onView(withId(R.id.log_list))
                .check(matches(not(isDisplayed())));
    }
}
