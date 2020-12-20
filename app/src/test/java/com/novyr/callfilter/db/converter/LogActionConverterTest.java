package com.novyr.callfilter.db.converter;

import com.novyr.callfilter.db.entity.enums.LogAction;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class LogActionConverterTest {
    @Test
    public void toAction() {
        assertEquals(LogAction.BLOCKED, LogActionConverter.toLogAction(0));
        assertEquals(LogAction.ALLOWED, LogActionConverter.toLogAction(1));
        assertEquals(LogAction.FAILED, LogActionConverter.toLogAction(2));
        assertNull(LogActionConverter.toLogAction(3));
        assertNull(LogActionConverter.toLogAction(-1));
    }

    @Test
    public void fromAction() {
        assertEquals((Integer) 0, LogActionConverter.fromLogAction(LogAction.BLOCKED));
        assertEquals((Integer) 1, LogActionConverter.fromLogAction(LogAction.ALLOWED));
        assertEquals((Integer) 2, LogActionConverter.fromLogAction(LogAction.FAILED));
        assertNull(LogActionConverter.fromLogAction(null));
    }
}
