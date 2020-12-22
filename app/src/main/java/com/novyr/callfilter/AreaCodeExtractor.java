package com.novyr.callfilter;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AreaCodeExtractor {
    private static final String TAG = AreaCodeExtractor.class.getName();

    @NonNull
    private final PhoneNumberUtil mPhoneNumberUtil;

    public AreaCodeExtractor(@NonNull PhoneNumberUtil phoneNumberUtil) {
        mPhoneNumberUtil = phoneNumberUtil;
    }

    public String extract(String number) {
        Phonenumber.PhoneNumber parsedNumber;

        try {
            parsedNumber = mPhoneNumberUtil.parse(number, "US");
        } catch (NumberParseException e) {
            Log.d(TAG, "Failed to parse number: " + e.toString());
            return null;
        }

        String formatted = mPhoneNumberUtil.format(
                parsedNumber,
                PhoneNumberUtil.PhoneNumberFormat.INTERNATIONAL
        );

        // If we were provided with a valid US or Canadian (non-local) phone number the
        // international format should be +1 ###-###-####
        Pattern countryCodePattern = Pattern.compile("^\\+[0-9] ([0-9]{3})-[0-9]{3}-[0-9]{4}$");
        Matcher countryMatcher = countryCodePattern.matcher(formatted);

        if (!countryMatcher.find()) {
            return null;
        }

        return countryMatcher.group(1);
    }
}
