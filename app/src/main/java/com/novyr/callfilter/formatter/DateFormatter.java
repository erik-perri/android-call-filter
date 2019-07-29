package com.novyr.callfilter.formatter;

import com.novyr.callfilter.db.entity.LogEntity;

public interface DateFormatter {
    String formatDate(LogEntity entity);
}
