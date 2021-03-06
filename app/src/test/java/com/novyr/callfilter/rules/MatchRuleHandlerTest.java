package com.novyr.callfilter.rules;

import com.novyr.callfilter.CallDetails;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class MatchRuleHandlerTest {
    @Test
    public void checkPrivateMatch() {
        MatchRuleHandler checker = new MatchRuleHandler();

        assertFalse(checker.isMatch(new CallDetails(null), "8005551234"));
    }

    @Test
    public void checkInvalidMatch() {
        MatchRuleHandler checker = new MatchRuleHandler();

        assertFalse(checker.isMatch(new CallDetails("1"), "8005551234"));
        assertFalse(checker.isMatch(new CallDetails("1"), null));
    }

    @Test
    public void checkNormalMatch() {
        MatchRuleHandler checker = new MatchRuleHandler();

        assertTrue(checker.isMatch(new CallDetails("8005551234"), "8005551234"));
        assertFalse(checker.isMatch(new CallDetails("9005551234"), "8005551234"));
    }

    @Test
    public void checkWildcardMatch() {
        MatchRuleHandler checker = new MatchRuleHandler();

        assertTrue(checker.isMatch(new CallDetails("8005551234"), "*8005551234*"));

        assertTrue(checker.isMatch(new CallDetails("8045551234"), "80?*"));
        assertFalse(checker.isMatch(new CallDetails("8145551234"), "80?*"));

        assertFalse(checker.isMatch(new CallDetails("8005551234"), "800555"));
        assertTrue(checker.isMatch(new CallDetails("8005551234"), "800555*"));
        assertFalse(checker.isMatch(new CallDetails("8005551234"), "5551234"));
        assertTrue(checker.isMatch(new CallDetails("8005551234"), "*5551234"));

        assertFalse(checker.isMatch(new CallDetails("9005551234"), "*321*"));
        assertTrue(checker.isMatch(new CallDetails("9005551234"), "*123*"));

        assertTrue(checker.isMatch(new CallDetails("9005551234"), "?005551234"));
        assertTrue(checker.isMatch(new CallDetails("8005551234"), "?005551234"));
    }

    @Test
    public void checkVariant() {
        MatchRuleHandler checker = new MatchRuleHandler();

        assertTrue(checker.isMatch(new CallDetails("18005551234"), "8005551234"));
        assertTrue(checker.isMatch(new CallDetails("8005551234"), "8005551234"));
        assertTrue(checker.isMatch(new CallDetails("8005551234"), "18005551234"));
        assertTrue(checker.isMatch(new CallDetails("800-555-1234"), "8005551234"));
        assertTrue(checker.isMatch(new CallDetails("1 (800) 555 1234"), "8005551234"));
    }
}
