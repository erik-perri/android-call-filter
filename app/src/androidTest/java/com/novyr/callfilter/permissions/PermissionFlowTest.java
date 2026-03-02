package com.novyr.callfilter.permissions;

import static org.junit.Assert.fail;

import android.content.Context;
import android.os.Build;

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
import java.util.List;

/**
 * Tests the permission request flow when permissions are NOT pre-granted.
 *
 * Does NOT use GrantPermissionRule. Relies on the Test Orchestrator with
 * clearPackageData to ensure each test starts with a fresh app state where
 * no permissions have been granted.
 */
@MediumTest
public class PermissionFlowTest {
    private static final long DIALOG_TIMEOUT_MS = 5000;
    private static final long SHORT_TIMEOUT_MS = 1500;
    private static final long SNACKBAR_TIMEOUT_MS = 5000;

    private UiDevice device;
    private ActivityScenario<LogListActivity> scenario;
    private String retryText;

    @Before
    public void setUp() {
        device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation());

        // On Q+, pre-grant the call screening role so only the runtime permission
        // dialog appears (not the role dialog). This isolates what we're testing
        // and avoids the onStart() re-trigger cycle caused by role dialog dismissal.
        grantCallScreeningRoleIfNeeded();

        Context targetContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
        retryText = targetContext.getString(R.string.permission_notice_retry);

        // Launch the activity — no permissions are granted (clearPackageData ensures
        // a fresh state), so onStart() will trigger the permission dialog.
        scenario = ActivityScenario.launch(LogListActivity.class);
    }

    @After
    public void tearDown() {
        if (scenario != null) {
            scenario.close();
        }

        device.pressBack();
    }

    @Test
    public void permissionCheck_onStart_showsPermissionDialog() {
        UiObject2 dialog = findPermissionDialog();
        assertNotNullWithDump("System permission dialog should appear on launch", dialog);
    }

    @Test
    public void permissionCheck_denied_showsSnackBar() {
        UiObject2 dialog = findPermissionDialog();
        assertNotNullWithDump("System permission dialog should appear", dialog);

        denyAllPermissionDialogs();

        // After denying, onStart() may re-trigger a permission dialog when the
        // GrantPermissionsActivity finishes. Dismiss it so PermissionChecker reaches
        // onFinished() and the Snackbar appears.
        dismissReTriggeredDialogs();

        // The snackbar shows "Permission request denied" or "Permission request
        // blocked" depending on the re-trigger cycle. Both indicate correct
        // behavior. Check for the RETRY button which appears on either message.
        UiObject2 retryButton = device.wait(
                Until.findObject(By.text(retryText)),
                SNACKBAR_TIMEOUT_MS);
        assertNotNullWithDump("Snackbar with RETRY should appear after denying", retryButton);
    }

    @Test
    public void permissionCheck_retryClicked_showsDialogAgain() {
        UiObject2 dialog = findPermissionDialog();
        assertNotNullWithDump("System permission dialog should appear", dialog);

        denyAllPermissionDialogs();

        dismissReTriggeredDialogs();

        UiObject2 retryButton = device.wait(
                Until.findObject(By.text(retryText)),
                SNACKBAR_TIMEOUT_MS);
        assertNotNullWithDump("Snackbar RETRY button should appear", retryButton);

        // The re-trigger cycle may have caused the permission to become "blocked"
        // (shouldShowRequestPermissionRationale returns false). Clear the hasRequested
        // flag so AndroidPermissionChecker treats it as a first-time request and
        // shows the dialog.
        clearPermissionPreferences();

        retryButton.click();

        UiObject2 retryDialog = findPermissionDialog();
        assertNotNullWithDump("Permission dialog should reappear after RETRY", retryDialog);
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private void assertNotNullWithDump(String message, Object object) {
        if (object != null) {
            return;
        }
        StringBuilder sb = new StringBuilder(message);
        sb.append("\n\n=== Screen dump ===\n");
        sb.append("API level: ").append(Build.VERSION.SDK_INT).append("\n");
        sb.append("Current package: ").append(device.getCurrentPackageName()).append("\n\n");
        try {
            List<UiObject2> textNodes = device.findObjects(By.textStartsWith(""));
            if (textNodes != null) {
                for (UiObject2 obj : textNodes) {
                    sb.append("  text=\"").append(obj.getText()).append("\"");
                    sb.append("  class=").append(obj.getClassName());
                    if (obj.getResourceName() != null) {
                        sb.append("  res=").append(obj.getResourceName());
                    }
                    sb.append("\n");
                }
            }
        } catch (Exception e) {
            sb.append("(dump failed: ").append(e.getMessage()).append(")\n");
        }
        fail(sb.toString());
    }

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

    private void denyAllPermissionDialogs() {
        clickDenyOnce();

        // Pre-Q requests multiple permissions (CALL_PHONE, READ_PHONE_STATE,
        // READ_CONTACTS, etc.) which the system shows as separate dialogs per
        // permission group. Deny each one.
        // On Q+ only READ_CONTACTS is requested — a single dialog, no loop needed.
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            while (findPermissionDialogWithTimeout(SHORT_TIMEOUT_MS) != null) {
                clickDenyOnce();
            }
        }
    }

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
     * After denying permissions, the GrantPermissionsActivity finishes and the
     * activity resumes, causing onStart() to re-trigger PermissionChecker. This
     * can show another permission dialog (sometimes with null root nodes that
     * UiAutomator cannot interact with via buttons). Dismiss any such dialogs
     * by checking if a foreign package is in the foreground and pressing back.
     */
    private void dismissReTriggeredDialogs() {
        // Wait for the app to return to the foreground before checking for
        // re-triggered dialogs. On API 29, the permission dialog's dismissal
        // animation is still running when waitForIdle() returns, so
        // getCurrentPackageName() can transiently report the permission
        // controller package. A back press at that moment destroys the activity.
        long deadline = System.currentTimeMillis() + 3000;
        while (System.currentTimeMillis() < deadline
                && !"com.novyr.callfilter".equals(device.getCurrentPackageName())) {
            device.waitForIdle(200);
        }

        for (int i = 0; i < 3; i++) {
            device.waitForIdle(500);

            String currentPkg = device.getCurrentPackageName();
            if ("com.novyr.callfilter".equals(currentPkg)) {
                break;
            }

            // A dialog from another package is on top. It may have null root
            // nodes, making button clicks impossible. Press back to dismiss.
            device.pressBack();
        }
    }

    private void clearPermissionPreferences() {
        Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        context.getSharedPreferences(
                context.getString(R.string.permission_preferences_file),
                Context.MODE_PRIVATE
        ).edit().clear().commit();
    }

    private void grantCallScreeningRoleIfNeeded() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            return;
        }
        try {
            device.executeShellCommand(
                    "cmd role add-role-holder android.app.role.CALL_SCREENING com.novyr.callfilter");
        } catch (IOException e) {
            // Best effort — test will still run, but may see role dialog
        }
    }
}
