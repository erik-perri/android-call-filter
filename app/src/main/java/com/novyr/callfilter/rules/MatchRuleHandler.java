package com.novyr.callfilter.rules;

import android.view.View;
import android.widget.EditText;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.novyr.callfilter.CallDetails;
import com.novyr.callfilter.R;
import com.novyr.callfilter.db.entity.RuleEntity;
import com.novyr.callfilter.rules.exception.InvalidValueException;

import io.github.azagniotov.matcher.AntPathMatcher;

public class MatchRuleHandler implements RuleHandlerInterface, RuleHandlerWithFormInterface {
    @Override
    public boolean isMatch(@NonNull CallDetails details, @Nullable String ruleValue) {
        String number = details.getPhoneNumber();
        if (number == null || ruleValue == null) {
            return false;
        }

        if (ruleValue.contains("*") || ruleValue.contains("?")) {
            AntPathMatcher pathMatcher = new AntPathMatcher.Builder().build();
            return pathMatcher.isMatch(ruleValue, normalizeNumber(number));
        }

        return normalizeNumber(number).equals(normalizeNumber(ruleValue));
    }

    private String normalizeNumber(String number) {
        String normalizedNumber = number.replaceAll("[^\\d]", "");

        // We exclude the country code in case it was only provided in one of the numbers
        if (normalizedNumber.length() == 11 && normalizedNumber.startsWith("1")) {
            return normalizedNumber.substring(1);
        }

        return normalizedNumber;
    }

    @Override
    @LayoutRes
    public int getEditDialogLayout() {
        return R.layout.form_rule_match;
    }

    @Override
    public void loadFormValues(View view, RuleEntity rule) {
        EditText input = view.findViewById(R.id.match_input);

        input.setText(rule.getValue());
    }

    @Override
    public void saveFormValues(View view, RuleEntity rule) throws InvalidValueException {
        EditText inputView = view.findViewById(R.id.match_input);
        String input = inputView.getText().toString();

        if (input.isEmpty()) {
            throw new InvalidValueException(R.string.rule_form_label_match);
        }

        rule.setValue(input);
    }
}
