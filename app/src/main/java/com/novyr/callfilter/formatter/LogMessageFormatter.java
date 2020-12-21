package com.novyr.callfilter.formatter;

import android.content.res.Resources;
import android.os.Build;
import android.telephony.PhoneNumberUtils;

import androidx.annotation.NonNull;

import com.novyr.callfilter.ContactFinder;
import com.novyr.callfilter.R;
import com.novyr.callfilter.db.entity.LogEntity;

import java.util.Locale;

public class LogMessageFormatter implements MessageFormatter {
    private final Resources mResources;
    private final ContactFinder mContactFinder;

    public LogMessageFormatter(Resources resources, ContactFinder contactFinder) {
        mResources = resources;
        mContactFinder = contactFinder;
    }

    public String formatMessage(LogEntity entity) {
        String action;
        switch (entity.getAction()) {
            case BLOCKED:
                action = mResources.getString(R.string.log_action_blocked);
                break;
            case ALLOWED:
                action = mResources.getString(R.string.log_action_allowed);
                break;
            case FAILED:
                action = mResources.getString(R.string.log_action_failed);
                break;
            default:
                action = String.format(
                        mResources.getString(R.string.log_action_unknown),
                        entity.getAction().getCode()
                );
                break;
        }

        String number = entity.getNumber();
        String formatted;

        if (number == null) {
            formatted = mResources.getString(R.string.log_number_private);
        } else {
            formatted = formatNumber(number);
        }

        if (formatted != null) {
            number = formatted;
        }

        return String.format(mResources.getString(R.string.log_message_format), action, number);
    }

    private String formatNumber(@NonNull String number) {
        try {
            String contactName = mContactFinder.findContactName(number);
            if (contactName != null) {
                return contactName;
            }
        } catch (InternalError ignored) {
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            return PhoneNumberUtils.formatNumber(number, Locale.getDefault().getCountry());
        } else {
            // noinspection deprecation
            return PhoneNumberUtils.formatNumber(number);
        }
    }
}
