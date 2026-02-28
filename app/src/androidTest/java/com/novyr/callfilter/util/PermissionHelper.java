package com.novyr.callfilter.util;

import android.Manifest;
import android.os.Build;

import androidx.test.rule.GrantPermissionRule;
import androidx.test.uiautomator.UiDevice;
import androidx.test.uiautomator.UiObject;
import androidx.test.uiautomator.UiObjectNotFoundException;
import androidx.test.uiautomator.UiSelector;

import java.util.ArrayList;
import java.util.List;

public class PermissionHelper {
    private PermissionHelper() {
    }

    public static GrantPermissionRule grantAllPermissions() {
        List<String> permissions = new ArrayList<>();

        permissions.add(Manifest.permission.READ_CONTACTS);

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            permissions.add(Manifest.permission.CALL_PHONE);
            permissions.add(Manifest.permission.READ_PHONE_STATE);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                permissions.add(Manifest.permission.ANSWER_PHONE_CALLS);
                permissions.add(Manifest.permission.READ_CALL_LOG);
            }
        }

        return GrantPermissionRule.grant(permissions.toArray(new String[0]));
    }

    public static void grantCallScreeningRole(UiDevice device) throws UiObjectNotFoundException {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            return;
        }

        // Try finding the list using the AOSP package name
        UiObject listView = device.findObject(new UiSelector()
                .resourceId("com.android.permissioncontroller:id/list"));

        // If not found, try the Google package name (Common on API 30+ Play images)
        if (!listView.waitForExists(1000)) {
            listView = device.findObject(new UiSelector()
                    .resourceId("com.google.android.permissioncontroller:id/list"));
        }

        // If still not found, try a generic class match (Last resort fallback)
        if (!listView.exists()) {
            listView = device.findObject(new UiSelector()
                    .className("android.widget.ListView"));
        }

        if (!listView.waitForExists(3000)) {
            // Dialog didn't appear or we couldn't find it
            return;
        }

        // Find the app by text "Call Filter" (Ensure your emulator is in English)
        UiObject callFilterTitle = listView.getChild(new UiSelector()
                .textContains("Call Filter"));

        if (callFilterTitle.waitForExists(2000)) {
            callFilterTitle.click();
        }

        // Click "Set as default"
        // Note: The ID "android:id/button1" is standard and safe to use
        UiObject setDefaultButton = device.findObject(new UiSelector()
                .resourceId("android:id/button1")
                .enabled(true));

        if (setDefaultButton.waitForExists(2000)) {
            setDefaultButton.click();
        }
    }
}
