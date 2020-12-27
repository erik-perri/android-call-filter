package com.novyr.callfilter;

import android.os.Build;
import android.telecom.Connection;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

public class CallDetails {
    private static final String TAG = CallDetails.class.getSimpleName();

    @Nullable
    private final String mPhoneNumber;
    private final int mNetworkVerificationStatus;

    public CallDetails(@Nullable String phoneNumber, int networkVerificationStatus) {
        mPhoneNumber = phoneNumber;
        mNetworkVerificationStatus = networkVerificationStatus;
    }

    public CallDetails(@Nullable String phoneNumber) {
        this(phoneNumber, 0 /* Connection.VERIFICATION_STATUS_NOT_VERIFIED */);
    }

    @Nullable
    public String getPhoneNumber() {
        return mPhoneNumber;
    }

    @RequiresApi(api = Build.VERSION_CODES.R)
    public boolean isNotVerified() {
        if (mNetworkVerificationStatus != Connection.VERIFICATION_STATUS_FAILED &&
                mNetworkVerificationStatus != Connection.VERIFICATION_STATUS_PASSED &&
                mNetworkVerificationStatus != Connection.VERIFICATION_STATUS_NOT_VERIFIED) {
            Log.w(
                    TAG,
                    String.format("Unexpected verification status %d", mNetworkVerificationStatus)
            );
        }

        return mNetworkVerificationStatus != Connection.VERIFICATION_STATUS_PASSED &&
                mNetworkVerificationStatus != Connection.VERIFICATION_STATUS_FAILED;
    }

    @RequiresApi(api = Build.VERSION_CODES.R)
    public boolean isVerificationPassed() {
        return mNetworkVerificationStatus == Connection.VERIFICATION_STATUS_PASSED;
    }

    @RequiresApi(api = Build.VERSION_CODES.R)
    public boolean isVerificationFailed() {
        return mNetworkVerificationStatus == Connection.VERIFICATION_STATUS_FAILED;
    }
}
