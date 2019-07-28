package com.novyr.callfilter;

import android.content.Context;
import android.os.Build;
import android.telephony.PhoneNumberUtils;

import java.util.Locale;

public class NumberHelper {
    private final ContactHelper mContactHelper;

    public NumberHelper(Context context) {
        mContactHelper = new ContactHelper(context);
    }

    public String formatNumber(String number) {
        if (number == null) {
            return "Private";
        }

        try {
            String contactName = mContactHelper.findContactName(number);
            if (contactName != null) {
                return contactName;
            }
        } catch (Exception ignored) {
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            return PhoneNumberUtils.formatNumber(number, Locale.getDefault().getCountry());
        } else {
            //noinspection deprecation
            return PhoneNumberUtils.formatNumber(number);
        }
    }
}
