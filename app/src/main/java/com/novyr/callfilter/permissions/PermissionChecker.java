package com.novyr.callfilter.permissions;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;

import com.novyr.callfilter.BuildConfig;
import com.novyr.callfilter.R;
import com.novyr.callfilter.permissions.checker.AndroidPermissionChecker;
import com.novyr.callfilter.permissions.checker.CallScreeningRoleChecker;
import com.novyr.callfilter.permissions.checker.CheckerInterface;
import com.novyr.callfilter.permissions.checker.CheckerWithErrorsInterface;

import java.util.LinkedList;
import java.util.List;

import static android.content.pm.PackageManager.PERMISSION_DENIED;

public class PermissionChecker {
    private static final String TAG = PermissionChecker.class.getSimpleName();

    public static final int PERMISSION_CHECKER_REQUEST = 250;
    private final LinkedList<CheckerInterface> mCheckers;
    private final List<String> mErrors;
    private final Activity mActivity;
    private final NotificationHandlerInterface mNotificationHandler;
    private int mIndex = 0;

    public PermissionChecker(Activity activity, NotificationHandlerInterface notificationHandler) {
        mActivity = activity;
        mNotificationHandler = notificationHandler;
        mCheckers = new LinkedList<>();
        mErrors = new LinkedList<>();

        mCheckers.add(new AndroidPermissionChecker());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            mCheckers.add(new CallScreeningRoleChecker());
        }
    }

    public void onStart() {
        mIndex = -1;
        mErrors.clear();

        mNotificationHandler.setErrors(new LinkedList<>());
        checkNext();
    }

    private void onFinished() {
        mNotificationHandler.setErrors(mErrors);
    }

    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        CheckerInterface checker = mCheckers.get(mIndex);
        if (requestCode != PERMISSION_CHECKER_REQUEST || checker.getClass() != AndroidPermissionChecker.class) {
            if (BuildConfig.DEBUG) {
                Log.w(TAG, String.format("Unexpected onRequestPermissionsResult call, requestCode: %d, checker: %s", requestCode, checker.getClass().getSimpleName()));
            }
            return;
        }

        if (!wereAllPermissionsGranted(grantResults)) {
            mErrors.add(mActivity.getString(R.string.permission_request_denied));
        }

        checkNext();
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        CheckerInterface checker = mCheckers.get(mIndex);
        if (requestCode != PERMISSION_CHECKER_REQUEST || checker.getClass() != CallScreeningRoleChecker.class) {
            if (BuildConfig.DEBUG) {
                Log.w(TAG, String.format("Unexpected onActivityResult call, requestCode: %d, checker: %s", requestCode, checker.getClass().getSimpleName()));
            }
            return;
        }

        if (resultCode != android.app.Activity.RESULT_OK) {
            mErrors.add(mActivity.getString(R.string.permission_screening_denied));
        }

        checkNext();
    }

    private void checkNext() {
        int nextIndex = findNextChecker(mIndex);
        if (nextIndex == -1) {
            onFinished();
            return;
        }

        mIndex = nextIndex;
        CheckerInterface checker = mCheckers.get(mIndex);

        if (checker.hasAccess(mActivity)) {
            checkNext();
            return;
        }

        boolean handled = checker.requestAccess(mActivity, false);

        if (checker instanceof CheckerWithErrorsInterface) {
            List<String> checkerErrors = ((CheckerWithErrorsInterface) checker).getErrors();
            if (checkerErrors != null) {
                mErrors.addAll(checkerErrors);
            }
        }

        if (!handled) {
            checkNext();
        }
    }

    private int findNextChecker(int currentIndex) {
        for (int i = currentIndex + 1; i < mCheckers.size(); i++) {
            CheckerInterface checker = mCheckers.get(i);
            if (!checker.hasAccess(mActivity)) {
                return i;
            }
        }

        return -1;
    }

    private boolean wereAllPermissionsGranted(@NonNull int[] grantResults) {
        for (int grantResult : grantResults) {
            if (grantResult == PERMISSION_DENIED) {
                return false;
            }
        }
        return true;
    }
}
