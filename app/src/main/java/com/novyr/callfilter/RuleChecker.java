package com.novyr.callfilter;

import com.novyr.callfilter.db.entity.RuleEntity;
import com.novyr.callfilter.db.entity.enums.RuleAction;
import com.novyr.callfilter.rules.RuleHandlerInterface;
import com.novyr.callfilter.rules.RuleHandlerManager;

public class RuleChecker {
    private final RuleHandlerManager mHandlerManager;
    private final RuleEntity[] mRules;

    RuleChecker(RuleHandlerManager handlerManager, RuleEntity[] rules) {
        mHandlerManager = handlerManager;
        mRules = rules;
    }

    public boolean allowCall(CallDetails details) {
        for (RuleEntity rule : mRules) {
            if (!rule.isEnabled()) {
                continue;
            }

            RuleHandlerInterface ruleHandler = mHandlerManager.findHandler(rule.getType());
            if (ruleHandler == null) {
                continue;
            }

            if (ruleHandler.isMatch(details, rule.getValue())) {
                return rule.getAction() == RuleAction.ALLOW;
            }
        }

        // If no rules processed we don't block anything
        return true;
    }
}
