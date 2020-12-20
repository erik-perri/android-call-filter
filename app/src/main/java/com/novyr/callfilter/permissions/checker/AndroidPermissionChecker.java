package com.novyr.callfilter.permissions.checker;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.novyr.callfilter.BuildConfig;
import com.novyr.callfilter.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import static com.novyr.callfilter.permissions.PermissionChecker.PERMISSION_CHECKER_REQUEST;

public class AndroidPermissionChecker implements CheckerInterface, CheckerWithErrorsInterface {
    private static final String TAG = AndroidPermissionChecker.class.getSimpleName();

    private final List<String> mErrors;
    private final List<String> mWantedPermissions;

    public AndroidPermissionChecker() {
        mErrors = new LinkedList<>();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            mWantedPermissions = new LinkedList<>(Collections.singletonList(Manifest.permission.READ_CONTACTS));
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            mWantedPermissions = new LinkedList<>(Arrays.asList(
                    Manifest.permission.ANSWER_PHONE_CALLS,
                    Manifest.permission.CALL_PHONE,
                    Manifest.permission.READ_CALL_LOG,
                    Manifest.permission.READ_CONTACTS,
                    Manifest.permission.READ_PHONE_STATE
            ));
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mWantedPermissions = new LinkedList<>(Arrays.asList(
                    Manifest.permission.CALL_PHONE,
                    Manifest.permission.READ_CONTACTS,
                    Manifest.permission.READ_PHONE_STATE
            ));
        } else {
            mWantedPermissions = new LinkedList<>(Arrays.asList(
                    Manifest.permission.CALL_PHONE,
                    Manifest.permission.READ_CONTACTS,
                    Manifest.permission.READ_PHONE_STATE,
                    Manifest.permission.MODIFY_PHONE_STATE
            ));
        }

        if (BuildConfig.DEBUG) {
            Log.i(TAG, String.format("Permissions wanted: %d", mWantedPermissions.size()));
            for (int i = 0; i < mWantedPermissions.size(); i++) {
                Log.i(TAG, String.format(" - %s", mWantedPermissions.get(i)));
            }
        }
    }

    @Override
    public List<String> getErrors() {
        return mErrors;
    }

    private SharedPreferences getSharedPreferences(Context context) {
        return context.getSharedPreferences(
                context.getString(R.string.permission_preferences_file),
                Context.MODE_PRIVATE
        );
    }

    private boolean hasRequested(Context context, String permission) {
        return getSharedPreferences(context).getBoolean(
                String.format("requested-%s", permission),
                false
        );
    }

    private void setRequested(Context context, String permission) {
        getSharedPreferences(context).edit()
                                     .putBoolean(String.format("requested-%s", permission), true)
                                     .apply();
    }

    public boolean hasAccess(Activity activity) {
        for (String item : mWantedPermissions) {
            if (ContextCompat.checkSelfPermission(
                    activity,
                    item
            ) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }

        return true;
    }

    public boolean requestAccess(Activity activity, boolean forceAttempt) {
        mErrors.clear();

        PermissionResults permissions = findNeededPermissions(activity, forceAttempt);

        if (permissions.mBlockedPermissions.size() > 0) {
            mErrors.add(activity.getString(R.string.permission_request_blocked));
        }

        if (permissions.mNeededPermissions.size() > 0) {
            ActivityCompat.requestPermissions(
                    activity,
                    permissions.mNeededPermissions.toArray(new String[0]),
                    PERMISSION_CHECKER_REQUEST
            );

            for (String permission : permissions.mNeededPermissions) {
                setRequested(activity, permission);
            }

            return true;
        }


        return false;
    }

    private PermissionResults findNeededPermissions(Activity activity, boolean forceRequest) {
        List<String> neededPermissions = new ArrayList<>();
        List<String> blockedPermissions = new ArrayList<>();

        for (String permission : mWantedPermissions) {
            if (forceRequest) {
                neededPermissions.add(permission);
                continue;
            }

            if (ContextCompat.checkSelfPermission(
                    activity,
                    permission
            ) != PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)) {
                    neededPermissions.add(permission);
                } else {
                    if (!hasRequested(activity, permission)) {
                        neededPermissions.add(permission);
                    } else {
                        blockedPermissions.add(permission);
                    }
                }
            }
        }

        if (BuildConfig.DEBUG) {
            Log.i(TAG, String.format("Permissions needed: %d", neededPermissions.size()));
            for (int i = 0; i < neededPermissions.size(); i++) {
                Log.i(TAG, String.format(" - %s", neededPermissions.get(i)));
            }

            Log.i(TAG, String.format("Permissions blocked: %d", blockedPermissions.size()));
            for (int i = 0; i < blockedPermissions.size(); i++) {
                Log.i(TAG, String.format(" - %s", blockedPermissions.get(i)));
            }
        }

        return new PermissionResults(neededPermissions, blockedPermissions);
    }

    private static class PermissionResults {
        private final List<String> mNeededPermissions;
        private final List<String> mBlockedPermissions;

        PermissionResults(List<String> neededPermissions, List<String> blockedPermissions) {
            mNeededPermissions = neededPermissions;
            mBlockedPermissions = blockedPermissions;
        }
    }
}
