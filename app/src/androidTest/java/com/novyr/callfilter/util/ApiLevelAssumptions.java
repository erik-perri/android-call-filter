package com.novyr.callfilter.util;

import android.os.Build;

import static org.junit.Assume.assumeTrue;

public class ApiLevelAssumptions {
    private ApiLevelAssumptions() {
    }

    public static void assumePreQ() {
        assumeTrue(
                "Test requires pre-Q (API < 29)",
                Build.VERSION.SDK_INT < Build.VERSION_CODES.Q
        );
    }

    public static void assumeQOrHigher() {
        assumeTrue(
                "Test requires Q or higher (API >= 29)",
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q
        );
    }

    public static void assumeROrHigher() {
        assumeTrue(
                "Test requires R or higher (API >= 30)",
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.R
        );
    }

    public static void assumePieOrHigher() {
        assumeTrue(
                "Test requires Pie or higher (API >= 28)",
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.P
        );
    }
}
