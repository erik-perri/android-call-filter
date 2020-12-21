package com.novyr.callfilter.formatter;

import com.novyr.callfilter.db.entity.LogEntity;

import java.text.DateFormat;

public class LogDateFormatter implements DateFormatter {
    public String formatDate(LogEntity entity) {
        DateFormat format = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT);
        return format.format(entity.getCreated().getTime());
    }
}
