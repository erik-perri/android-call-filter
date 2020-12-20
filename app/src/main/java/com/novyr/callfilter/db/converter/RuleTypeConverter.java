package com.novyr.callfilter.db.converter;

import androidx.room.TypeConverter;

import com.novyr.callfilter.db.entity.enums.RuleType;

public class RuleTypeConverter {
    @TypeConverter
    public static RuleType toRuleType(int numeral) {
        for (RuleType type : RuleType.values()) {
            if (type.getCode() == numeral) {
                return type;
            }
        }
        return null;
    }

    @TypeConverter
    public static Integer fromRuleType(RuleType type) {
        if (type != null) {
            return type.getCode();
        }

        return null;
    }
}
