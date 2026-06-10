package com.novyr.callfilter;

import com.novyr.callfilter.db.entity.RuleEntity;
import com.novyr.callfilter.db.entity.enums.RuleAction;
import com.novyr.callfilter.permissions.Capability;
import com.novyr.callfilter.permissions.CapabilityResolver;
import com.novyr.callfilter.rules.RuleHandlerInterface;
import com.novyr.callfilter.rules.RuleHandlerManager;

public class RuleChecker {
    private final RuleHandlerManager mHandlerManager;
    private final RuleEntity[] mRules;
    private final CapabilityResolver mCapabilityResolver;

    RuleChecker(
            RuleHandlerManager handlerManager,
            RuleEntity[] rules,
            CapabilityResolver capabilityResolver
    ) {
        mHandlerManager = handlerManager;
        mRules = rules;
        mCapabilityResolver = capabilityResolver;
    }

    /**
     * Evaluate the rules against the call and resolve the action of the first matching enabled
     * rule to one this device can currently perform. Rules whose type depends on an inactive
     * capability (e.g. contacts matching without the contacts permission) are skipped so they
     * never match on missing data; a matched ANSWER_AND_END without the hang-up capability
     * degrades to BLOCK, flagged on the decision so the log can say so.
     */
    public CallDecision checkAction(CallDetails details) {
        for (RuleEntity rule : mRules) {
            if (!rule.isEnabled() || !canEvaluate(rule)) {
                continue;
            }

            RuleHandlerInterface ruleHandler = mHandlerManager.findHandler(rule.getType());
            if (ruleHandler == null) {
                continue;
            }

            if (ruleHandler.isMatch(details, rule.getValue())) {
                return resolve(rule.getAction());
            }
        }

        // If no rules matched we don't block anything
        return new CallDecision(RuleAction.ALLOW, false);
    }

    private boolean canEvaluate(RuleEntity rule) {
        Capability required = rule.getType().getRequiredCapability();

        return required == null || mCapabilityResolver.isActive(required);
    }

    private CallDecision resolve(RuleAction action) {
        Capability required = action.getRequiredCapability();
        if (required != null && !mCapabilityResolver.isActive(required)) {
            return new CallDecision(RuleAction.BLOCK, true);
        }

        return new CallDecision(action, false);
    }
}
