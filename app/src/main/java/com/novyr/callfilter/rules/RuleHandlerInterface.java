package com.novyr.callfilter.rules;

import androidx.annotation.Nullable;

public interface RuleHandlerInterface {
    boolean isMatch(@Nullable String number, @Nullable String value);
}
