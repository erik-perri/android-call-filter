package com.novyr.callfilter.permissions;

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

import java.util.List;

import static org.junit.Assert.fail;

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
    private String permissionDeniedText;
    private String retryText;

    @Before
    public void setUp() {
        device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation());

        Context targetContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
        permissionDeniedText = targetContext.getString(R.string.permission_request_denied);
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
    public void onStartRequestsPermissions() {
        UiObject2 dialog = findPermissionDialog();
        assertNotNullWithDump("System permission dialog should appear on launch", dialog);
    }

    @Test
    public void permissionDeniedShowsSnackBar() {
        UiObject2 dialog = findPermissionDialog();
        assertNotNullWithDump("System permission dialog should appear", dialog);

        denyAllPermissionDialogs();

        // On Q+, after denying runtime permissions, the call screening role dialog
        // appears next. Dismiss it so the PermissionChecker flow finishes.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            dismissCallScreeningDialogIfPresent();
        }

        UiObject2 snackbar = device.wait(
                Until.findObject(By.textContains(permissionDeniedText)),
                SNACKBAR_TIMEOUT_MS);
        assertNotNullWithDump(
                "Snackbar should contain '" + permissionDeniedText + "'", snackbar);
    }

    @Test
    public void snackBarRetryRequestsAgain() {
        UiObject2 dialog = findPermissionDialog();
        assertNotNullWithDump("System permission dialog should appear", dialog);

        denyAllPermissionDialogs();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            dismissCallScreeningDialogIfPresent();
        }

        UiObject2 retryButton = device.wait(
                Until.findObject(By.text(retryText)),
                SNACKBAR_TIMEOUT_MS);
        assertNotNullWithDump("Snackbar RETRY button should appear", retryButton);

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

        while (findPermissionDialogWithTimeout(SHORT_TIMEOUT_MS) != null) {
            clickDenyOnce();
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

    private void dismissCallScreeningDialogIfPresent() {
        UiObject2 cancelButton = device.wait(
                Until.findObject(By.res("android:id/button2")), 3000);
        if (cancelButton != null) {
            cancelButton.click();
            return;
        }

        device.pressBack();
    }
}
