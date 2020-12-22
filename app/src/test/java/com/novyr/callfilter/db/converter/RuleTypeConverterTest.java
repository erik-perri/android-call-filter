package com.novyr.callfilter.db.converter;

import com.novyr.callfilter.db.entity.enums.RuleType;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class RuleTypeConverterTest {
    @Test
    public void testToType() {
        assertEquals(RuleType.UNMATCHED, RuleTypeConverter.toRuleType(1));
        assertEquals(RuleType.MATCH, RuleTypeConverter.toRuleType(6));
        assertNull(RuleTypeConverter.toRuleType(99));
        assertNull(RuleTypeConverter.toRuleType(-1));
    }

    @Test
    public void testFromType() {
        assertEquals((Integer) 1, RuleTypeConverter.fromRuleType(RuleType.UNMATCHED));
        assertEquals((Integer) 6, RuleTypeConverter.fromRuleType(RuleType.MATCH));
    }
}
