package com.novyr.callfilter.rules;

import com.novyr.callfilter.CallDetails;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class AreaCodeRuleHandlerTest {
    @Test
    public void checkPrivateMatch() {
        AreaCodeRuleHandler checker = new AreaCodeRuleHandler();

        assertFalse(checker.isMatch(new CallDetails(null), "800"));
    }

    @Test
    public void checkInvalidMatch() {
        AreaCodeRuleHandler checker = new AreaCodeRuleHandler();

        assertFalse(checker.isMatch(new CallDetails("1"), "800"));
        assertFalse(checker.isMatch(new CallDetails("1"), null));
    }

    @Test
    public void checkNormalMatch() {
        AreaCodeRuleHandler checker = new AreaCodeRuleHandler();

        assertTrue(checker.isMatch(new CallDetails("8005551234"), "800"));
        assertFalse(checker.isMatch(new CallDetails("9005551234"), "800"));
    }

    @Test
    public void checkVariant() {
        AreaCodeRuleHandler checker = new AreaCodeRuleHandler();

        assertTrue(checker.isMatch(new CallDetails("18005551234"), "800"));
        assertTrue(checker.isMatch(new CallDetails("8005551234"), "800"));
        assertTrue(checker.isMatch(new CallDetails("800-555-1234"), "800"));
        assertTrue(checker.isMatch(new CallDetails("1 (800) 555 1234"), "800"));
    }

    @Test
    public void checkWrongSize() {
        // TODO Should we even test unexpected behavior?
        AreaCodeRuleHandler checker = new AreaCodeRuleHandler();

        assertTrue(checker.isMatch(new CallDetails("18005551234"), "80"));
        assertTrue(checker.isMatch(new CallDetails("8005551234"), "800555"));
        assertFalse(checker.isMatch(new CallDetails("8005551234"), "8005554"));
        assertTrue(checker.isMatch(new CallDetails("800-555-1234"), "8005551234"));
        assertFalse(checker.isMatch(new CallDetails("1 (800) 555 1234"), "80055512341"));
    }
}
