package com.novyr.callfilter.managers;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class PermissionManager {
    private Activity mActivity;

    /**
     * Permissions needed for the App to run
     */
    private List<String> mRequiredPermissions;

    /**
     * Permissions not yet granted
     */
    private ArrayList<String> mNeededPermissions;

    public PermissionManager(Activity activity) {
        this.mActivity = activity;
        this.mRequiredPermissions = new LinkedList<>(Arrays.asList(
                Manifest.permission.CALL_PHONE,
                Manifest.permission.READ_CONTACTS,
                Manifest.permission.READ_PHONE_STATE
        ));

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            mRequiredPermissions.add(Manifest.permission.READ_CALL_LOG);
        }

        this.mNeededPermissions = new ArrayList<>();
    }

    public boolean hasRequiredPermissions() {
        for (String item : mRequiredPermissions) {
            if (ContextCompat.checkSelfPermission(mActivity, item) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    public boolean shouldRequestPermissions() {
        return shouldRequestPermissions(false);
    }

    public boolean shouldRequestPermissions(boolean forceAttempt) {
        this.mNeededPermissions.clear();

        for (String item : mRequiredPermissions) {
            if (forceAttempt || ContextCompat.checkSelfPermission(mActivity, item) != PackageManager.PERMISSION_GRANTED) {
                if (forceAttempt || !ActivityCompat.shouldShowRequestPermissionRationale(mActivity, item)) {
                    this.mNeededPermissions.add(item);
                }
            }
        }

        return this.mNeededPermissions.size() > 0;
    }

    public void requestPermissions(int requestCode) {
        if (this.mNeededPermissions.size() < 1) {
            return;
        }

        String[] permissions = this.mNeededPermissions.toArray(new String[this.mNeededPermissions.size()]);
        ActivityCompat.requestPermissions(this.mActivity, permissions, requestCode);
    }

    public void requestPermissions() {
        requestPermissions(0);
    }
}
