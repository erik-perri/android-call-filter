package com.novyr.callfilter.db.converter;

import androidx.room.TypeConverter;

import com.novyr.callfilter.db.entity.enums.RuleAction;

public class RuleActionConverter {
    @TypeConverter
    public static RuleAction toRuleAction(int numeral) {
        for (RuleAction action : RuleAction.values()) {
            if (action.getCode() == numeral) {
                return action;
            }
        }
        return null;
    }

    @TypeConverter
    public static Integer fromRuleAction(RuleAction action) {
        if (action != null) {
            return action.getCode();
        }

        return null;
    }
}
