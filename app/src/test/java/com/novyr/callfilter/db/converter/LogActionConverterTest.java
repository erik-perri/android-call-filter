package com.novyr.callfilter.db.converter;

import com.novyr.callfilter.db.entity.enums.LogAction;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class LogActionConverterTest {
    @Test
    public void toLogAction_intValue_returnsCorrectEnum() {
        assertEquals(LogAction.BLOCKED, LogActionConverter.toLogAction(0));
        assertEquals(LogAction.ALLOWED, LogActionConverter.toLogAction(1));
        assertEquals(LogAction.FAILED, LogActionConverter.toLogAction(2));
        assertEquals(LogAction.ENDED_NO_VOICEMAIL, LogActionConverter.toLogAction(3));
        assertEquals(LogAction.FELL_BACK_TO_BLOCK, LogActionConverter.toLogAction(4));
        assertNull(LogActionConverter.toLogAction(5));
        assertNull(LogActionConverter.toLogAction(-1));
    }

    @Test
    public void fromLogAction_enumValue_returnsCorrectInt() {
        assertEquals((Integer) 0, LogActionConverter.fromLogAction(LogAction.BLOCKED));
        assertEquals((Integer) 1, LogActionConverter.fromLogAction(LogAction.ALLOWED));
        assertEquals((Integer) 2, LogActionConverter.fromLogAction(LogAction.FAILED));
        assertEquals((Integer) 3, LogActionConverter.fromLogAction(LogAction.ENDED_NO_VOICEMAIL));
        assertEquals((Integer) 4, LogActionConverter.fromLogAction(LogAction.FELL_BACK_TO_BLOCK));
        assertNull(LogActionConverter.fromLogAction(null));
    }
}
