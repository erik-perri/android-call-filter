package com.novyr.callfilter.rules;

import com.novyr.callfilter.AreaCodeExtractor;
import com.novyr.callfilter.CallDetails;
import com.novyr.callfilter.ContactFinder;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class AreaCodeRuleHandlerTest {
    private static final String INVALID_NUMBER = "1";
    private static final String VALID_NUMBER_1 = "8005551234";
    private static final String VALID_NUMBER_2 = "9005551234";
    private static final String VALID_AREA_CODE_1 = "800";
    private static final String VALID_AREA_CODE_2 = "900";

    private AreaCodeExtractor createExtractorMock() {
        AreaCodeExtractor extractor = mock(AreaCodeExtractor.class);

        when(extractor.extract(INVALID_NUMBER)).thenReturn(null);
        when(extractor.extract(VALID_NUMBER_1)).thenReturn(VALID_AREA_CODE_1);
        when(extractor.extract(VALID_NUMBER_2)).thenReturn(VALID_AREA_CODE_2);

        return extractor;
    }

    @Test
    public void checkPrivateMatch() {
        AreaCodeRuleHandler checker = new AreaCodeRuleHandler(createExtractorMock());

        assertFalse(checker.isMatch(new CallDetails(null), VALID_AREA_CODE_1));
    }

    @Test
    public void checkInvalidMatch() {
        AreaCodeRuleHandler checker = new AreaCodeRuleHandler(createExtractorMock());

        assertFalse(checker.isMatch(new CallDetails(INVALID_NUMBER), VALID_AREA_CODE_1));
        assertFalse(checker.isMatch(new CallDetails(INVALID_NUMBER), null));
    }

    @Test
    public void checkNormalMatch() {
        AreaCodeRuleHandler checker = new AreaCodeRuleHandler(createExtractorMock());

        assertTrue(checker.isMatch(new CallDetails(VALID_NUMBER_1), VALID_AREA_CODE_1));
        assertTrue(checker.isMatch(new CallDetails(VALID_NUMBER_2), VALID_AREA_CODE_2));
        assertFalse(checker.isMatch(new CallDetails(VALID_NUMBER_1), VALID_AREA_CODE_2));
        assertFalse(checker.isMatch(new CallDetails(VALID_NUMBER_2), VALID_AREA_CODE_1));
        assertFalse(checker.isMatch(new CallDetails(VALID_NUMBER_2), null));
    }
}
