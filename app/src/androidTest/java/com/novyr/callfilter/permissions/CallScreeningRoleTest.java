package com.novyr.callfilter.permissions;

import android.content.Context;

import androidx.test.core.app.ActivityScenario;
import androidx.test.filters.MediumTest;
import androidx.test.filters.SdkSuppress;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.rule.GrantPermissionRule;
import androidx.test.uiautomator.By;
import androidx.test.uiautomator.UiDevice;
import androidx.test.uiautomator.UiObject2;
import androidx.test.uiautomator.Until;

import static org.junit.Assert.assertNotNull;

import com.novyr.callfilter.R;
import com.novyr.callfilter.ui.loglist.LogListActivity;
import com.novyr.callfilter.util.PermissionHelper;
import com.novyr.callfilter.util.UiDumpOnFailureRule;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.io.IOException;

/**
 * Tests the call screening role request flow on Q+ (API 29+).
 *
 * Uses GrantPermissionRule to pre-grant runtime permissions so the only dialog
 * that appears is the call screening role request dialog. Relies on the Test
 * Orchestrator with clearPackageData so the call screening role is never
 * pre-assigned.
 */
@MediumTest
@SdkSuppress(minSdkVersion = 29)
public class CallScreeningRoleTest {
    private static final long DIALOG_TIMEOUT_MS = 5000;
    private static final long SNACKBAR_TIMEOUT_MS = 5000;

    private final UiDevice device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation());

    @Rule
    public GrantPermissionRule permissions = PermissionHelper.grantAllPermissions();

    @Rule
    public UiDumpOnFailureRule dumpRule = new UiDumpOnFailureRule(device);

    private ActivityScenario<LogListActivity> scenario;
    private String screeningDeniedText;

    @Before
    public void setUp() {

        // clearPackageData does NOT clear system-managed role assignments.
        // Explicitly remove the call screening role so the dialog appears.
        removeCallScreeningRole();

        Context targetContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
        screeningDeniedText = targetContext.getString(R.string.permission_screening_denied);

        // Launch the activity — runtime permissions are granted (via Rule), but the
        // call screening role is not held, so the role dialog should appear.
        scenario = ActivityScenario.launch(LogListActivity.class);
    }

    @After
    public void tearDown() {
        if (scenario != null) {
            scenario.close();
        }

        if (device != null) {
            device.pressBack();
        }
    }

    @Test
    public void roleRequest_onLaunch_showsDialog() {
        UiObject2 dialog = findRoleDialog();
        assertNotNull("Call screening role request dialog should appear", dialog);
    }

    @Test
    public void roleRequest_denied_showsSnackBar() {
        UiObject2 dialog = findRoleDialog();
        assertNotNull("Call screening role request dialog should appear", dialog);

        clickCancel();

        UiObject2 snackbar = device.wait(
                Until.findObject(By.textContains(screeningDeniedText)),
                SNACKBAR_TIMEOUT_MS);
        assertNotNull(
                "Snackbar should contain '" + screeningDeniedText + "'", snackbar);
    }

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

    private void clickCancel() {
        UiObject2 cancelButton = device.wait(
                Until.findObject(By.res("android:id/button2")),
                DIALOG_TIMEOUT_MS);
        if (cancelButton != null) {
            cancelButton.click();
            return;
        }

        device.pressBack();
    }

    private void removeCallScreeningRole() {
        try {
            device.executeShellCommand(
                    "cmd role remove-role-holder android.app.role.CALL_SCREENING com.novyr.callfilter");
        } catch (IOException e) {
            // Role may not be held — safe to ignore
        }
    }
}
