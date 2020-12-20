package com.novyr.callfilter.rules;

import androidx.annotation.Nullable;

import com.novyr.callfilter.rules.renderers.MatchRenderer;

public class MatchRuleHandler implements RuleHandlerInterface {
    @Override
    public boolean isMatch(@Nullable String number, @Nullable String value) {
        if (number == null || value == null) {
            return false;
        }

        return normalizeNumber(number).equals(normalizeNumber(value));
    }

    private String normalizeNumber(String number) {
        String normalizedNumber = number.replaceAll("[^\\d]", "");

        // We exclude the country code in case it was only provided in one of the numbers
        if (normalizedNumber.length() == 11 && normalizedNumber.startsWith("1")) {
            return normalizedNumber.substring(1);
        }

        return normalizedNumber;
    }
}
