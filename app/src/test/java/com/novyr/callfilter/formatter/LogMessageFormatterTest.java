package com.novyr.callfilter.formatter;

import android.content.res.Resources;

import com.novyr.callfilter.ContactFinder;
import com.novyr.callfilter.R;
import com.novyr.callfilter.db.entity.LogEntity;
import com.novyr.callfilter.db.entity.enums.LogAction;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class LogMessageFormatterTest {
    private final String RECOGNIZED_NAME = "Contact Name";
    private final String RECOGNIZED_NUMBER = "8005551234";
    private final String UNRECOGNIZED_NUMBER = "9005554321";
    private final String NUMBER_PRIVATE = "PRIVATE";
    private final String ACTION_ALLOWED = "ALLOWED";
    private final String ACTION_BLOCKED = "BLOCKED";
    private final String ACTION_FAILED = "FAILED";

    @Before
    public void setUp() {
        when(mMockResources.getString(R.string.log_action_allowed)).thenReturn(ACTION_ALLOWED);
        when(mMockResources.getString(R.string.log_action_blocked)).thenReturn(ACTION_BLOCKED);
        when(mMockResources.getString(R.string.log_action_failed)).thenReturn(ACTION_FAILED);
        when(mMockResources.getString(R.string.log_number_private)).thenReturn(NUMBER_PRIVATE);
        when(mMockResources.getString(R.string.log_message_format)).thenReturn("%1$s: %2$s");
    }

    @Mock
    Resources mMockResources;

    @Test
    public void testRecognized() {
        LogMessageFormatter formatter = new LogMessageFormatter(mMockResources, createFinderMock());

        LogEntity log = new LogEntity(LogAction.ALLOWED, RECOGNIZED_NUMBER);

        assertEquals(
                String.format("%s: %s", ACTION_ALLOWED, RECOGNIZED_NAME),
                formatter.formatMessage(log)
        );
    }

    @Test
    public void testUnrecognized() {
        LogMessageFormatter formatter = new LogMessageFormatter(mMockResources, createFinderMock());

        LogEntity log = new LogEntity(LogAction.ALLOWED, UNRECOGNIZED_NUMBER);

        assertEquals(
                String.format("%s: %s", ACTION_ALLOWED, UNRECOGNIZED_NUMBER),
                formatter.formatMessage(log)
        );
    }

    @Test
    public void testPrivate() {
        LogMessageFormatter formatter = new LogMessageFormatter(mMockResources, createFinderMock());

        LogEntity log = new LogEntity(LogAction.ALLOWED, null);

        assertEquals(
                String.format("%s: %s", ACTION_ALLOWED, NUMBER_PRIVATE),
                formatter.formatMessage(log)
        );
    }

    @Test
    public void testActions() {
        LogMessageFormatter formatter = new LogMessageFormatter(mMockResources, createFinderMock());

        LogEntity log = new LogEntity(LogAction.ALLOWED, null);
        assertEquals(
                String.format("%s: %s", ACTION_ALLOWED, NUMBER_PRIVATE),
                formatter.formatMessage(log)
        );

        log.setAction(LogAction.BLOCKED);
        assertEquals(
                String.format("%s: %s", ACTION_BLOCKED, NUMBER_PRIVATE),
                formatter.formatMessage(log)
        );

        log.setAction(LogAction.FAILED);
        assertEquals(
                String.format("%s: %s", ACTION_FAILED, NUMBER_PRIVATE),
                formatter.formatMessage(log)
        );
    }

    private ContactFinder createFinderMock() {
        ContactFinder finder = mock(ContactFinder.class);

        when(finder.findContactName(RECOGNIZED_NUMBER)).thenReturn(RECOGNIZED_NAME);
        when(finder.findContactName(UNRECOGNIZED_NUMBER)).thenReturn(null);

        return finder;
    }
}
