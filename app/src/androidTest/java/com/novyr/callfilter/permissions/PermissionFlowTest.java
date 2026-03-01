package com.novyr.callfilter.permissions;

import android.app.Instrumentation;
import android.os.Build;
import android.os.ParcelFileDescriptor;

import androidx.test.core.app.ActivityScenario;
import androidx.test.filters.MediumTest;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.uiautomator.By;
import androidx.test.uiautomator.UiDevice;
import androidx.test.uiautomator.UiObject2;
import androidx.test.uiautomator.Until;

import com.novyr.callfilter.R;
import com.novyr.callfilter.ui.loglist.LogListActivity;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.junit.Assert.assertNotNull;

/**
 * Tests the permission request flow when permissions are NOT pre-granted.
 *
 * Does NOT use GrantPermissionRule. Instead, revokes permissions before each test
 * and launches the activity manually so the system permission dialog appears.
 */
@MediumTest
public class PermissionFlowTest {
    private static final long DIALOG_TIMEOUT_MS = 5000;
    private static final long SHORT_TIMEOUT_MS = 1500;
    private static final String PACKAGE = "com.novyr.callfilter";

    private UiDevice device;
    private ActivityScenario<LogListActivity> scenario;

    @Before
    public void setUp() {
        Instrumentation instrumentation = InstrumentationRegistry.getInstrumentation();
        device = UiDevice.getInstance(instrumentation);

        // Revoke runtime permissions so the dialog will appear on launch
        revokePermissions(instrumentation);

        // Clear the permission SharedPreferences so AndroidPermissionChecker doesn't
        // think permissions are "blocked" (previously requested + denied + don't ask again)
        clearPermissionPreferences(instrumentation);

        // On Q+, also remove the call screening role so it doesn't interfere
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            shellCommand(instrumentation,
                    "cmd role remove-role-holder android.app.role.CALL_SCREENING " + PACKAGE);
        }

        // Now launch the activity — permissions are revoked, so onStart() will
        // trigger the permission dialog
        scenario = ActivityScenario.launch(LogListActivity.class);
    }

    @After
    public void tearDown() {
        if (scenario != null) {
            scenario.close();
        }

        // Dismiss any lingering system dialogs
        device.pressBack();
    }

    @Test
    public void onStartRequestsPermissions() {
        // The activity's onStart triggers PermissionChecker.onStart(), which requests
        // runtime permissions. A system permission dialog should appear.
        UiObject2 dialog = findPermissionDialog();
        assertNotNull("System permission dialog should appear on launch", dialog);
    }

    @Test
    public void permissionDeniedShowsSnackBar() {
        // Wait for the permission dialog, then deny
        UiObject2 dialog = findPermissionDialog();
        assertNotNull("System permission dialog should appear", dialog);

        // Deny all permission dialogs — the system may show one per permission group
        // (e.g. PHONE, CONTACTS, CALL_LOG on pre-Q)
        denyAllPermissionDialogs();

        // On Q+, after denying runtime permissions, the call screening role dialog
        // appears next. Dismiss it so the PermissionChecker flow finishes.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            dismissCallScreeningDialogIfPresent();
        }

        // The Snackbar should appear with the denied message
        onView(withText(R.string.permission_request_denied))
                .check(matches(isDisplayed()));
    }

    @Test
    public void snackBarRetryRequestsAgain() {
        // Deny the initial permission dialog(s)
        UiObject2 dialog = findPermissionDialog();
        assertNotNull("System permission dialog should appear", dialog);

        denyAllPermissionDialogs();

        // Dismiss call screening dialog if present (Q+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            dismissCallScreeningDialogIfPresent();
        }

        // Verify Snackbar is shown, then click RETRY
        onView(withText(R.string.permission_notice_retry))
                .check(matches(isDisplayed()));

        onView(withText(R.string.permission_notice_retry))
                .perform(click());

        // Permission dialog should reappear
        UiObject2 retryDialog = findPermissionDialog();
        assertNotNull("Permission dialog should reappear after RETRY", retryDialog);
    }

    private void revokePermissions(Instrumentation instrumentation) {
        String[] permissions = {
                "android.permission.READ_CONTACTS",
                "android.permission.CALL_PHONE",
                "android.permission.READ_PHONE_STATE",
                "android.permission.ANSWER_PHONE_CALLS",
                "android.permission.READ_CALL_LOG",
        };

        for (String permission : permissions) {
            shellCommand(instrumentation, "pm revoke " + PACKAGE + " " + permission);
        }
    }

    private void clearPermissionPreferences(Instrumentation instrumentation) {
        shellCommand(instrumentation,
                "run-as " + PACKAGE + " rm -f /data/data/" + PACKAGE
                        + "/shared_prefs/permissions.xml");
    }

    private void shellCommand(Instrumentation instrumentation, String command) {
        try {
            ParcelFileDescriptor pfd = instrumentation.getUiAutomation()
                    .executeShellCommand(command);
            pfd.close();
        } catch (IOException e) {
            // Ignore — some commands may fail on certain API levels
        }
    }

    /**
     * Finds the system permission dialog by looking for common button text patterns.
     */
    private UiObject2 findPermissionDialog() {
        return findPermissionDialogWithTimeout(DIALOG_TIMEOUT_MS);
    }

    private UiObject2 findPermissionDialogWithTimeout(long timeout) {
        UiObject2 button = device.wait(
                Until.findObject(By.text("Allow")), timeout);
        if (button != null) {
            return button;
        }

        button = device.wait(
                Until.findObject(By.text("While using the app")), SHORT_TIMEOUT_MS);
        if (button != null) {
            return button;
        }

        return device.wait(
                Until.findObject(By.textContains("ALLOW")), SHORT_TIMEOUT_MS);
    }

    /**
     * Denies all permission dialogs. The system may show one dialog per permission
     * group (e.g. PHONE, CONTACTS, CALL_LOG are separate groups on API 28).
     * Loops until no more permission dialogs appear.
     */
    private void denyAllPermissionDialogs() {
        // Click deny on the first dialog (already confirmed present by caller)
        clickDenyOnce();

        // Keep denying as long as more permission dialogs appear
        while (findPermissionDialogWithTimeout(SHORT_TIMEOUT_MS) != null) {
            clickDenyOnce();
        }
    }

    /**
     * Clicks the deny/don't allow button on the currently visible permission dialog.
     */
    private void clickDenyOnce() {
        UiObject2 denyButton = device.wait(
                Until.findObject(By.text("Deny")), SHORT_TIMEOUT_MS);
        if (denyButton != null) {
            denyButton.click();
            return;
        }

        denyButton = device.wait(
                Until.findObject(By.text("Don't allow")), SHORT_TIMEOUT_MS);
        if (denyButton != null) {
            denyButton.click();
            return;
        }

        denyButton = device.wait(
                Until.findObject(By.textContains("DENY")), SHORT_TIMEOUT_MS);
        if (denyButton != null) {
            denyButton.click();
        }
    }

    /**
     * Dismisses the call screening role dialog if it appears (Q+ only).
     * On Q+, after the permission dialog, PermissionChecker moves to
     * CallScreeningRoleChecker which shows the role selection dialog.
     */
    private void dismissCallScreeningDialogIfPresent() {
        // Try the standard cancel button (android:id/button2)
        UiObject2 cancelButton = device.wait(
                Until.findObject(By.res("android:id/button2")), 3000);
        if (cancelButton != null) {
            cancelButton.click();
            return;
        }

        // Fallback: press back to dismiss whatever dialog is showing
        device.pressBack();
    }
}
