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

        // The dialog is a ListView inside com.google.android.permissioncontroller.
        // Find the "Call Filter" row by its title TextView inside the list, then
        // click the parent LinearLayout row to select it.
        UiObject listView = device.findObject(new UiSelector()
                .resourceId("com.android.permissioncontroller:id/list"));
        if (!listView.waitForExists(3000)) {
            return;
        }

        UiObject callFilterTitle = listView.getChild(new UiSelector()
                .resourceId("com.android.permissioncontroller:id/title")
                .text("Call Filter"));
        if (callFilterTitle.waitForExists(1000)) {
            // Click the parent row to select the radio button
            callFilterTitle.click();

            // "Set as default" (android:id/button1) becomes enabled after selection
            UiObject setDefaultButton = device.findObject(new UiSelector()
                    .resourceId("android:id/button1")
                    .enabled(true));
            if (setDefaultButton.waitForExists(2000)) {
                setDefaultButton.click();
            }
        }
    }
}
