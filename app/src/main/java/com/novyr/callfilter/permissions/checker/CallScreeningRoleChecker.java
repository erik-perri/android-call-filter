package com.novyr.callfilter.permissions.checker;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.role.RoleManager;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;

import com.novyr.callfilter.BuildConfig;

import static android.app.role.RoleManager.ROLE_CALL_SCREENING;
import static com.novyr.callfilter.permissions.PermissionChecker.PERMISSION_CHECKER_REQUEST;

@RequiresApi(api = Build.VERSION_CODES.Q)
public class CallScreeningRoleChecker implements CheckerInterface {
    private static final String TAG = CallScreeningRoleChecker.class.getSimpleName();

    @Override
    public boolean hasAccess(Activity activity) {
        RoleManager roleManager = (RoleManager) activity.getSystemService(Activity.ROLE_SERVICE);
        if (roleManager == null || !roleManager.isRoleAvailable(ROLE_CALL_SCREENING)) {
            if (BuildConfig.DEBUG) {
                Log.w(TAG, String.format("Role %s is not available", ROLE_CALL_SCREENING));
            }
            return false;
        }

        return roleManager.isRoleHeld(ROLE_CALL_SCREENING);
    }

    @Override
    public boolean requestAccess(Activity activity, boolean forceAttempt) {
        @SuppressLint("WrongConstant")
        RoleManager roleManager = (RoleManager) activity.getSystemService(Activity.ROLE_SERVICE);
        if (roleManager == null || !roleManager.isRoleAvailable(ROLE_CALL_SCREENING)) {
            if (BuildConfig.DEBUG) {
                Log.w(TAG, String.format("Role %s is not available", ROLE_CALL_SCREENING));
            }
            return false;
        }

        if (forceAttempt || !roleManager.isRoleHeld(ROLE_CALL_SCREENING)) {
            Intent intent = roleManager.createRequestRoleIntent(ROLE_CALL_SCREENING);
            activity.startActivityForResult(intent, PERMISSION_CHECKER_REQUEST);
            return true;
        }

        return false;
    }
}
