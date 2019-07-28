package com.novyr.callfilter.db.converter;

import com.novyr.callfilter.db.entity.enums.Action;

import androidx.room.TypeConverter;

public class ActionConverter {
    @TypeConverter
    public static Action toAction(int numeral) {
        for (Action action : Action.values()) {
            if (action.getCode() == numeral) {
                return action;
            }
        }
        return null;
    }

    @TypeConverter
    public static Integer fromAction(Action action) {
        if (action != null) {
            return action.getCode();
        }

        return null;
    }
}
