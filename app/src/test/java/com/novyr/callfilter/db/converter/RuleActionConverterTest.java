package com.novyr.callfilter.db.converter;

import com.novyr.callfilter.db.entity.enums.RuleAction;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class RuleActionConverterTest {
    @Test
    public void testToAction() {
        assertEquals(RuleAction.BLOCK, RuleActionConverter.toRuleAction(1));
        assertEquals(RuleAction.ALLOW, RuleActionConverter.toRuleAction(2));
        assertNull(RuleActionConverter.toRuleAction(3));
        assertNull(RuleActionConverter.toRuleAction(-1));
    }

    @Test
    public void testFromAction() {
        assertEquals((Integer) 1, RuleActionConverter.fromRuleAction(RuleAction.BLOCK));
        assertEquals((Integer) 2, RuleActionConverter.fromRuleAction(RuleAction.ALLOW));
        assertNull(RuleActionConverter.fromRuleAction(null));
    }
}
