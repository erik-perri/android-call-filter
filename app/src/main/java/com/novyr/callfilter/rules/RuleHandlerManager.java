package com.novyr.callfilter.rules;

import android.os.Build;

import androidx.annotation.NonNull;

import com.google.i18n.phonenumbers.PhoneNumberUtil;

import com.novyr.callfilter.AreaCodeExtractor;
import com.novyr.callfilter.ContactFinder;
import com.novyr.callfilter.db.entity.enums.RuleType;

import java.util.Hashtable;

public class RuleHandlerManager {
    private final Hashtable<RuleType, RuleHandlerInterface> mKnownHandlers;

    public RuleHandlerManager(@NonNull ContactFinder contactFinder) {
        mKnownHandlers = buildHandlers(contactFinder);
    }

    private Hashtable<RuleType, RuleHandlerInterface> buildHandlers(
            @NonNull ContactFinder contactFinder
    ) {
        Hashtable<RuleType, RuleHandlerInterface> rules = new Hashtable<>();

        PhoneNumberUtil phoneNumberUtil = PhoneNumberUtil.getInstance();

        rules.put(RuleType.UNMATCHED, new UnmatchedRuleHandler());
        rules.put(RuleType.PRIVATE, new PrivateRuleHandler());
        rules.put(RuleType.UNRECOGNIZED, new UnrecognizedRuleHandler(contactFinder));
        rules.put(RuleType.RECOGNIZED, new RecognizedRuleHandler(contactFinder));
        rules.put(
                RuleType.AREA_CODE,
                new AreaCodeRuleHandler(new AreaCodeExtractor(phoneNumberUtil))
        );
        rules.put(RuleType.MATCH, new MatchRuleHandler());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            rules.put(RuleType.VERIFICATION_FAILED, new VerificationFailedRuleHandler());
            rules.put(RuleType.VERIFICATION_PASSED, new VerificationPassedRuleHandler());
        }

        return rules;
    }

    public RuleHandlerInterface findHandler(RuleType type) {
        return mKnownHandlers.get(type);
    }
}
