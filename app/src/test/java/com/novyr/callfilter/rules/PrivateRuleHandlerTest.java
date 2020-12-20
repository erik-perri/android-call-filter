package com.novyr.callfilter.rules;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class PrivateRuleHandlerTest {
    @Test
    public void checkNormalMatch() {
        PrivateRuleHandler checker = new PrivateRuleHandler();

        assertFalse(checker.isMatch("8005551234", null));
        assertTrue(checker.isMatch(null, null));
    }
}
