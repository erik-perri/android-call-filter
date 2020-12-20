package com.novyr.callfilter;

import android.content.Context;

import com.novyr.callfilter.db.RuleRepository;
import com.novyr.callfilter.db.entity.RuleEntity;
import com.novyr.callfilter.rules.RuleHandlerManager;

public class RuleCheckerFactory {
    private static volatile RuleChecker INSTANCE;

    public static RuleChecker create(final Context context) {
        if (INSTANCE == null) {
            synchronized (RuleChecker.class) {
                if (INSTANCE == null) {
                    RuleRepository repo = ((CallFilterApplication) context.getApplicationContext())
                            .getRuleRepository();
                    INSTANCE = new RuleChecker(
                            new RuleHandlerManager(new ContactFinder(context)),
                            repo.findEnabled().toArray(new RuleEntity[0])
                    );
                }
            }
        }
        return INSTANCE;
    }
}
