package com.novyr.callfilter;

import android.os.Build;
import android.telecom.Connection;

import com.novyr.callfilter.db.entity.RuleEntity;
import com.novyr.callfilter.db.entity.enums.RuleAction;
import com.novyr.callfilter.db.entity.enums.RuleType;
import com.novyr.callfilter.rules.RuleHandlerManager;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class RuleCheckerTest {
    private final String RECOGNIZED_NUMBER = "8005551234";
    private final String UNRECOGNIZED_NUMBER = "9005554321";

    private ContactFinder createFinderMock() {
        ContactFinder finder = mock(ContactFinder.class);

        when(finder.findContactId(RECOGNIZED_NUMBER)).thenReturn("1");
        when(finder.findContactId(UNRECOGNIZED_NUMBER)).thenReturn(null);

        return finder;
    }

    @Test
    public void allowCall_noRules_returnsTrue() {
        RuleChecker ruleChecker = new RuleChecker(
                new RuleHandlerManager(createFinderMock()),
                new RuleEntity[0]
        );

        assertTrue(ruleChecker.allowCall(new CallDetails(null)));
        assertTrue(ruleChecker.allowCall(new CallDetails(RECOGNIZED_NUMBER)));
        assertTrue(ruleChecker.allowCall(new CallDetails(UNRECOGNIZED_NUMBER)));
    }

    @Test
    public void allowCall_unmatchedRule_matchesAllCalls() {
        RuleChecker ruleChecker = new RuleChecker(
                new RuleHandlerManager(createFinderMock()),
                new RuleEntity[]{
                        new RuleEntity(RuleType.UNMATCHED, RuleAction.ALLOW, null, true, 0)
                }
        );

        assertTrue(ruleChecker.allowCall(new CallDetails(null)));
        assertTrue(ruleChecker.allowCall(new CallDetails(RECOGNIZED_NUMBER)));
        assertTrue(ruleChecker.allowCall(new CallDetails(UNRECOGNIZED_NUMBER)));

        ruleChecker = new RuleChecker(
                new RuleHandlerManager(createFinderMock()),
                new RuleEntity[]{
                        new RuleEntity(RuleType.UNMATCHED, RuleAction.BLOCK, null, true, 0)
                }
        );

        assertFalse(ruleChecker.allowCall(new CallDetails(null)));
        assertFalse(ruleChecker.allowCall(new CallDetails(RECOGNIZED_NUMBER)));
        assertFalse(ruleChecker.allowCall(new CallDetails(UNRECOGNIZED_NUMBER)));
    }

    @Test
    public void allowCall_higherPriorityBlockRule_blocksCall() {
        String code = RECOGNIZED_NUMBER.substring(0, 3);
        RuleChecker ruleChecker = new RuleChecker(
                new RuleHandlerManager(createFinderMock()),
                new RuleEntity[]{
                        new RuleEntity(RuleType.AREA_CODE, RuleAction.BLOCK, code, true, 2),
                        new RuleEntity(RuleType.RECOGNIZED, RuleAction.ALLOW, null, true, 0),
                }
        );

        assertFalse(ruleChecker.allowCall(new CallDetails(RECOGNIZED_NUMBER)));
    }

    @Test
    public void allowCall_whitelistConfig_allowsRecognizedOnly() {
        RuleChecker ruleChecker = new RuleChecker(
                new RuleHandlerManager(createFinderMock()),
                new RuleEntity[]{
                        new RuleEntity(RuleType.RECOGNIZED, RuleAction.ALLOW, null, true, 2),
                        new RuleEntity(RuleType.UNMATCHED, RuleAction.BLOCK, null, true, 0),
                }
        );

        assertFalse(ruleChecker.allowCall(new CallDetails(null)));
        assertFalse(ruleChecker.allowCall(new CallDetails(UNRECOGNIZED_NUMBER)));
        assertTrue(ruleChecker.allowCall(new CallDetails(RECOGNIZED_NUMBER)));
    }

    @Test
    public void allowCall_blacklistConfig_blocksPrivateAndUnrecognized() {
        RuleChecker ruleChecker = new RuleChecker(
                new RuleHandlerManager(createFinderMock()),
                new RuleEntity[]{
                        new RuleEntity(RuleType.PRIVATE, RuleAction.BLOCK, null, true, 4),
                        new RuleEntity(RuleType.UNRECOGNIZED, RuleAction.BLOCK, null, true, 2),
                        new RuleEntity(RuleType.UNMATCHED, RuleAction.ALLOW, null, true, 0),
                }
        );

        assertFalse(ruleChecker.allowCall(new CallDetails(null)));
        assertFalse(ruleChecker.allowCall(new CallDetails(UNRECOGNIZED_NUMBER)));
        assertTrue(ruleChecker.allowCall(new CallDetails(RECOGNIZED_NUMBER)));
    }

    @Test
    public void allowCall_disabledRules_allowsAllCalls() {
        RuleChecker ruleChecker = new RuleChecker(
                new RuleHandlerManager(createFinderMock()),
                new RuleEntity[]{
                        new RuleEntity(RuleType.PRIVATE, RuleAction.BLOCK, null, false, 6),
                        new RuleEntity(RuleType.UNRECOGNIZED, RuleAction.BLOCK, null, false, 4),
                        new RuleEntity(RuleType.RECOGNIZED, RuleAction.BLOCK, null, false, 2),
                        new RuleEntity(RuleType.UNMATCHED, RuleAction.ALLOW, null, true, 0),
                }
        );

        assertTrue(ruleChecker.allowCall(new CallDetails(null)));
        assertTrue(ruleChecker.allowCall(new CallDetails(UNRECOGNIZED_NUMBER)));
        assertTrue(ruleChecker.allowCall(new CallDetails(RECOGNIZED_NUMBER)));
    }

    @Test
    public void allowCall_areaCode_blocked() {
        RuleChecker ruleChecker = new RuleChecker(
                new RuleHandlerManager(createFinderMock()),
                new RuleEntity[]{
                        new RuleEntity(RuleType.AREA_CODE, RuleAction.BLOCK, "800", true, 4),
                        new RuleEntity(RuleType.UNMATCHED, RuleAction.ALLOW, null, true, 0),
                }
        );

        assertFalse(ruleChecker.allowCall(new CallDetails("8005551234")));
    }

    @Test
    public void allowCall_wildcardStar_blocked() {
        RuleChecker ruleChecker = new RuleChecker(
                new RuleHandlerManager(createFinderMock()),
                new RuleEntity[]{
                        new RuleEntity(RuleType.MATCH, RuleAction.BLOCK, "555*", true, 4),
                        new RuleEntity(RuleType.UNMATCHED, RuleAction.ALLOW, null, true, 0),
                }
        );

        assertFalse(ruleChecker.allowCall(new CallDetails("5551234")));
    }

    @Test
    public void allowCall_exactMatch_blocked() {
        RuleChecker ruleChecker = new RuleChecker(
                new RuleHandlerManager(createFinderMock()),
                new RuleEntity[]{
                        new RuleEntity(RuleType.MATCH, RuleAction.BLOCK, "5551234", true, 4),
                        new RuleEntity(RuleType.UNMATCHED, RuleAction.ALLOW, null, true, 0),
                }
        );

        assertFalse(ruleChecker.allowCall(new CallDetails("5551234")));
    }

    @Test
    public void allowCall_questionMarkMatch_blocked() {
        RuleChecker ruleChecker = new RuleChecker(
                new RuleHandlerManager(createFinderMock()),
                new RuleEntity[]{
                        new RuleEntity(RuleType.MATCH, RuleAction.BLOCK, "555????", true, 4),
                        new RuleEntity(RuleType.UNMATCHED, RuleAction.ALLOW, null, true, 0),
                }
        );

        assertFalse(ruleChecker.allowCall(new CallDetails("5551234")));
    }

    @Test
    public void allowCall_questionMarkNoMatch_allowed() {
        RuleChecker ruleChecker = new RuleChecker(
                new RuleHandlerManager(createFinderMock()),
                new RuleEntity[]{
                        new RuleEntity(RuleType.MATCH, RuleAction.BLOCK, "555????", true, 4),
                        new RuleEntity(RuleType.UNMATCHED, RuleAction.ALLOW, null, true, 0),
                }
        );

        assertTrue(ruleChecker.allowCall(new CallDetails("55512345")));
    }

    @Test
    public void allowCall_countryCodeNormalized_blocked() {
        RuleChecker ruleChecker = new RuleChecker(
                new RuleHandlerManager(createFinderMock()),
                new RuleEntity[]{
                        new RuleEntity(RuleType.MATCH, RuleAction.BLOCK, "5551234567", true, 4),
                        new RuleEntity(RuleType.UNMATCHED, RuleAction.ALLOW, null, true, 0),
                }
        );

        assertFalse(ruleChecker.allowCall(new CallDetails("15551234567")));
    }

    @Test
    public void allowCall_verificationPassed_allowed() {
        assumeTrue(Build.VERSION.SDK_INT >= Build.VERSION_CODES.R);
        RuleChecker ruleChecker = new RuleChecker(
                new RuleHandlerManager(createFinderMock()),
                new RuleEntity[]{
                        new RuleEntity(RuleType.VERIFICATION_PASSED, RuleAction.ALLOW, null, true, 4),
                        new RuleEntity(RuleType.UNMATCHED, RuleAction.BLOCK, null, true, 0),
                }
        );

        assertTrue(ruleChecker.allowCall(new CallDetails("5551234", Connection.VERIFICATION_STATUS_PASSED)));
    }

    @Test
    public void allowCall_verificationFailed_blocked() {
        assumeTrue(Build.VERSION.SDK_INT >= Build.VERSION_CODES.R);
        RuleChecker ruleChecker = new RuleChecker(
                new RuleHandlerManager(createFinderMock()),
                new RuleEntity[]{
                        new RuleEntity(RuleType.VERIFICATION_FAILED, RuleAction.BLOCK, null, true, 4),
                        new RuleEntity(RuleType.UNMATCHED, RuleAction.ALLOW, null, true, 0),
                }
        );

        assertFalse(ruleChecker.allowCall(new CallDetails("5551234", Connection.VERIFICATION_STATUS_FAILED)));
    }

    @Test
    public void allowCall_verificationNotVerified_allowed() {
        assumeTrue(Build.VERSION.SDK_INT >= Build.VERSION_CODES.R);
        RuleChecker ruleChecker = new RuleChecker(
                new RuleHandlerManager(createFinderMock()),
                new RuleEntity[]{
                        new RuleEntity(RuleType.VERIFICATION_PASSED, RuleAction.ALLOW, null, true, 4),
                        new RuleEntity(RuleType.VERIFICATION_FAILED, RuleAction.BLOCK, null, true, 2),
                        new RuleEntity(RuleType.UNMATCHED, RuleAction.ALLOW, null, true, 0),
                }
        );

        assertTrue(ruleChecker.allowCall(new CallDetails("5551234", Connection.VERIFICATION_STATUS_NOT_VERIFIED)));
    }
}