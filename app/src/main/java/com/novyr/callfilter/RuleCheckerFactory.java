package com.novyr.callfilter;

import android.content.Context;

import com.novyr.callfilter.db.RuleRepository;
import com.novyr.callfilter.db.entity.RuleEntity;
import com.novyr.callfilter.rules.RuleHandlerManager;

public class RuleCheckerFactory {
    public static RuleChecker create(final Context context) {
        RuleRepository repo = ((CallFilterApplication) context.getApplicationContext())
                .getRuleRepository();
        return new RuleChecker(
                new RuleHandlerManager(new ContactFinder(context)),
                repo.findEnabled().toArray(new RuleEntity[0])
        );
    }
}
