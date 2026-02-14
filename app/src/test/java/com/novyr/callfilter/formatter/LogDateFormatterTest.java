package com.novyr.callfilter.formatter;

import com.novyr.callfilter.db.entity.LogEntity;
import com.novyr.callfilter.db.entity.enums.LogAction;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;

import static org.junit.Assert.assertEquals;

public class LogDateFormatterTest {
    private Locale mOriginalLocale;
    private TimeZone mOriginalTimezone;

    @Before
    public void setUp() {
        mOriginalLocale = Locale.getDefault();
        mOriginalTimezone = TimeZone.getDefault();
    }

    @After
    public void tearDown() {
        Locale.setDefault(mOriginalLocale);
        TimeZone.setDefault(mOriginalTimezone);
    }

    private static int getJavaVersion() {
        String version = System.getProperty("java.specification.version");
        if (version.startsWith("1.")) {
            version = version.substring(2);
        }
        int dotIndex = version.indexOf('.');
        if (dotIndex != -1) {
            version = version.substring(0, dotIndex);
        }
        return Integer.parseInt(version);
    }

    @Test
    public void testFormatter() {
        LogDateFormatter formatter = new LogDateFormatter();
        int jdkVersion = getJavaVersion();

        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));

        Locale locale = new Locale("en");
        Locale.setDefault(locale);

        LogEntity log = new LogEntity(
                buildCalendar(1980, 5, 8, 9, 22, 0),
                LogAction.ALLOWED,
                null
        );

        String expectedEn;
        if (jdkVersion >= 20) {
            expectedEn = "6/8/80, 9:22\u202FAM";
        } else if (jdkVersion >= 9) {
            expectedEn = "6/8/80, 9:22 AM";
        } else {
            expectedEn = "6/8/80 9:22 AM";
        }
        assertEquals(expectedEn, formatter.formatDate(log));

        locale = new Locale("ja");
        Locale.setDefault(locale);

        log.setCreated(buildCalendar(2020, 11, 25, 12, 5, 59));

        String expectedJa;
        if (jdkVersion >= 9) {
            expectedJa = "2020/12/25 12:05";
        } else {
            expectedJa = "20/12/25 12:05";
        }
        assertEquals(expectedJa, formatter.formatDate(log));
    }

    private Calendar buildCalendar(int year, int month, int day, int hour, int minute, int second) {
        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        calendar.set(Calendar.AM_PM, 0);
        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.MONTH, month);
        calendar.set(Calendar.DATE, day);
        calendar.set(Calendar.HOUR, hour);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, second);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar;
    }
}
