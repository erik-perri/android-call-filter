package com.novyr.callfilter;

import com.google.i18n.phonenumbers.PhoneNumberUtil;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class AreaCodeExtractorTest {
    @Test
    public void checkInvalid() {
        AreaCodeExtractor extractor = new AreaCodeExtractor(PhoneNumberUtil.getInstance());

        assertNull(extractor.extract(null));
        assertNull(extractor.extract("1"));
        assertNull(extractor.extract("invalid"));
        assertNull(extractor.extract("230589-9843276723"));
    }

    @Test
    public void checkLocal() {
        AreaCodeExtractor extractor = new AreaCodeExtractor(PhoneNumberUtil.getInstance());

        assertNull(extractor.extract("5551234"));
    }

    @Test
    public void checkVariant() {
        AreaCodeExtractor extractor = new AreaCodeExtractor(PhoneNumberUtil.getInstance());

        assertEquals(extractor.extract("18005551234"), "800");
        assertEquals(extractor.extract("8005551234"), "800");
        assertEquals(extractor.extract("800.555.1234"), "800");
        assertEquals(extractor.extract("800-555-1234"), "800");
        assertEquals(extractor.extract("(800) 555-1234"), "800");
        assertEquals(extractor.extract("1 (800) 555 1234"), "800");
    }
}
