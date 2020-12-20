package com.novyr.callfilter.rules;

import androidx.annotation.Nullable;

import com.novyr.callfilter.ContactFinder;

public class UnrecognizedRuleHandler implements RuleHandlerInterface {
    private final ContactFinder mContactFinder;

    public UnrecognizedRuleHandler(ContactFinder contactFinder) {
        mContactFinder = contactFinder;
    }

    @Override
    public boolean isMatch(@Nullable String number, @Nullable String value) {
        // If we did not get a contact finder we don't want to prevent any calls from working so
        // we report not matching
        if (mContactFinder == null) {
            return false;
        }

        // If the number is private the PrivateChecker should handle it despite not being recognized
        if (number == null) {
            return false;
        }

        return mContactFinder.findContactId(number) == null;
    }
}
