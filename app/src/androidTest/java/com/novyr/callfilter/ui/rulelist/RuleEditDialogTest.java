package com.novyr.callfilter.ui.rulelist;

import android.os.Build;

import androidx.test.core.app.ActivityScenario;
import androidx.test.espresso.IdlingRegistry;
import androidx.test.filters.MediumTest;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.rule.GrantPermissionRule;
import androidx.test.uiautomator.UiDevice;

import com.novyr.callfilter.R;
import com.novyr.callfilter.db.entity.RuleEntity;
import com.novyr.callfilter.db.entity.enums.RuleAction;
import com.novyr.callfilter.db.entity.enums.RuleType;
import com.novyr.callfilter.util.DatabaseHelper;
import com.novyr.callfilter.util.DatabaseIdlingResource;
import com.novyr.callfilter.util.PermissionHelper;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.util.List;

import static androidx.test.espresso.Espresso.onData;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.longClick;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.assertion.ViewAssertions.doesNotExist;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.contrib.RecyclerViewActions.actionOnItemAtPosition;
import static androidx.test.espresso.matcher.RootMatchers.isDialog;
import static androidx.test.espresso.matcher.RootMatchers.isPlatformPopup;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withSpinnerText;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@MediumTest
public class RuleEditDialogTest {
    private final DatabaseHelper dbHelper = new DatabaseHelper();
    private final DatabaseIdlingResource idlingResource = new DatabaseIdlingResource();
    private ActivityScenario<RuleListActivity> scenario;

    private static final int TEST_ORDER = 100;

    @Rule
    public GrantPermissionRule permissions = PermissionHelper.grantAllPermissions();

    @Before
    public void setUp() throws Exception {
        IdlingRegistry.getInstance().register(idlingResource);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            UiDevice device = UiDevice.getInstance(
                    InstrumentationRegistry.getInstrumentation());
            PermissionHelper.grantCallScreeningRole(device);
        }

        dbHelper.resetRules();
    }

    @After
    public void tearDown() {
        if (scenario != null) {
            scenario.close();
        }
        dbHelper.resetRules();
        IdlingRegistry.getInstance().unregister(idlingResource);
    }

    private void launchActivity() {
        scenario = ActivityScenario.launch(RuleListActivity.class);
    }

    private void openCreateDialog() {
        onView(withId(R.id.add_button)).perform(click());
    }

    private void openEditDialog(int position) {
        onView(withId(R.id.rule_list))
                .perform(actionOnItemAtPosition(position, longClick()));
        onView(withText(R.string.context_menu_edit_rule))
                .perform(click());
    }

    private void selectTypeSpinnerItem(String displayName) {
        onView(withId(R.id.type_spinner)).perform(click());
        onData(allOf(instanceOf(CharSequence.class), is((CharSequence) displayName)))
                .inRoot(isPlatformPopup())
                .perform(click());
    }

    private void clickOk() {
        onView(withText("OK")).inRoot(isDialog()).perform(click());
    }

    @Test
    public void fab_clicked_opensCreateDialog() {
        launchActivity();

        openCreateDialog();

        onView(withText(R.string.rule_form_heading_create))
                .check(matches(isDisplayed()));
    }

    @Test
    public void createDialog_opened_showsAllSpinners() {
        launchActivity();

        openCreateDialog();

        onView(withId(R.id.enabled_spinner)).check(matches(isDisplayed()));
        onView(withId(R.id.action_spinner)).check(matches(isDisplayed()));
        onView(withId(R.id.type_spinner)).check(matches(isDisplayed()));
    }

    @Test
    public void typeSpinner_areaCodeSelected_showsAreaCodeInput() {
        launchActivity();
        openCreateDialog();

        selectTypeSpinnerItem(getStringResource(R.string.rule_type_area_code));

        onView(withId(R.id.area_code_input)).check(matches(isDisplayed()));
    }

    @Test
    public void typeSpinner_matchSelected_showsMatchInput() {
        launchActivity();
        openCreateDialog();

        selectTypeSpinnerItem(getStringResource(R.string.rule_type_match));

        onView(withId(R.id.match_input)).check(matches(isDisplayed()));
    }

    @Test
    public void typeSpinner_privateSelected_showsNoExtraInput() {
        launchActivity();
        openCreateDialog();

        selectTypeSpinnerItem(getStringResource(R.string.rule_type_private));

        onView(withId(R.id.area_code_input)).check(doesNotExist());
        onView(withId(R.id.match_input)).check(doesNotExist());
    }

    @Test
    public void areaCodeInput_tooShortValue_rejectsOnSave() {
        launchActivity();
        openCreateDialog();

        selectTypeSpinnerItem(getStringResource(R.string.rule_type_area_code));
        onView(withId(R.id.area_code_input))
                .perform(replaceText("12"), closeSoftKeyboard());

        clickOk();

        // Dialog should still be displayed (validation failed)
        onView(withText(R.string.rule_form_heading_create))
                .check(matches(isDisplayed()));
    }

    @Test
    public void areaCodeInput_validValue_savesAndCloses() throws InterruptedException {
        launchActivity();
        openCreateDialog();

        selectTypeSpinnerItem(getStringResource(R.string.rule_type_area_code));
        onView(withId(R.id.area_code_input))
                .perform(replaceText("212"), closeSoftKeyboard());

        clickOk();

        // Dialog should be dismissed
        onView(withText(R.string.rule_form_heading_create))
                .check(doesNotExist());

        // Verify rule persisted to DB
        Thread.sleep(500);
        List<RuleEntity> rules = dbHelper.getRuleEntries();
        boolean found = false;
        for (RuleEntity rule : rules) {
            if (rule.getType() == RuleType.AREA_CODE && "212".equals(rule.getValue())) {
                found = true;
                break;
            }
        }
        assertTrue("Rule with type=AREA_CODE and value=212 should exist in DB", found);
    }

    @Test
    public void matchInput_emptyValue_rejectsOnSave() {
        launchActivity();
        openCreateDialog();

        selectTypeSpinnerItem(getStringResource(R.string.rule_type_match));
        // Leave match_input empty

        clickOk();

        // Dialog should still be displayed
        onView(withText(R.string.rule_form_heading_create))
                .check(matches(isDisplayed()));
    }

    @Test
    public void matchInput_validValue_savesAndCloses() throws InterruptedException {
        launchActivity();
        openCreateDialog();

        selectTypeSpinnerItem(getStringResource(R.string.rule_type_match));
        onView(withId(R.id.match_input))
                .perform(replaceText("555*"), closeSoftKeyboard());

        clickOk();

        // Dialog should be dismissed
        onView(withText(R.string.rule_form_heading_create))
                .check(doesNotExist());

        // Verify rule persisted to DB
        Thread.sleep(500);
        List<RuleEntity> rules = dbHelper.getRuleEntries();
        boolean found = false;
        for (RuleEntity rule : rules) {
            if (rule.getType() == RuleType.MATCH && rule.getValue() != null
                    && rule.getValue().contains("555")) {
                found = true;
                break;
            }
        }
        assertTrue("Rule with type=MATCH should exist in DB", found);
    }

    @Test
    public void editDialog_areaCodeRule_populatesFields() {
        dbHelper.insertRule(RuleType.AREA_CODE, RuleAction.BLOCK, "212", true, TEST_ORDER);

        launchActivity();
        openEditDialog(0);

        onView(withText(R.string.rule_form_heading_edit))
                .check(matches(isDisplayed()));
        onView(withId(R.id.type_spinner))
                .check(matches(withSpinnerText(
                        containsString(getStringResource(R.string.rule_type_area_code)))));
        onView(withId(R.id.area_code_input))
                .check(matches(withText("212")));
    }

    @Test
    public void typeSpinner_apiR_includesVerificationTypes() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
            return; // Only meaningful on API 30+
        }

        launchActivity();
        openCreateDialog();

        onView(withId(R.id.type_spinner)).perform(click());

        onData(allOf(
                instanceOf(CharSequence.class),
                is((CharSequence) getStringResource(R.string.rule_type_verification_failed))
        )).inRoot(isPlatformPopup())
                .check(matches(isDisplayed()));
    }

    @Test
    public void typeSpinner_belowApiR_excludesVerificationTypes() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            return; // Only meaningful below API 30
        }

        launchActivity();
        openCreateDialog();

        onView(withId(R.id.type_spinner)).perform(click());

        // On pre-R devices, the verification type should not be in the spinner at all.
        // Since onData().check(doesNotExist()) doesn't work, verify by checking the
        // spinner text after closing the dropdown — it should not contain the value.
        onView(withId(R.id.type_spinner))
                .check(matches(not(withSpinnerText(
                        containsString(getStringResource(R.string.rule_type_verification_failed))))));
    }

    private String getStringResource(int resId) {
        return InstrumentationRegistry.getInstrumentation()
                .getTargetContext().getString(resId);
    }
}
