package com.novyr.callfilter;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.novyr.callfilter.db.WhitelistRepository;
import com.novyr.callfilter.db.entity.WhitelistEntity;

import androidx.preference.PreferenceManager;

class CallChecker {
    private static final String TAG = CallChecker.class.getSimpleName();

    private final SharedPreferences mSharedPref;
    private final ContactFinder mContactFinder;
    private final WhitelistRepository mWhitelistRepository;

    CallChecker(Context context) {
        mSharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        mContactFinder = new ContactFinder(context);
        mWhitelistRepository = ((CallFilterApplication) context.getApplicationContext()).getWhitelistRepository();
    }

    boolean shouldBlockCall(String number) {
        if (number == null) {
            return mSharedPref.getBoolean("block_private", false);
        }

        boolean blockUnknown = mSharedPref.getBoolean("block_unknown", false);

        return blockUnknown && !isNumberInContacts(number) && !isNumberInWhitelist(number);
    }

    private boolean isNumberInWhitelist(String number) {
        WhitelistEntity whitelistEntity = mWhitelistRepository.findByNumber(number);
        return whitelistEntity != null;
    }

    private boolean isNumberInContacts(String number) {
        if (number == null) {
            return false;
        }

        try {
            return mContactFinder.findContactName(number) != null;
        } catch (Exception e) {
            Log.d(TAG, "Failed to lookup phone number in contacts", e);

            // If we failed to check we to return true to prevent blocking anything
            return true;
        }
    }
}
