package com.novyr.callfilter.rules;

import androidx.annotation.Nullable;

import com.novyr.callfilter.ContactFinder;
import com.novyr.callfilter.db.entity.enums.RuleType;

import java.util.Hashtable;

public class RuleHandlerManager {
    private final Hashtable<RuleType, RuleHandlerInterface> mKnownHandlers;

    public RuleHandlerManager(@Nullable ContactFinder contactFinder) {
        mKnownHandlers = buildHandlers(contactFinder);
    }

    private Hashtable<RuleType, RuleHandlerInterface> buildHandlers(
            @Nullable ContactFinder contactFinder
    ) {
        Hashtable<RuleType, RuleHandlerInterface> rules = new Hashtable<>();

        rules.put(RuleType.UNMATCHED, new UnmatchedRuleHandler());
        rules.put(RuleType.PRIVATE, new PrivateRuleHandler());
        rules.put(RuleType.UNRECOGNIZED, new UnrecognizedRuleHandler(contactFinder));
        rules.put(RuleType.RECOGNIZED, new RecognizedRuleHandler(contactFinder));
        rules.put(RuleType.AREA_CODE, new AreaCodeRuleHandler());
        rules.put(RuleType.MATCH, new MatchRuleHandler());

        return rules;
    }

    public RuleHandlerInterface findHandler(RuleType type) {
        return mKnownHandlers.get(type);
    }
}
