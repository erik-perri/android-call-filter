package com.novyr.callfilter.db.converter;

import androidx.room.TypeConverter;

import com.novyr.callfilter.db.entity.enums.LogAction;

public class LogActionConverter {
    @TypeConverter
    public static LogAction toLogAction(int numeral) {
        for (LogAction action : LogAction.values()) {
            if (action.getCode() == numeral) {
                return action;
            }
        }
        return null;
    }

    @TypeConverter
    public static Integer fromLogAction(LogAction action) {
        if (action != null) {
            return action.getCode();
        }

        return null;
    }
}
