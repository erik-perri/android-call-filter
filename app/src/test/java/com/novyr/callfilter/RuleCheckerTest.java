package com.novyr.callfilter;

import android.os.Build;
import android.telecom.Connection;

import com.novyr.callfilter.db.entity.RuleEntity;
import com.novyr.callfilter.db.entity.enums.RuleAction;
import com.novyr.callfilter.db.entity.enums.RuleType;
import com.novyr.callfilter.permissions.Capability;
import com.novyr.callfilter.permissions.CapabilityResolver;
import com.novyr.callfilter.rules.RuleHandlerManager;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class RuleCheckerTest {
    private final String RECOGNIZED_NUMBER = "8005551234";
    private final String UNRECOGNIZED_NUMBER = "9005554321";

    private static final CapabilityResolver ALL_ACTIVE = capability -> true;

    private ContactFinder createFinderMock() {
        ContactFinder finder = mock(ContactFinder.class);

        when(finder.findContactId(RECOGNIZED_NUMBER)).thenReturn("1");
        when(finder.findContactId(UNRECOGNIZED_NUMBER)).thenReturn(null);

        return finder;
    }

    private RuleChecker createChecker(RuleEntity[] rules) {
        return createChecker(rules, ALL_ACTIVE);
    }

    private RuleChecker createChecker(RuleEntity[] rules, CapabilityResolver capabilityResolver) {
        return new RuleChecker(
                new RuleHandlerManager(createFinderMock()),
                rules,
                capabilityResolver
        );
    }

    private void assertAction(RuleChecker checker, String number, RuleAction expected) {
        assertEquals(expected, checker.checkAction(new CallDetails(number)).getAction());
    }

    @Test
    public void checkAction_noRules_returnsAllow() {
        RuleChecker ruleChecker = createChecker(new RuleEntity[0]);

        assertAction(ruleChecker, null, RuleAction.ALLOW);
        assertAction(ruleChecker, RECOGNIZED_NUMBER, RuleAction.ALLOW);
        assertAction(ruleChecker, UNRECOGNIZED_NUMBER, RuleAction.ALLOW);
    }

    @Test
    public void checkAction_unmatchedRule_matchesAllCalls() {
        RuleChecker ruleChecker = createChecker(new RuleEntity[]{
                new RuleEntity(RuleType.UNMATCHED, RuleAction.ALLOW, null, true, 0)
        });

        assertAction(ruleChecker, null, RuleAction.ALLOW);
        assertAction(ruleChecker, RECOGNIZED_NUMBER, RuleAction.ALLOW);
        assertAction(ruleChecker, UNRECOGNIZED_NUMBER, RuleAction.ALLOW);

        ruleChecker = createChecker(new RuleEntity[]{
                new RuleEntity(RuleType.UNMATCHED, RuleAction.BLOCK, null, true, 0)
        });

        assertAction(ruleChecker, null, RuleAction.BLOCK);
        assertAction(ruleChecker, RECOGNIZED_NUMBER, RuleAction.BLOCK);
        assertAction(ruleChecker, UNRECOGNIZED_NUMBER, RuleAction.BLOCK);
    }

    @Test
    public void checkAction_higherPriorityBlockRule_blocksCall() {
        String code = RECOGNIZED_NUMBER.substring(0, 3);
        RuleChecker ruleChecker = createChecker(new RuleEntity[]{
                new RuleEntity(RuleType.AREA_CODE, RuleAction.BLOCK, code, true, 2),
                new RuleEntity(RuleType.RECOGNIZED, RuleAction.ALLOW, null, true, 0),
        });

        assertAction(ruleChecker, RECOGNIZED_NUMBER, RuleAction.BLOCK);
    }

    @Test
    public void checkAction_whitelistConfig_allowsRecognizedOnly() {
        RuleChecker ruleChecker = createChecker(new RuleEntity[]{
                new RuleEntity(RuleType.RECOGNIZED, RuleAction.ALLOW, null, true, 2),
                new RuleEntity(RuleType.UNMATCHED, RuleAction.BLOCK, null, true, 0),
        });

        assertAction(ruleChecker, null, RuleAction.BLOCK);
        assertAction(ruleChecker, UNRECOGNIZED_NUMBER, RuleAction.BLOCK);
        assertAction(ruleChecker, RECOGNIZED_NUMBER, RuleAction.ALLOW);
    }

    @Test
    public void checkAction_blacklistConfig_blocksPrivateAndUnrecognized() {
        RuleChecker ruleChecker = createChecker(new RuleEntity[]{
                new RuleEntity(RuleType.PRIVATE, RuleAction.BLOCK, null, true, 4),
                new RuleEntity(RuleType.UNRECOGNIZED, RuleAction.BLOCK, null, true, 2),
                new RuleEntity(RuleType.UNMATCHED, RuleAction.ALLOW, null, true, 0),
        });

        assertAction(ruleChecker, null, RuleAction.BLOCK);
        assertAction(ruleChecker, UNRECOGNIZED_NUMBER, RuleAction.BLOCK);
        assertAction(ruleChecker, RECOGNIZED_NUMBER, RuleAction.ALLOW);
    }

    @Test
    public void checkAction_disabledRules_allowsAllCalls() {
        RuleChecker ruleChecker = createChecker(new RuleEntity[]{
                new RuleEntity(RuleType.PRIVATE, RuleAction.BLOCK, null, false, 6),
                new RuleEntity(RuleType.UNRECOGNIZED, RuleAction.BLOCK, null, false, 4),
                new RuleEntity(RuleType.RECOGNIZED, RuleAction.BLOCK, null, false, 2),
                new RuleEntity(RuleType.UNMATCHED, RuleAction.ALLOW, null, true, 0),
        });

        assertAction(ruleChecker, null, RuleAction.ALLOW);
        assertAction(ruleChecker, UNRECOGNIZED_NUMBER, RuleAction.ALLOW);
        assertAction(ruleChecker, RECOGNIZED_NUMBER, RuleAction.ALLOW);
    }

    @Test
    public void checkAction_areaCode_blocked() {
        RuleChecker ruleChecker = createChecker(new RuleEntity[]{
                new RuleEntity(RuleType.AREA_CODE, RuleAction.BLOCK, "800", true, 4),
                new RuleEntity(RuleType.UNMATCHED, RuleAction.ALLOW, null, true, 0),
        });

        assertAction(ruleChecker, "8005551234", RuleAction.BLOCK);
    }

    @Test
    public void checkAction_wildcardStar_blocked() {
        RuleChecker ruleChecker = createChecker(new RuleEntity[]{
                new RuleEntity(RuleType.MATCH, RuleAction.BLOCK, "555*", true, 4),
                new RuleEntity(RuleType.UNMATCHED, RuleAction.ALLOW, null, true, 0),
        });

        assertAction(ruleChecker, "5551234", RuleAction.BLOCK);
    }

    @Test
    public void checkAction_exactMatch_blocked() {
        RuleChecker ruleChecker = createChecker(new RuleEntity[]{
                new RuleEntity(RuleType.MATCH, RuleAction.BLOCK, "5551234", true, 4),
                new RuleEntity(RuleType.UNMATCHED, RuleAction.ALLOW, null, true, 0),
        });

        assertAction(ruleChecker, "5551234", RuleAction.BLOCK);
    }

    @Test
    public void checkAction_questionMarkMatch_blocked() {
        RuleChecker ruleChecker = createChecker(new RuleEntity[]{
                new RuleEntity(RuleType.MATCH, RuleAction.BLOCK, "555????", true, 4),
                new RuleEntity(RuleType.UNMATCHED, RuleAction.ALLOW, null, true, 0),
        });

        assertAction(ruleChecker, "5551234", RuleAction.BLOCK);
    }

    @Test
    public void checkAction_questionMarkNoMatch_allowed() {
        RuleChecker ruleChecker = createChecker(new RuleEntity[]{
                new RuleEntity(RuleType.MATCH, RuleAction.BLOCK, "555????", true, 4),
                new RuleEntity(RuleType.UNMATCHED, RuleAction.ALLOW, null, true, 0),
        });

        assertAction(ruleChecker, "55512345", RuleAction.ALLOW);
    }

    @Test
    public void checkAction_countryCodeNormalized_blocked() {
        RuleChecker ruleChecker = createChecker(new RuleEntity[]{
                new RuleEntity(RuleType.MATCH, RuleAction.BLOCK, "5551234567", true, 4),
                new RuleEntity(RuleType.UNMATCHED, RuleAction.ALLOW, null, true, 0),
        });

        assertAction(ruleChecker, "15551234567", RuleAction.BLOCK);
    }

    @Test
    public void checkAction_verificationPassed_allowed() {
        assumeTrue(Build.VERSION.SDK_INT >= Build.VERSION_CODES.R);
        RuleChecker ruleChecker = createChecker(new RuleEntity[]{
                new RuleEntity(RuleType.VERIFICATION_PASSED, RuleAction.ALLOW, null, true, 4),
                new RuleEntity(RuleType.UNMATCHED, RuleAction.BLOCK, null, true, 0),
        });

        assertEquals(
                RuleAction.ALLOW,
                ruleChecker.checkAction(
                        new CallDetails("5551234", Connection.VERIFICATION_STATUS_PASSED)
                ).getAction()
        );
    }

    @Test
    public void checkAction_verificationFailed_blocked() {
        assumeTrue(Build.VERSION.SDK_INT >= Build.VERSION_CODES.R);
        RuleChecker ruleChecker = createChecker(new RuleEntity[]{
                new RuleEntity(RuleType.VERIFICATION_FAILED, RuleAction.BLOCK, null, true, 4),
                new RuleEntity(RuleType.UNMATCHED, RuleAction.ALLOW, null, true, 0),
        });

        assertEquals(
                RuleAction.BLOCK,
                ruleChecker.checkAction(
                        new CallDetails("5551234", Connection.VERIFICATION_STATUS_FAILED)
                ).getAction()
        );
    }

    @Test
    public void checkAction_verificationNotVerified_allowed() {
        assumeTrue(Build.VERSION.SDK_INT >= Build.VERSION_CODES.R);
        RuleChecker ruleChecker = createChecker(new RuleEntity[]{
                new RuleEntity(RuleType.VERIFICATION_PASSED, RuleAction.ALLOW, null, true, 4),
                new RuleEntity(RuleType.VERIFICATION_FAILED, RuleAction.BLOCK, null, true, 2),
                new RuleEntity(RuleType.UNMATCHED, RuleAction.ALLOW, null, true, 0),
        });

        assertEquals(
                RuleAction.ALLOW,
                ruleChecker.checkAction(
                        new CallDetails("5551234", Connection.VERIFICATION_STATUS_NOT_VERIFIED)
                ).getAction()
        );
    }

    @Test
    public void checkAction_answerAndEndRule_returnsAnswerAndEnd() {
        RuleChecker ruleChecker = createChecker(new RuleEntity[]{
                new RuleEntity(RuleType.UNRECOGNIZED, RuleAction.ANSWER_AND_END, null, true, 0),
        });

        CallDecision decision = ruleChecker.checkAction(new CallDetails(UNRECOGNIZED_NUMBER));

        assertEquals(RuleAction.ANSWER_AND_END, decision.getAction());
        assertFalse(decision.fellBackToBlock());
    }

    @Test
    public void checkAction_hangUpCapabilityInactive_fallsBackToBlock() {
        RuleChecker ruleChecker = createChecker(
                new RuleEntity[]{
                        new RuleEntity(RuleType.UNRECOGNIZED, RuleAction.ANSWER_AND_END, null, true, 0),
                },
                capability -> capability != Capability.HANG_UP
        );

        CallDecision decision = ruleChecker.checkAction(new CallDetails(UNRECOGNIZED_NUMBER));

        assertEquals(RuleAction.BLOCK, decision.getAction());
        assertTrue(decision.fellBackToBlock());
    }

    @Test
    public void checkAction_blockRule_isNotMarkedAsFallback() {
        RuleChecker ruleChecker = createChecker(new RuleEntity[]{
                new RuleEntity(RuleType.UNMATCHED, RuleAction.BLOCK, null, true, 0),
        });

        CallDecision decision = ruleChecker.checkAction(new CallDetails(UNRECOGNIZED_NUMBER));

        assertEquals(RuleAction.BLOCK, decision.getAction());
        assertFalse(decision.fellBackToBlock());
    }

    @Test
    public void checkAction_contactsCapabilityInactive_skipsContactRules() {
        RuleChecker ruleChecker = createChecker(
                new RuleEntity[]{
                        new RuleEntity(RuleType.UNRECOGNIZED, RuleAction.BLOCK, null, true, 2),
                        new RuleEntity(RuleType.UNMATCHED, RuleAction.ALLOW, null, true, 0),
                },
                capability -> capability != Capability.CONTACTS_MATCHING
        );

        // Without contacts access the unrecognized rule cannot be evaluated; the call falls
        // through to the unmatched allow rule instead of being blocked on missing data.
        assertAction(ruleChecker, UNRECOGNIZED_NUMBER, RuleAction.ALLOW);
    }
}
