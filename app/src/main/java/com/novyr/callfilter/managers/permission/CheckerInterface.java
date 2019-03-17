package com.novyr.callfilter.managers.permission;

import android.app.Activity;

public interface CheckerInterface {
    /**
     * @param activity The application activity
     * @return Whether the checker has the access it requires
     */
    boolean hasAccess(Activity activity);

    /**
     * @param activity     The application activity
     * @param forceAttempt Whether to force the attempt event if it looks like we have access
     */
    void requestAccess(Activity activity, boolean forceAttempt);
}
