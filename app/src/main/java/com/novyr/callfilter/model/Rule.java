package com.novyr.callfilter.model;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.novyr.callfilter.db.entity.enums.RuleAction;
import com.novyr.callfilter.db.entity.enums.RuleType;

public interface Rule {
    int getId();

    @NonNull
    RuleType getType();

    @Nullable
    String getValue();

    @NonNull
    RuleAction getAction();

    boolean isEnabled();

    int getOrder();
}
