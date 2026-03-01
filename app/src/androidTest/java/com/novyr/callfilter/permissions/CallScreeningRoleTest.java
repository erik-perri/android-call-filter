package com.novyr.callfilter.permissions;

import android.app.Instrumentation;
import android.os.ParcelFileDescriptor;

import androidx.test.core.app.ActivityScenario;
import androidx.test.filters.MediumTest;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.rule.GrantPermissionRule;
import androidx.test.uiautomator.By;
import androidx.test.uiautomator.UiDevice;
import androidx.test.uiautomator.UiObject2;
import androidx.test.uiautomator.Until;

import com.novyr.callfilter.R;
import com.novyr.callfilter.ui.loglist.LogListActivity;
import com.novyr.callfilter.util.ApiLevelAssumptions;
import com.novyr.callfilter.util.PermissionHelper;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.io.IOException;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.junit.Assert.assertNotNull;

/**
 * Tests the call screening role request flow on Q+ (API 29+).
 *
 * Uses GrantPermissionRule to pre-grant runtime permissions so the only dialog
 * that appears is the call screening role request dialog.
 */
@MediumTest
public class CallScreeningRoleTest {
    private static final long DIALOG_TIMEOUT_MS = 5000;
    private static final String PACKAGE = "com.novyr.callfilter";

    @Rule
    public GrantPermissionRule permissions = PermissionHelper.grantAllPermissions();

    private UiDevice device;
    private ActivityScenario<LogListActivity> scenario;

    @Before
    public void setUp() {
        ApiLevelAssumptions.assumeQOrHigher();

        Instrumentation instrumentation = InstrumentationRegistry.getInstrumentation();
        device = UiDevice.getInstance(instrumentation);

        // Remove the call screening role so the role request dialog will appear
        shellCommand(instrumentation,
                "cmd role remove-role-holder android.app.role.CALL_SCREENING " + PACKAGE);

        // Launch the activity — runtime permissions are granted (via Rule), but the
        // call screening role is not held, so the role dialog should appear
        scenario = ActivityScenario.launch(LogListActivity.class);
    }

    @After
    public void tearDown() {
        if (scenario != null) {
            scenario.close();
        }

        // Dismiss any lingering system dialogs
        if (device != null) {
            device.pressBack();
        }
    }

    @Test
    public void roleRequestShowsDialog() {
        // On Q+, with runtime permissions granted but no call screening role,
        // the PermissionChecker should show the system role request dialog.
        UiObject2 dialog = findRoleDialog();
        assertNotNull("Call screening role request dialog should appear", dialog);
    }

    @Test
    public void roleDeniedShowsSnackBar() {
        // Wait for the role dialog to appear
        UiObject2 dialog = findRoleDialog();
        assertNotNull("Call screening role request dialog should appear", dialog);

        // Cancel/dismiss the dialog
        clickCancel();

        // The Snackbar should appear with the screening denied message
        onView(withText(R.string.permission_screening_denied))
                .check(matches(isDisplayed()));
    }

    /**
     * Finds the call screening role request dialog.
     * Looks for the list view or "Set as default" button that identifies this dialog.
     */
    private UiObject2 findRoleDialog() {
        // Try AOSP permission controller list
        UiObject2 list = device.wait(
                Until.findObject(By.res("com.android.permissioncontroller:id/list")),
                DIALOG_TIMEOUT_MS);
        if (list != null) {
            return list;
        }

        // Try Google permission controller list (common on Play images)
        list = device.wait(
                Until.findObject(By.res("com.google.android.permissioncontroller:id/list")),
                1000);
        if (list != null) {
            return list;
        }

        // Fallback: look for "Set as default" button (android:id/button1)
        UiObject2 setDefault = device.wait(
                Until.findObject(By.res("android:id/button1")),
                1000);
        if (setDefault != null) {
            return setDefault;
        }

        // Last resort: generic ListView
        return device.wait(
                Until.findObject(By.clazz("android.widget.ListView")),
                1000);
    }

    /**
     * Clicks the cancel/negative button on the role request dialog.
     */
    private void clickCancel() {
        // Try the standard negative button (android:id/button2)
        UiObject2 cancelButton = device.wait(
                Until.findObject(By.res("android:id/button2")),
                DIALOG_TIMEOUT_MS);
        if (cancelButton != null) {
            cancelButton.click();
            return;
        }

        // Fallback: press back to dismiss the dialog
        device.pressBack();
    }

    private void shellCommand(Instrumentation instrumentation, String command) {
        try {
            ParcelFileDescriptor pfd = instrumentation.getUiAutomation()
                    .executeShellCommand(command);
            pfd.close();
        } catch (IOException e) {
            // Ignore — command may fail if role was never assigned
        }
    }
}
