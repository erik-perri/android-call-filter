package com.novyr.callfilter.rules;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.novyr.callfilter.ContactFinder;

public class RecognizedRuleHandler implements RuleHandlerInterface {
    @NonNull
    private final ContactFinder mContactFinder;

    public RecognizedRuleHandler(@NonNull ContactFinder contactFinder) {
        mContactFinder = contactFinder;
    }

    @Override
    public boolean isMatch(@Nullable String number, @Nullable String value) {
        // TODO Should this return true if no contact finder is available?
        if (mContactFinder == null || number == null) {
            return false;
        }

        return mContactFinder.findContactId(number) != null;
    }
}
