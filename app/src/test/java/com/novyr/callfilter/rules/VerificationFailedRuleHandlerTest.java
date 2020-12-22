package com.novyr.callfilter.rules;

import android.telecom.Connection;

import com.novyr.callfilter.CallDetails;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class VerificationFailedRuleHandlerTest {
    @Test
    public void checkHandler() {
        VerificationFailedRuleHandler checker = new VerificationFailedRuleHandler();

        assertFalse(checker.isMatch(
                new CallDetails("8005551234", Connection.VERIFICATION_STATUS_NOT_VERIFIED),
                null
        ));

        assertTrue(checker.isMatch(
                new CallDetails("8005551234", Connection.VERIFICATION_STATUS_FAILED),
                null
        ));

        assertFalse(checker.isMatch(
                new CallDetails("8005551234", Connection.VERIFICATION_STATUS_PASSED),
                null
        ));
    }
}
