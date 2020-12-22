package com.novyr.callfilter.rules;

import android.os.Build;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import com.novyr.callfilter.CallDetails;

@RequiresApi(api = Build.VERSION_CODES.R)
public class VerificationPassedRuleHandler implements RuleHandlerInterface {
    @Override
    public boolean isMatch(@NonNull CallDetails details, @Nullable String ruleValue) {
        if (details.isNotVerified()) {
            return false;
        }

        return details.isVerificationPassed();
    }
}
