package com.novyr.callfilter.rules;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.novyr.callfilter.CallDetails;
import com.novyr.callfilter.ContactFinder;

public class UnrecognizedRuleHandler implements RuleHandlerInterface {
    @NonNull
    private final ContactFinder mContactFinder;

    public UnrecognizedRuleHandler(@NonNull ContactFinder contactFinder) {
        mContactFinder = contactFinder;
    }

    @Override
    public boolean isMatch(@NonNull CallDetails details, @Nullable String ruleValue) {
        // If the number is private the PrivateChecker should handle it despite being unrecognized
        // TODO Should we handle it anyway? Would someone really want to block unrecognized but not
        //      private numbers?
        String number = details.getPhoneNumber();
        if (number == null) {
            return false;
        }

        return mContactFinder.findContactId(number) == null;
    }
}
