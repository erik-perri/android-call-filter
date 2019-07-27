package com.novyr.callfilter.managers.permission;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.role.RoleManager;
import android.content.Intent;
import android.os.Build;

import androidx.annotation.RequiresApi;

@RequiresApi(api = Build.VERSION_CODES.Q)
public class CallScreeningRoleChecker implements CheckerInterface {
    public static final int CALL_SCREENING_REQUEST = 1;
    private static final String CALL_SCREEN_APP_ROLE = "android.app.role.CALL_SCREENING";

    @Override
    public boolean hasAccess(Activity activity) {
        @SuppressLint("WrongConstant")
        RoleManager roleManager = (RoleManager) activity.getSystemService(Activity.ROLE_SERVICE);
        if (roleManager == null || !roleManager.isRoleAvailable(CALL_SCREEN_APP_ROLE)) {
            return false;
        }

        return roleManager.isRoleAvailable(CALL_SCREEN_APP_ROLE) && roleManager.isRoleHeld(CALL_SCREEN_APP_ROLE);
    }

    @Override
    public void requestAccess(Activity activity, boolean forceAttempt) {
        @SuppressLint("WrongConstant")
        RoleManager roleManager = (RoleManager) activity.getSystemService(Activity.ROLE_SERVICE);
        if (roleManager == null || !roleManager.isRoleAvailable(CALL_SCREEN_APP_ROLE)) {
            return;
        }

        if (forceAttempt || !roleManager.isRoleHeld(CALL_SCREEN_APP_ROLE)) {
            Intent intent = roleManager.createRequestRoleIntent(CALL_SCREEN_APP_ROLE);
            activity.startActivityForResult(intent, CALL_SCREENING_REQUEST);
        }
    }
}
