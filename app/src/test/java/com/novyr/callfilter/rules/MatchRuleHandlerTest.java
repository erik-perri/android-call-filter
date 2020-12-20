package com.novyr.callfilter.rules;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class MatchRuleHandlerTest {
    @Test
    public void checkPrivateMatch() {
        MatchRuleHandler checker = new MatchRuleHandler();

        assertFalse(checker.isMatch(null, "8005551234"));
    }

    @Test
    public void checkInvalidMatch() {
        MatchRuleHandler checker = new MatchRuleHandler();

        assertFalse(checker.isMatch("1", "8005551234"));
        assertFalse(checker.isMatch("1", null));
    }

    @Test
    public void checkNormalMatch() {
        MatchRuleHandler checker = new MatchRuleHandler();

        assertTrue(checker.isMatch("8005551234", "8005551234"));
        assertFalse(checker.isMatch("9005551234", "8005551234"));
    }

    @Test
    public void checkVariant() {
        MatchRuleHandler checker = new MatchRuleHandler();

        assertTrue(checker.isMatch("18005551234", "8005551234"));
        assertTrue(checker.isMatch("8005551234", "8005551234"));
        assertTrue(checker.isMatch("8005551234", "18005551234"));
        assertTrue(checker.isMatch("800-555-1234", "8005551234"));
        assertTrue(checker.isMatch("1 (800) 555 1234", "8005551234"));
    }
}
