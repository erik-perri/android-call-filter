package com.novyr.callfilter.rules;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class AreaCodeRuleHandlerTest {
    @Test
    public void checkPrivateMatch() {
        AreaCodeRuleHandler checker = new AreaCodeRuleHandler();

        assertFalse(checker.isMatch(null, "800"));
    }

    @Test
    public void checkInvalidMatch() {
        AreaCodeRuleHandler checker = new AreaCodeRuleHandler();

        assertFalse(checker.isMatch("1", "800"));
        assertFalse(checker.isMatch("1", null));
    }

    @Test
    public void checkNormalMatch() {
        AreaCodeRuleHandler checker = new AreaCodeRuleHandler();

        assertTrue(checker.isMatch("8005551234", "800"));
        assertFalse(checker.isMatch("9005551234", "800"));
    }

    @Test
    public void checkVariant() {
        AreaCodeRuleHandler checker = new AreaCodeRuleHandler();

        assertTrue(checker.isMatch("18005551234", "800"));
        assertTrue(checker.isMatch("8005551234", "800"));
        assertTrue(checker.isMatch("800-555-1234", "800"));
        assertTrue(checker.isMatch("1 (800) 555 1234", "800"));
    }

    @Test
    public void checkWrongSize() {
        // TODO Should we even test unexpected behavior?
        AreaCodeRuleHandler checker = new AreaCodeRuleHandler();

        assertTrue(checker.isMatch("18005551234", "80"));
        assertTrue(checker.isMatch("8005551234", "800555"));
        assertFalse(checker.isMatch("8005551234", "8005554"));
        assertTrue(checker.isMatch("800-555-1234", "8005551234"));
        assertFalse(checker.isMatch("1 (800) 555 1234", "80055512341"));
    }
}
