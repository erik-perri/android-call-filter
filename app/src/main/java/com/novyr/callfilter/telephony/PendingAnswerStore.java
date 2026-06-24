package com.novyr.callfilter.telephony;

import android.os.SystemClock;
import android.telephony.PhoneNumberUtils;
import android.text.TextUtils;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Process-scoped handoff between {@code CallFilterService} (the CallScreeningService on API 29+)
 * and {@code CallReceiver}. A CallScreeningService cannot answer a call, so for the
 * answer-then-hang-up action it allows the call through (silenced) and marks it here; the
 * PHONE_STATE receiver sees the call reach the ringing state, claims the matching mark, and
 * performs the answer + hang up.
 *
 * <p>This holds a collection of marks rather than a single slot: two answer-and-end calls can
 * overlap within the expiry window (call waiting, or one declined as another arrives) and each
 * needs its own claimable mark. Claiming is atomic and removes the matched entry, so duplicate
 * RINGING broadcasts for the same call cannot trigger a second answer.
 */
public final class PendingAnswerStore {
    /**
     * How long a mark stays live. The screened call reaches the ringing state within a fraction of
     * a second of being marked, so a tight window is enough to claim it while keeping the span in
     * which an unrelated call could be answered by mistake small. Measured against
     * {@link SystemClock#elapsedRealtime()} so a wall-clock change between mark and broadcast can't
     * expire a live mark early or keep a stale one alive.
     */
    private static final long EXPIRY_MS = 3000;

    private static final List<Claim> sPending = new ArrayList<>();

    private PendingAnswerStore() {
    }

    public static synchronized void markAnswerNextRinging(@Nullable String number) {
        prune();
        sPending.add(new Claim(number, SystemClock.elapsedRealtime() + EXPIRY_MS));
    }

    /**
     * Atomically claim a live mark matching the call that just reached the ringing state, if one
     * exists. The number check matters because marks are global to the process: between marking
     * and the RINGING broadcast a different call could start ringing, and without it we would
     * answer whichever call happened to ring next.
     *
     * <p>The answer-then-hang-up capability now requires READ_CALL_LOG, so the broadcast normally
     * carries the number and the per-number match above does the work. A numberless broadcast is
     * the rare fallback (e.g. an OEM that redacts it despite the grant); there, matching is only
     * safe if exactly one mark is outstanding, so with several we refuse rather than risk answering
     * the wrong call.
     *
     * @return The claimed mark, or null if this ringing call is not one we should answer.
     */
    @Nullable
    public static synchronized Claim claim(@Nullable String incomingNumber) {
        prune();

        if (TextUtils.isEmpty(incomingNumber)) {
            return sPending.size() == 1 ? sPending.remove(0) : null;
        }

        for (Iterator<Claim> it = sPending.iterator(); it.hasNext(); ) {
            Claim claim = it.next();
            if (TextUtils.isEmpty(claim.getNumber())
                    || PhoneNumberUtils.compare(claim.getNumber(), incomingNumber)) {
                it.remove();
                return claim;
            }
        }

        return null;
    }

    public static synchronized void reset() {
        sPending.clear();
    }

    private static void prune() {
        long now = SystemClock.elapsedRealtime();
        for (Iterator<Claim> it = sPending.iterator(); it.hasNext(); ) {
            if (now > it.next().mExpiresAt) {
                it.remove();
            }
        }
    }

    public static final class Claim {
        @Nullable
        private final String mNumber;
        private final long mExpiresAt;

        Claim(@Nullable String number, long expiresAt) {
            mNumber = number;
            mExpiresAt = expiresAt;
        }

        @Nullable
        public String getNumber() {
            return mNumber;
        }
    }
}
