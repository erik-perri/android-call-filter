package com.novyr.callfilter.permissions;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.List;

public class AndroidCapabilityResolver implements CapabilityResolver {
    private final Context mContext;

    public AndroidCapabilityResolver(Context context) {
        mContext = context.getApplicationContext();
    }

    @Override
    public boolean isActive(Capability capability) {
        return isAvailableOnThisDevice(capability) && isGranted(capability);
    }

    public boolean isAvailableOnThisDevice(Capability capability) {
        switch (capability) {
            case CONTACTS_MATCHING:
                return true;
            case HANG_UP:
                // TODO Figure out the Build.VERSION floor, if there is one.
                return true;
        }

        return false;
    }

    public boolean isGranted(Capability capability) {
        return missingPermissions(mContext, capability).length == 0;
    }

    /**
     * The runtime permissions the capability needs on this device that are not currently granted.
     * Used both to decide whether a capability is active and as the exact list an in-context
     * permission request should ask for.
     */
    public static String[] missingPermissions(Context context, Capability capability) {
        List<String> missing = new ArrayList<>();
        for (String permission : requiredPermissions(capability)) {
            if (ContextCompat.checkSelfPermission(context, permission)
                    != PackageManager.PERMISSION_GRANTED) {
                missing.add(permission);
            }
        }

        return missing.toArray(new String[0]);
    }

    private static String[] requiredPermissions(Capability capability) {
        switch (capability) {
            case CONTACTS_MATCHING:
                return new String[]{Manifest.permission.READ_CONTACTS};
            case HANG_UP:
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    // ANSWER_PHONE_CALLS drives acceptRingingCall()/endCall(). READ_PHONE_STATE is
                    // what gets the PHONE_STATE ringing broadcast delivered to CallReceiver for
                    // the answer handoff.
                    return new String[]{
                            Manifest.permission.ANSWER_PHONE_CALLS,
                            Manifest.permission.READ_PHONE_STATE,
                    };
                }

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    // Already part of the required launch permissions on P (used for endCall);
                    // listed so the capability reads as inactive if it was never granted.
                    return new String[]{Manifest.permission.ANSWER_PHONE_CALLS};
                }

                // <=27 answers via ITelephony reflection so nothing beyond the required set.
                return new String[0];
        }

        return new String[0];
    }
}
