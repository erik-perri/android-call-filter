package com.novyr.callfilter.rules;

import android.view.View;

import androidx.annotation.LayoutRes;

import com.novyr.callfilter.db.entity.RuleEntity;
import com.novyr.callfilter.rules.exception.InvalidValueException;

public interface RuleHandlerWithFormInterface {
    @LayoutRes
    int getEditDialogLayout();

    void loadFormValues(View view, RuleEntity rule);

    void saveFormValues(View view, RuleEntity rule) throws InvalidValueException;
}
