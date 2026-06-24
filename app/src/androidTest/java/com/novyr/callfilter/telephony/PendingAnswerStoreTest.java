package com.novyr.callfilter.telephony;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.SmallTest;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Covers the answer-then-hang-up handoff store. The matching logic leans on
 * {@link android.telephony.PhoneNumberUtils#compare} and {@link android.text.TextUtils}, which are
 * stubbed (and throw) in the plain JVM unit set, so these run as instrumentation tests on a real
 * device/emulator.
 *
 * <p>The store is process-scoped static state, so each test resets it before and after to stay
 * isolated.
 */
@RunWith(AndroidJUnit4.class)
@SmallTest
public class PendingAnswerStoreTest {
    @Before
    public void setUp() {
        PendingAnswerStore.reset();
    }

    @After
    public void tearDown() {
        PendingAnswerStore.reset();
    }

    @Test
    public void claim_noMarks_returnsNull() {
        assertNull(PendingAnswerStore.claim("5551234"));
    }

    @Test
    public void claim_matchingNumber_returnsMarkAndConsumesIt() {
        PendingAnswerStore.markAnswerNextRinging("5551234");

        PendingAnswerStore.Claim claim = PendingAnswerStore.claim("5551234");
        assertNotNull(claim);
        assertEquals("5551234", claim.getNumber());

        // The claim is atomic: a duplicate RINGING broadcast for the same call must not answer it
        // a second time.
        assertNull(PendingAnswerStore.claim("5551234"));
    }

    @Test
    public void claim_nonMatchingNumber_returnsNullAndLeavesMark() {
        PendingAnswerStore.markAnswerNextRinging("5551234");

        assertNull(PendingAnswerStore.claim("5559999"));

        // The original mark is untouched and still claimable by the call it was meant for.
        PendingAnswerStore.Claim claim = PendingAnswerStore.claim("5551234");
        assertNotNull(claim);
        assertEquals("5551234", claim.getNumber());
    }

    @Test
    public void claim_differentFormattingSameNumber_matches() {
        PendingAnswerStore.markAnswerNextRinging("555-1234");

        // PhoneNumberUtils.compare treats these as the same number.
        PendingAnswerStore.Claim claim = PendingAnswerStore.claim("5551234");
        assertNotNull(claim);
    }

    @Test
    public void claim_numberlessMark_matchesAnyIncomingNumber() {
        // A mark made without a number (handle missing, or a broadcast redacted despite the grant)
        // is a wildcard.
        PendingAnswerStore.markAnswerNextRinging(null);

        PendingAnswerStore.Claim claim = PendingAnswerStore.claim("5551234");
        assertNotNull(claim);
        assertNull(claim.getNumber());
    }

    @Test
    public void claim_numberlessBroadcast_singleMark_returnsIt() {
        // When the RINGING broadcast carries no number, a single outstanding mark is unambiguous.
        PendingAnswerStore.markAnswerNextRinging("5551234");

        PendingAnswerStore.Claim claim = PendingAnswerStore.claim(null);
        assertNotNull(claim);
        assertEquals("5551234", claim.getNumber());
    }

    @Test
    public void claim_numberlessBroadcast_multipleMarks_refusesRatherThanGuess() {
        PendingAnswerStore.markAnswerNextRinging("5551234");
        PendingAnswerStore.markAnswerNextRinging("5559999");

        // With more than one mark and no number to match on, answering the wrong call is the risk,
        // so the store refuses.
        assertNull(PendingAnswerStore.claim(null));
        assertNull(PendingAnswerStore.claim(""));
    }

    @Test
    public void claim_numberlessBroadcast_afterDisambiguating_returnsRemainingMark() {
        PendingAnswerStore.markAnswerNextRinging("5551234");
        PendingAnswerStore.markAnswerNextRinging("5559999");

        // Consume one by number; the single survivor is now unambiguous for a numberless broadcast.
        assertNotNull(PendingAnswerStore.claim("5551234"));

        PendingAnswerStore.Claim claim = PendingAnswerStore.claim(null);
        assertNotNull(claim);
        assertEquals("5559999", claim.getNumber());
    }

    @Test
    public void claim_overlappingMarks_eachClaimedIndependently() {
        // Two answer-and-end calls can overlap within the expiry window (call waiting); each keeps
        // its own claimable mark.
        PendingAnswerStore.markAnswerNextRinging("5551234");
        PendingAnswerStore.markAnswerNextRinging("5559999");

        PendingAnswerStore.Claim second = PendingAnswerStore.claim("5559999");
        assertNotNull(second);
        assertEquals("5559999", second.getNumber());

        PendingAnswerStore.Claim first = PendingAnswerStore.claim("5551234");
        assertNotNull(first);
        assertEquals("5551234", first.getNumber());
    }

    @Test
    public void reset_clearsOutstandingMarks() {
        PendingAnswerStore.markAnswerNextRinging("5551234");

        PendingAnswerStore.reset();

        assertNull(PendingAnswerStore.claim("5551234"));
    }
}
