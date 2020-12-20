package com.novyr.callfilter.rules;

import androidx.annotation.Nullable;

import com.novyr.callfilter.rules.renderers.AreaCodeRenderer;

public class AreaCodeRuleHandler implements RuleHandlerInterface {
    @Override
    public boolean isMatch(@Nullable String number, @Nullable String value) {
        if (number == null || value == null) {
            return false;
        }

        String foundCode = null;
        String areaCode = value.replaceAll("[^\\d]", "");
        String normalized = number.replaceAll("[^\\d]", "");

        // Since we don't prevent the user from entering an area code longer than expected we'll
        // go ahead and check the full length of what was provided
        int areaCodeLength = areaCode.length();

        int normalizedLength = normalized.length();
        if (normalizedLength == 11) {
            // With country code 18005551234
            foundCode = normalized.substring(1, Math.min(normalized.length(), 1 + areaCodeLength));
        } else if (normalizedLength == 10) {
            // No country code 8005551234
            foundCode = normalized.substring(0, Math.min(normalized.length(), areaCodeLength));
        }

        return foundCode != null && foundCode.equals(value);
    }
}
