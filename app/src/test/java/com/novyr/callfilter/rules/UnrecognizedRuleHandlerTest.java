package com.novyr.callfilter.rules;

import com.novyr.callfilter.CallDetails;
import com.novyr.callfilter.ContactFinder;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class UnrecognizedRuleHandlerTest {
    private final String RECOGNIZED_NUMBER = "8005551234";
    private final String UNRECOGNIZED_NUMBER = "9005554321";

    private ContactFinder createFinderMock() {
        ContactFinder finder = mock(ContactFinder.class);

        when(finder.findContactId(RECOGNIZED_NUMBER)).thenReturn("1");
        when(finder.findContactId(UNRECOGNIZED_NUMBER)).thenReturn(null);

        return finder;
    }

    @Test
    public void checkPrivateMatch() {
        UnrecognizedRuleHandler checker = new UnrecognizedRuleHandler(createFinderMock());

        assertFalse(checker.isMatch(new CallDetails(null), null));
    }

    @Test
    public void checkNormalMatch() {
        UnrecognizedRuleHandler checker = new UnrecognizedRuleHandler(createFinderMock());

        assertTrue(checker.isMatch(new CallDetails(UNRECOGNIZED_NUMBER), null));
        assertFalse(checker.isMatch(new CallDetails(RECOGNIZED_NUMBER), null));
    }
}
