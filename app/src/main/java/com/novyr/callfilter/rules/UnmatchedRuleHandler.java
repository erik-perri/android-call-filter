package com.novyr.callfilter.rules;

import androidx.annotation.Nullable;

public class UnmatchedRuleHandler implements RuleHandlerInterface {
    @Override
    public boolean isMatch(@Nullable String number, @Nullable String value) {
        return true;
    }
}
