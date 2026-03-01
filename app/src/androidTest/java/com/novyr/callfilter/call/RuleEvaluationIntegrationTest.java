package com.novyr.callfilter.call;

import android.content.Context;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.test.filters.MediumTest;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.rule.GrantPermissionRule;

import com.novyr.callfilter.CallDetails;
import com.novyr.callfilter.RuleChecker;
import com.novyr.callfilter.RuleCheckerFactory;
import com.novyr.callfilter.db.entity.RuleEntity;
import com.novyr.callfilter.db.entity.enums.RuleAction;
import com.novyr.callfilter.db.entity.enums.RuleType;
import com.novyr.callfilter.util.ContactHelper;
import com.novyr.callfilter.util.DatabaseHelper;
import com.novyr.callfilter.util.PermissionHelper;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.Collection;

import static org.junit.Assert.assertEquals;
import static org.junit.Assume.assumeTrue;

@RunWith(Parameterized.class)
@MediumTest
public class RuleEvaluationIntegrationTest {

    private static final String CONTACT_NUMBER = "5551234";
    private static final String UNKNOWN_NUMBER = "5559999";

    private static final int STATUS_NOT_VERIFIED = 0;
    private static final int STATUS_PASSED = 1;
    private static final int STATUS_FAILED = 2;

    @Rule
    public GrantPermissionRule permissions = PermissionHelper.grantAllPermissions();

    @Parameterized.Parameter
    public TestCase testCase;

    private DatabaseHelper dbHelper;
    private ContactHelper contactHelper;
    private Context context;

    @Parameterized.Parameters(name = "{0}")
    public static Collection<TestCase> data() {
        return Arrays.asList(
                TestCase.create("allowCall_recognizedContact_allowed")
                        .withRules(
                                rule(RuleType.RECOGNIZED, RuleAction.ALLOW, null, true, 4),
                                rule(RuleType.UNMATCHED, RuleAction.BLOCK, null, true, 0))
                        .withContact()
                        .calling(CONTACT_NUMBER)
                        .expectAllowed(),

                TestCase.create("allowCall_unrecognizedNumber_blocked")
                        .withRules(
                                rule(RuleType.UNRECOGNIZED, RuleAction.BLOCK, null, true, 4),
                                rule(RuleType.UNMATCHED, RuleAction.ALLOW, null, true, 0))
                        .calling(UNKNOWN_NUMBER)
                        .expectBlocked(),

                TestCase.create("allowCall_privateCall_blocked")
                        .withRules(
                                rule(RuleType.PRIVATE, RuleAction.BLOCK, null, true, 4),
                                rule(RuleType.UNMATCHED, RuleAction.ALLOW, null, true, 0))
                        .calling(null)
                        .expectBlocked(),

                TestCase.create("allowCall_areaCode800_blocked")
                        .withRules(
                                rule(RuleType.AREA_CODE, RuleAction.BLOCK, "800", true, 4),
                                rule(RuleType.UNMATCHED, RuleAction.ALLOW, null, true, 0))
                        .calling("8005551234")
                        .expectBlocked(),

                TestCase.create("allowCall_wildcardStar_blocked")
                        .withRules(
                                rule(RuleType.MATCH, RuleAction.BLOCK, "555*", true, 4),
                                rule(RuleType.UNMATCHED, RuleAction.ALLOW, null, true, 0))
                        .calling("5551234")
                        .expectBlocked(),

                TestCase.create("allowCall_exactMatch_blocked")
                        .withRules(
                                rule(RuleType.MATCH, RuleAction.BLOCK, "5551234", true, 4),
                                rule(RuleType.UNMATCHED, RuleAction.ALLOW, null, true, 0))
                        .calling("5551234")
                        .expectBlocked(),

                TestCase.create("allowCall_questionMarkMatch_blocked")
                        .withRules(
                                rule(RuleType.MATCH, RuleAction.BLOCK, "555????", true, 4),
                                rule(RuleType.UNMATCHED, RuleAction.ALLOW, null, true, 0))
                        .calling("5551234")
                        .expectBlocked(),

                TestCase.create("allowCall_questionMarkNoMatch_allowed")
                        .withRules(
                                rule(RuleType.MATCH, RuleAction.BLOCK, "555????", true, 4),
                                rule(RuleType.UNMATCHED, RuleAction.ALLOW, null, true, 0))
                        .calling("55512345")
                        .expectAllowed(),

                TestCase.create("allowCall_countryCodeNormalized_blocked")
                        .withRules(
                                rule(RuleType.MATCH, RuleAction.BLOCK, "5551234567", true, 4),
                                rule(RuleType.UNMATCHED, RuleAction.ALLOW, null, true, 0))
                        .calling("15551234567")
                        .expectBlocked(),

                TestCase.create("allowCall_firstMatchWins_allowed")
                        .withRules(
                                rule(RuleType.RECOGNIZED, RuleAction.ALLOW, null, true, 4),
                                rule(RuleType.UNMATCHED, RuleAction.BLOCK, null, true, 0))
                        .withContact()
                        .calling(CONTACT_NUMBER)
                        .expectAllowed(),

                TestCase.create("allowCall_disabledRule_allowed")
                        .withRules(
                                rule(RuleType.UNRECOGNIZED, RuleAction.BLOCK, null, false, 4),
                                rule(RuleType.UNMATCHED, RuleAction.ALLOW, null, true, 0))
                        .calling(UNKNOWN_NUMBER)
                        .expectAllowed(),

                TestCase.create("allowCall_allRulesDisabled_allowed")
                        .withRules(
                                rule(RuleType.UNRECOGNIZED, RuleAction.BLOCK, null, false, 4),
                                rule(RuleType.PRIVATE, RuleAction.BLOCK, null, false, 2),
                                rule(RuleType.UNMATCHED, RuleAction.BLOCK, null, false, 0))
                        .calling(UNKNOWN_NUMBER)
                        .expectAllowed(),

                TestCase.create("allowCall_emptyRuleSet_allowed")
                        .withRules()
                        .calling(UNKNOWN_NUMBER)
                        .expectAllowed(),

                TestCase.create("allowCall_verificationPassed_allowed")
                        .withRules(
                                rule(RuleType.VERIFICATION_PASSED, RuleAction.ALLOW, null, true, 4),
                                rule(RuleType.UNMATCHED, RuleAction.BLOCK, null, true, 0))
                        .calling("5551234", STATUS_PASSED)
                        .requiresApi(Build.VERSION_CODES.R)
                        .expectAllowed(),

                TestCase.create("allowCall_verificationFailed_blocked")
                        .withRules(
                                rule(RuleType.VERIFICATION_FAILED, RuleAction.BLOCK, null, true, 4),
                                rule(RuleType.UNMATCHED, RuleAction.ALLOW, null, true, 0))
                        .calling("5551234", STATUS_FAILED)
                        .requiresApi(Build.VERSION_CODES.R)
                        .expectBlocked(),

                TestCase.create("allowCall_verificationNotVerified_allowed")
                        .withRules(
                                rule(RuleType.VERIFICATION_PASSED, RuleAction.ALLOW, null, true, 4),
                                rule(RuleType.VERIFICATION_FAILED, RuleAction.BLOCK, null, true, 2),
                                rule(RuleType.UNMATCHED, RuleAction.ALLOW, null, true, 0))
                        .calling("5551234", STATUS_NOT_VERIFIED)
                        .requiresApi(Build.VERSION_CODES.R)
                        .expectAllowed()
        );
    }

    @Before
    public void setUp() throws Exception {
        if (testCase.minApi > 0) {
            assumeTrue(
                    "Test requires API " + testCase.minApi + " or higher",
                    Build.VERSION.SDK_INT >= testCase.minApi
            );
        }

        context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        dbHelper = new DatabaseHelper();
        contactHelper = new ContactHelper();

        dbHelper.clearLogs();
        dbHelper.resetRules(testCase.rules);

        if (testCase.needsContact) {
            contactHelper.insertContact("Test Contact", CONTACT_NUMBER);
        }
    }

    @After
    public void tearDown() {
        dbHelper.clearLogs();
        dbHelper.resetRules();
        contactHelper.cleanupContacts();
    }

    @Test
    public void allowCall_evaluatesRules_matchesExpected() {
        RuleChecker checker = RuleCheckerFactory.create(context);
        assertEquals(testCase.expectedAllowed, checker.allowCall(testCase.callDetails));
    }

    private static RuleEntity rule(RuleType type, RuleAction action, String value,
                                   boolean enabled, int order) {
        return new RuleEntity(type, action, value, enabled, order);
    }

    public static class TestCase {
        final String name;
        RuleEntity[] rules = new RuleEntity[0];
        boolean needsContact;
        CallDetails callDetails;
        int minApi;
        boolean expectedAllowed;

        private TestCase(String name) {
            this.name = name;
        }

        static TestCase create(String name) {
            return new TestCase(name);
        }

        TestCase withRules(RuleEntity... rules) {
            this.rules = rules;
            return this;
        }

        TestCase withContact() {
            this.needsContact = true;
            return this;
        }

        TestCase calling(String number) {
            this.callDetails = new CallDetails(number);
            return this;
        }

        TestCase calling(String number, int verificationStatus) {
            this.callDetails = new CallDetails(number, verificationStatus);
            return this;
        }

        TestCase requiresApi(int minApi) {
            this.minApi = minApi;
            return this;
        }

        TestCase expectAllowed() {
            this.expectedAllowed = true;
            return this;
        }

        TestCase expectBlocked() {
            this.expectedAllowed = false;
            return this;
        }

        @NonNull
        @Override
        public String toString() {
            return name;
        }
    }
}
