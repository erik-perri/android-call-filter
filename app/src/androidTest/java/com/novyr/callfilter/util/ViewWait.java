package com.novyr.callfilter.util;

import static androidx.test.espresso.Espresso.onView;

import android.os.SystemClock;
import android.view.View;

import androidx.test.espresso.ViewAssertion;

import org.hamcrest.Matcher;

/**
 * Retries an Espresso check until it passes or a timeout elapses.
 *
 * Espresso only waits for the main looper, but list contents arrive via Room's
 * LiveData, which re-queries on a background executor after every write and
 * then posts the result to the main thread. A single onView().check() can run
 * before that post lands. Use this for any assertion (or as a precondition for
 * any action) that depends on database contents being reflected in the UI.
 */
public final class ViewWait {
    private static final long DEFAULT_TIMEOUT_MS = 10_000;
    private static final long POLL_INTERVAL_MS = 100;

    private ViewWait() {
    }

    public static void waitUntil(Matcher<View> viewMatcher, ViewAssertion assertion) {
        waitUntil(viewMatcher, assertion, DEFAULT_TIMEOUT_MS);
    }

    public static void waitUntil(Matcher<View> viewMatcher, ViewAssertion assertion, long timeoutMs) {
        long deadline = SystemClock.uptimeMillis() + timeoutMs;
        while (true) {
            try {
                onView(viewMatcher).check(assertion);
                return;
            } catch (RuntimeException | AssertionError e) {
                // NoMatchingViewException is a RuntimeException; failed matches()
                // assertions surface as AssertionError subclasses.
                if (SystemClock.uptimeMillis() >= deadline) {
                    throw e;
                }
            }
            SystemClock.sleep(POLL_INTERVAL_MS);
        }
    }
}
