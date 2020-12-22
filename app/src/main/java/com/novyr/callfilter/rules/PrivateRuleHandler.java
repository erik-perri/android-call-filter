package com.novyr.callfilter.rules;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.novyr.callfilter.CallDetails;

public class PrivateRuleHandler implements RuleHandlerInterface {
    @Override
    public boolean isMatch(@NonNull CallDetails details, @Nullable String ruleValue) {
        return details.getPhoneNumber() == null;
    }
}
