package com.novyr.callfilter.db.converter;

import org.junit.Test;

import java.util.Calendar;
import java.util.TimeZone;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class CalendarConverterTest {
    @Test
    public void testToCalendar() {
        assertEquals(
                buildCalendar(1970, 0, 1, 0, 0, 0),
                CalendarConverter.toCalendar(0L)
        );
        assertEquals(
                buildCalendar(2020, Calendar.DECEMBER, 25, 1, 20, 30),
                CalendarConverter.toCalendar(1608859230L * 1000L)
        );
        assertNull(CalendarConverter.toCalendar(null));
    }

    @Test
    public void testFromCalendar() {
        assertEquals(
                (Long) 0L,
                CalendarConverter.fromCalendar(buildCalendar(1970, 0, 1, 0, 0, 0))
        );
        assertEquals(
                (Long) (1608887355L * 1000L),
                CalendarConverter.fromCalendar(buildCalendar(2020, Calendar.DECEMBER, 25, 9, 9, 15))
        );
        assertNull(CalendarConverter.fromCalendar(null));
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
