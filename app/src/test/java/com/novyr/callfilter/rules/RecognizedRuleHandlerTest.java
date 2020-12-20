package com.novyr.callfilter.rules;

import com.novyr.callfilter.ContactFinder;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class RecognizedRuleHandlerTest {
    private final String RECOGNIZED_NUMBER = "8005551234";
    private final String UNRECOGNIZED_NUMBER = "9005554321";

    private ContactFinder createFinderMock() {
        ContactFinder finder = mock(ContactFinder.class);

        when(finder.findContactId(RECOGNIZED_NUMBER)).thenReturn("1");
        when(finder.findContactId(UNRECOGNIZED_NUMBER)).thenReturn(null);

        return finder;
    }

    @Test
    public void checkNoFinder() {
        RecognizedRuleHandler checker = new RecognizedRuleHandler(null);

        assertFalse(checker.isMatch(null, null));
        assertFalse(checker.isMatch(UNRECOGNIZED_NUMBER, null));
        assertFalse(checker.isMatch(RECOGNIZED_NUMBER, null));
    }

    @Test
    public void checkPrivateMatch() {
        RecognizedRuleHandler checker = new RecognizedRuleHandler(createFinderMock());

        assertFalse(checker.isMatch(null, null));
    }

    @Test
    public void checkNormalMatch() {
        RecognizedRuleHandler checker = new RecognizedRuleHandler(createFinderMock());

        assertFalse(checker.isMatch(UNRECOGNIZED_NUMBER, null));
        assertTrue(checker.isMatch(RECOGNIZED_NUMBER, null));
    }
}
