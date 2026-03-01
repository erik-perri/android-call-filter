package com.novyr.callfilter.rules;

import com.novyr.callfilter.CallDetails;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class MatchRuleHandlerTest {
    @Test
    public void isMatch_privateNumber_returnsFalse() {
        MatchRuleHandler checker = new MatchRuleHandler();

        assertFalse(checker.isMatch(new CallDetails(null), "8005551234"));
    }

    @Test
    public void isMatch_invalidNumber_returnsFalse() {
        MatchRuleHandler checker = new MatchRuleHandler();

        assertFalse(checker.isMatch(new CallDetails("1"), "8005551234"));
        assertFalse(checker.isMatch(new CallDetails("1"), null));
    }

    @Test
    public void isMatch_exactNumber_matchesCorrectly() {
        MatchRuleHandler checker = new MatchRuleHandler();

        assertTrue(checker.isMatch(new CallDetails("8005551234"), "8005551234"));
        assertFalse(checker.isMatch(new CallDetails("9005551234"), "8005551234"));
    }

    @Test
    public void isMatch_wildcardPattern_matchesCorrectly() {
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
    public void isMatch_formattedNumber_matchesNormalized() {
        MatchRuleHandler checker = new MatchRuleHandler();

        assertTrue(checker.isMatch(new CallDetails("18005551234"), "8005551234"));
        assertTrue(checker.isMatch(new CallDetails("8005551234"), "8005551234"));
        assertTrue(checker.isMatch(new CallDetails("8005551234"), "18005551234"));
        assertTrue(checker.isMatch(new CallDetails("800-555-1234"), "8005551234"));
        assertTrue(checker.isMatch(new CallDetails("1 (800) 555 1234"), "8005551234"));
    }
}
