package com.novyr.callfilter.rules;

import android.view.View;
import android.widget.EditText;

import androidx.annotation.Nullable;

import com.novyr.callfilter.R;
import com.novyr.callfilter.db.entity.RuleEntity;
import com.novyr.callfilter.rules.exception.InvalidValueException;

public class AreaCodeRuleHandler implements RuleHandlerInterface, RuleHandlerWithFormInterface {
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

    @Override
    public int getEditDialogLayout() {
        return R.layout.form_rule_area_code;
    }

    @Override
    public void loadFormValues(View view, RuleEntity rule) {
        EditText input = view.findViewById(R.id.area_code_input);

        input.setText(rule.getValue());
    }

    @Override
    public void saveFormValues(View view, RuleEntity rule) throws InvalidValueException {
        EditText inputView = view.findViewById(R.id.area_code_input);
        String code = inputView.getText().toString();

        if (code.isEmpty() || !code.matches("^[0-9]+$")) {
            throw new InvalidValueException(R.string.rule_form_label_area_code);
        }

        rule.setValue(code);
    }
}
