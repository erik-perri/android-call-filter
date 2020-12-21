package com.novyr.callfilter;

import com.novyr.callfilter.db.entity.RuleEntity;
import com.novyr.callfilter.db.entity.enums.RuleAction;
import com.novyr.callfilter.db.entity.enums.RuleType;
import com.novyr.callfilter.rules.RuleHandlerManager;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
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
    public void checkNoEntity() {
        RuleChecker ruleChecker = new RuleChecker(
                new RuleHandlerManager(createFinderMock()),
                new RuleEntity[0]
        );

        assertTrue(ruleChecker.allowCall(null));
        assertTrue(ruleChecker.allowCall(RECOGNIZED_NUMBER));
        assertTrue(ruleChecker.allowCall(UNRECOGNIZED_NUMBER));
    }

    @Test
    public void checkUnmatched() {
        RuleChecker ruleChecker = new RuleChecker(
                new RuleHandlerManager(createFinderMock()),
                new RuleEntity[]{
                        new RuleEntity(RuleType.UNMATCHED, RuleAction.ALLOW, null, true, 0)
                }
        );

        assertTrue(ruleChecker.allowCall(null));
        assertTrue(ruleChecker.allowCall(RECOGNIZED_NUMBER));
        assertTrue(ruleChecker.allowCall(UNRECOGNIZED_NUMBER));

        ruleChecker = new RuleChecker(
                new RuleHandlerManager(createFinderMock()),
                new RuleEntity[]{
                        new RuleEntity(RuleType.UNMATCHED, RuleAction.BLOCK, null, true, 0)
                }
        );

        assertFalse(ruleChecker.allowCall(null));
        assertFalse(ruleChecker.allowCall(RECOGNIZED_NUMBER));
        assertFalse(ruleChecker.allowCall(UNRECOGNIZED_NUMBER));
    }

    @Test
    public void checkOrder() {
        String code = RECOGNIZED_NUMBER.substring(0, 3);
        RuleChecker ruleChecker = new RuleChecker(
                new RuleHandlerManager(createFinderMock()),
                new RuleEntity[]{
                        new RuleEntity(RuleType.AREA_CODE, RuleAction.BLOCK, code, true, 2),
                        new RuleEntity(RuleType.RECOGNIZED, RuleAction.ALLOW, null, true, 0),
                }
        );

        assertFalse(ruleChecker.allowCall(RECOGNIZED_NUMBER));
    }

    @Test
    public void checkWhitelist() {
        RuleChecker ruleChecker = new RuleChecker(
                new RuleHandlerManager(createFinderMock()),
                new RuleEntity[]{
                        new RuleEntity(RuleType.RECOGNIZED, RuleAction.ALLOW, null, true, 2),
                        new RuleEntity(RuleType.UNMATCHED, RuleAction.BLOCK, null, true, 0),
                }
        );

        assertFalse(ruleChecker.allowCall(null));
        assertFalse(ruleChecker.allowCall(UNRECOGNIZED_NUMBER));
        assertTrue(ruleChecker.allowCall(RECOGNIZED_NUMBER));
    }

    @Test
    public void checkBlacklist() {
        RuleChecker ruleChecker = new RuleChecker(
                new RuleHandlerManager(createFinderMock()),
                new RuleEntity[]{
                        new RuleEntity(RuleType.PRIVATE, RuleAction.BLOCK, null, true, 4),
                        new RuleEntity(RuleType.UNRECOGNIZED, RuleAction.BLOCK, null, true, 2),
                        new RuleEntity(RuleType.UNMATCHED, RuleAction.ALLOW, null, true, 0),
                }
        );

        assertFalse(ruleChecker.allowCall(null));
        assertFalse(ruleChecker.allowCall(UNRECOGNIZED_NUMBER));
        assertTrue(ruleChecker.allowCall(RECOGNIZED_NUMBER));
    }

    @Test
    public void checkIgnoreDisabled() {
        RuleChecker ruleChecker = new RuleChecker(
                new RuleHandlerManager(createFinderMock()),
                new RuleEntity[]{
                        new RuleEntity(RuleType.PRIVATE, RuleAction.BLOCK, null, false, 6),
                        new RuleEntity(RuleType.UNRECOGNIZED, RuleAction.BLOCK, null, false, 4),
                        new RuleEntity(RuleType.RECOGNIZED, RuleAction.BLOCK, null, false, 2),
                        new RuleEntity(RuleType.UNMATCHED, RuleAction.ALLOW, null, true, 0),
                }
        );

        assertTrue(ruleChecker.allowCall(null));
        assertTrue(ruleChecker.allowCall(UNRECOGNIZED_NUMBER));
        assertTrue(ruleChecker.allowCall(RECOGNIZED_NUMBER));
    }
}