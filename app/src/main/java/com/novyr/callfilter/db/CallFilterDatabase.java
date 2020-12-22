package com.novyr.callfilter.db;

import android.content.Context;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.novyr.callfilter.db.dao.LogDao;
import com.novyr.callfilter.db.dao.RuleDao;
import com.novyr.callfilter.db.entity.LogEntity;
import com.novyr.callfilter.db.entity.RuleEntity;
import com.novyr.callfilter.db.entity.enums.RuleAction;
import com.novyr.callfilter.db.entity.enums.RuleType;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Database(entities = {LogEntity.class, RuleEntity.class}, version = 2)
public abstract class CallFilterDatabase extends RoomDatabase {
    private static volatile CallFilterDatabase INSTANCE;
    // TODO 4 is from the docs example but seems like a lot for our needs
    private static final int NUMBER_OF_THREADS = 4;
    static final ExecutorService databaseWriteExecutor =
            Executors.newFixedThreadPool(NUMBER_OF_THREADS);

    public static CallFilterDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (CallFilterDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room
                            .databaseBuilder(
                                    context.getApplicationContext(),
                                    CallFilterDatabase.class,
                                    "call_filter"
                            )
                            .addCallback(new RoomDatabase.Callback() {
                                @Override
                                public void onCreate(@NonNull SupportSQLiteDatabase db) {
                                    super.onCreate(db);

                                    createDefaultRules();
                                }
                            })
                            .build();
                }
            }
        }
        return INSTANCE;
    }

    public abstract LogDao logDao();

    public abstract RuleDao ruleDao();

    private static void createDefaultRules() {
        databaseWriteExecutor.execute(() -> {
            RuleDao dao = INSTANCE.ruleDao();
            dao.deleteAll();

            int order = 0;

            RuleEntity rule = new RuleEntity(
                    RuleType.UNMATCHED,
                    RuleAction.ALLOW,
                    null,
                    true,
                    order
            );
            dao.insert(rule);
            order += 2;

            rule = new RuleEntity(
                    RuleType.UNRECOGNIZED,
                    RuleAction.BLOCK,
                    null,
                    false,
                    order
            );
            dao.insert(rule);
            order += 2;

            rule = new RuleEntity(
                    RuleType.PRIVATE,
                    RuleAction.BLOCK,
                    null,
                    false,
                    order
            );
            dao.insert(rule);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                order += 2;

                rule = new RuleEntity(
                        RuleType.VERIFICATION_FAILED,
                        RuleAction.BLOCK,
                        null,
                        false,
                        order
                );
                dao.insert(rule);
                order += 2;

                rule = new RuleEntity(
                        RuleType.VERIFICATION_PASSED,
                        RuleAction.ALLOW,
                        null,
                        false,
                        order
                );
                dao.insert(rule);
            }
        });
    }
}
