package com.novyr.callfilter.db.converter;

import com.novyr.callfilter.db.entity.enums.Action;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class ActionConverterTest {
    @Test
    public void toAction() throws Exception {
        assertEquals(Action.BLOCKED, ActionConverter.toAction(0));
        assertEquals(Action.ALLOWED, ActionConverter.toAction(1));
        assertEquals(Action.FAILED, ActionConverter.toAction(2));
        assertNull(ActionConverter.toAction(3));
        assertNull(ActionConverter.toAction(-1));
    }
}
