package com.novyr.callfilter.permissions.checker;

import android.app.Activity;

import androidx.annotation.Nullable;

import java.util.List;

public interface CheckerInterface {
    /**
     * @param activity The application activity
     * @return Whether the checker has the access it requires
     */
    boolean hasAccess(Activity activity);

    /**
     * @param activity     The application activity
     * @param forceAttempt Whether to force the attempt event if it looks like we have access
     * @return Whether a request was made that needs to be handled before continuing
     */
    boolean requestAccess(Activity activity, boolean forceAttempt);
}
