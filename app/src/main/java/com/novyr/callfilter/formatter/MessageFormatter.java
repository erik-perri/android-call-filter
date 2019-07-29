package com.novyr.callfilter.formatter;

import com.novyr.callfilter.db.entity.LogEntity;

public interface MessageFormatter {
    String formatMessage(LogEntity entity);
}
