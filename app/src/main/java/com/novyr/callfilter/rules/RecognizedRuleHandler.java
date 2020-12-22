package com.novyr.callfilter.rules;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.novyr.callfilter.CallDetails;
import com.novyr.callfilter.ContactFinder;

public class RecognizedRuleHandler implements RuleHandlerInterface {
    @NonNull
    private final ContactFinder mContactFinder;

    public RecognizedRuleHandler(@NonNull ContactFinder contactFinder) {
        mContactFinder = contactFinder;
    }

    @Override
    public boolean isMatch(@NonNull CallDetails details, @Nullable String ruleValue) {
        String number = details.getPhoneNumber();
        if (number == null) {
            return false;
        }

        return mContactFinder.findContactId(number) != null;
    }
}
