package com.novyr.callfilter.managers;

import android.app.Activity;
import android.os.Build;

import com.novyr.callfilter.CallFilterApplication;
import com.novyr.callfilter.managers.permission.CallScreeningRoleChecker;
import com.novyr.callfilter.managers.permission.CheckerInterface;
import com.novyr.callfilter.managers.permission.PermissionsChecker;

import java.util.LinkedList;
import java.util.List;

public class PermissionManager {
    private List<CheckerInterface> mCheckers;

    public PermissionManager() {
        this.mCheckers = new LinkedList<>();

        if (Build.VERSION.SDK_INT >= CallFilterApplication.Q) {
            this.mCheckers.add(new CallScreeningRoleChecker());
        }

        this.mCheckers.add(new PermissionsChecker());
    }

    public boolean hasAccess(Activity activity) {
        for (CheckerInterface checker : mCheckers) {
            if (!checker.hasAccess(activity)) {
                return false;
            }
        }

        return true;
    }

    public void requestAccess(Activity activity, boolean forceRequest) {
        for (CheckerInterface checker : mCheckers) {
            checker.requestAccess(activity, forceRequest);
        }
    }
}
