package com.novyr.callfilter.rules;

import android.telecom.Connection;

import com.novyr.callfilter.CallDetails;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class VerificationPassedRuleHandlerTest {
    @Test
    public void checkHandler() {
        VerificationPassedRuleHandler checker = new VerificationPassedRuleHandler();

        assertFalse(checker.isMatch(
                new CallDetails("8005551234", Connection.VERIFICATION_STATUS_NOT_VERIFIED),
                null
        ));

        assertFalse(checker.isMatch(
                new CallDetails("8005551234", Connection.VERIFICATION_STATUS_FAILED),
                null
        ));

        assertTrue(checker.isMatch(
                new CallDetails("8005551234", Connection.VERIFICATION_STATUS_PASSED),
                null
        ));
    }
}
