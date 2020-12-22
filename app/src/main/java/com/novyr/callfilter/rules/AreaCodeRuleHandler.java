package com.novyr.callfilter.rules;

import android.view.View;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.novyr.callfilter.AreaCodeExtractor;
import com.novyr.callfilter.CallDetails;
import com.novyr.callfilter.R;
import com.novyr.callfilter.db.entity.RuleEntity;
import com.novyr.callfilter.rules.exception.InvalidValueException;

public class AreaCodeRuleHandler implements RuleHandlerInterface, RuleHandlerWithFormInterface {
    @NonNull
    private final AreaCodeExtractor mAreaCodeExtractor;

    public AreaCodeRuleHandler(@NonNull AreaCodeExtractor areaCodeExtractor) {
        mAreaCodeExtractor = areaCodeExtractor;
    }

    @Override
    public boolean isMatch(@NonNull CallDetails details, @Nullable String ruleValue) {
        String number = details.getPhoneNumber();
        if (number == null || ruleValue == null) {
            return false;
        }

        String areaCode = mAreaCodeExtractor.extract(number);

        return areaCode != null && areaCode.equals(ruleValue);
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

        if (code.isEmpty() || !code.matches("^[0-9]{3}$")) {
            throw new InvalidValueException(R.string.rule_form_label_area_code);
        }

        rule.setValue(code);
    }
}
