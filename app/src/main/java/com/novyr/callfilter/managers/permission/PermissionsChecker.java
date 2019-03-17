package com.novyr.callfilter.managers.permission;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

import com.novyr.callfilter.CallFilterApplication;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class PermissionsChecker implements CheckerInterface {
    private List<String> mWantedPermissions;

    public PermissionsChecker() {
        if (Build.VERSION.SDK_INT >= CallFilterApplication.Q) {
            mWantedPermissions = new LinkedList<>(Collections.singletonList(Manifest.permission.READ_CONTACTS));
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            mWantedPermissions = new LinkedList<>(Arrays.asList(
                    Manifest.permission.ANSWER_PHONE_CALLS,
                    Manifest.permission.CALL_PHONE,
                    Manifest.permission.READ_CALL_LOG,
                    Manifest.permission.READ_CONTACTS,
                    Manifest.permission.READ_PHONE_STATE
            ));
        } else {
            mWantedPermissions = new LinkedList<>(Arrays.asList(
                    Manifest.permission.CALL_PHONE,
                    Manifest.permission.READ_CONTACTS,
                    Manifest.permission.READ_PHONE_STATE
            ));
        }
    }

    public boolean hasAccess(Activity activity) {
        for (String item : mWantedPermissions) {
            if (ContextCompat.checkSelfPermission(activity, item) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }

        return true;
    }

    public void requestAccess(Activity activity, boolean forceAttempt) {
        String[] neededPermissions = findNeededPermissions(activity, forceAttempt);
        if (neededPermissions.length < 1) {
            return;
        }

        ActivityCompat.requestPermissions(activity, neededPermissions, 0);
    }

    private String[] findNeededPermissions(Activity activity, boolean forceRequest) {
        List<String> neededPermissions = new ArrayList<>();

        for (String item : mWantedPermissions) {
            if (forceRequest) {
                neededPermissions.add(item);
                continue;
            }

            if (ContextCompat.checkSelfPermission(activity, item) != PackageManager.PERMISSION_GRANTED) {
                if (!ActivityCompat.shouldShowRequestPermissionRationale(activity, item)) {
                    neededPermissions.add(item);
                }
            }
        }

        return neededPermissions.toArray(new String[0]);
    }
}
