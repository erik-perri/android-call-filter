package com.novyr.callfilter.db.converter;

import androidx.room.TypeConverter;

import java.util.Calendar;
import java.util.TimeZone;

public class CalendarConverter {
    @TypeConverter
    public static Calendar toCalendar(Long timestamp) {
        if (timestamp == null) {
            return null;
        }

        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        calendar.setTimeInMillis(timestamp);
        return calendar;
    }

    @TypeConverter
    public static Long fromCalendar(Calendar calendar) {
        if (calendar == null) {
            return null;
        }

        return calendar.getTime().getTime();
    }
}
