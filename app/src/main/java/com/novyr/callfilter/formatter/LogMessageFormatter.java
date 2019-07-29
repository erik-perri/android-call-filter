package com.novyr.callfilter.formatter;

import android.content.Context;
import android.os.Build;
import android.telephony.PhoneNumberUtils;

import com.novyr.callfilter.ContactFinder;
import com.novyr.callfilter.db.entity.LogEntity;

import java.util.Locale;

public class LogMessageFormatter implements MessageFormatter {
    private final ContactFinder mContactFinder;

    public LogMessageFormatter(Context context) {
        mContactFinder = new ContactFinder(context);
    }

    public String formatMessage(LogEntity entity) {
        String action;
        switch (entity.getAction()) {
            case BLOCKED:
                action = "Blocked call";
                break;
            case ALLOWED:
                action = "Allowed call";
                break;
            case FAILED:
                action = "Failed to block call";
                break;
            default:
                action = String.format("Unknown (%s)", entity.getAction().getCode());
                break;
        }

        String number = entity.getNumber();

        if (number != null) {
            String formatted = formatNumber(number);
            if (formatted != null) {
                number = formatted;
            }
        }

        return String.format("%s: %s", action, number);
    }

    private String formatNumber(String number) {
        if (number == null) {
            return "Private";
        }

        try {
            String contactName = mContactFinder.findContactName(number);
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
