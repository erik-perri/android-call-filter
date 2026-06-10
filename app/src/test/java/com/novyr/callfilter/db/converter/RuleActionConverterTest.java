package com.novyr.callfilter.db.converter;

import com.novyr.callfilter.db.entity.enums.RuleAction;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class RuleActionConverterTest {
    @Test
    public void toRuleAction_intValue_returnsCorrectEnum() {
        assertEquals(RuleAction.BLOCK, RuleActionConverter.toRuleAction(1));
        assertEquals(RuleAction.ALLOW, RuleActionConverter.toRuleAction(2));
        assertEquals(RuleAction.ANSWER_AND_END, RuleActionConverter.toRuleAction(3));
        assertNull(RuleActionConverter.toRuleAction(4));
        assertNull(RuleActionConverter.toRuleAction(-1));
    }

    @Test
    public void fromRuleAction_enumValue_returnsCorrectInt() {
        assertEquals((Integer) 1, RuleActionConverter.fromRuleAction(RuleAction.BLOCK));
        assertEquals((Integer) 2, RuleActionConverter.fromRuleAction(RuleAction.ALLOW));
        assertEquals((Integer) 3, RuleActionConverter.fromRuleAction(RuleAction.ANSWER_AND_END));
        assertNull(RuleActionConverter.fromRuleAction(null));
    }
}
