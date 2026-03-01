package com.novyr.callfilter.ui.rulelist;

import android.graphics.Rect;
import android.os.Build;

import androidx.test.core.app.ActivityScenario;
import androidx.test.espresso.IdlingRegistry;
import androidx.test.filters.MediumTest;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.rule.GrantPermissionRule;
import androidx.test.uiautomator.By;
import androidx.test.uiautomator.UiDevice;
import androidx.test.uiautomator.UiObject2;

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

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.longClick;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.contrib.RecyclerViewActions.actionOnItemAtPosition;
import static androidx.test.espresso.matcher.ViewMatchers.hasDescendant;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static com.novyr.callfilter.util.RecyclerViewMatcher.withRecyclerView;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

@MediumTest
public class RuleListActivityTest {
    private final DatabaseHelper dbHelper = new DatabaseHelper();
    private final DatabaseIdlingResource idlingResource = new DatabaseIdlingResource();
    private ActivityScenario<RuleListActivity> scenario;

    // Test-inserted rules use a high order so they appear at position 0 (query is ORDER BY order DESC).
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

    @Test
    public void ruleList_emptyRules_showsEmptyView() {
        launchActivity();

        onView(withId(R.id.empty_view))
                .check(matches(isDisplayed()));
        onView(withId(R.id.rule_list))
                .check(matches(not(isDisplayed())));
    }

    @Test
    public void ruleEntry_allowAction_showsAllowLabel() {
        dbHelper.insertRule(RuleType.RECOGNIZED, RuleAction.ALLOW, null, true, TEST_ORDER);

        launchActivity();

        onView(withRecyclerView(R.id.rule_list).atPositionOnView(0, R.id.rule_action_allow))
                .check(matches(isDisplayed()));
        onView(withRecyclerView(R.id.rule_list).atPositionOnView(0, R.id.rule_action_block))
                .check(matches(not(isDisplayed())));
    }

    @Test
    public void ruleEntry_blockAction_showsBlockLabel() {
        dbHelper.insertRule(RuleType.PRIVATE, RuleAction.BLOCK, null, true, TEST_ORDER);

        launchActivity();

        onView(withRecyclerView(R.id.rule_list).atPositionOnView(0, R.id.rule_action_block))
                .check(matches(isDisplayed()));
        onView(withRecyclerView(R.id.rule_list).atPositionOnView(0, R.id.rule_action_allow))
                .check(matches(not(isDisplayed())));
    }

    @Test
    public void ruleEntry_areaCodeType_showsTypeLabel() {
        dbHelper.insertRule(RuleType.AREA_CODE, RuleAction.BLOCK, "212", true, TEST_ORDER);

        launchActivity();

        onView(withRecyclerView(R.id.rule_list).atPositionOnView(0, R.id.rule_type))
                .check(matches(withText(R.string.rule_type_area_code)));
    }

    @Test
    public void ruleEntry_matchWithValue_showsValue() {
        dbHelper.insertRule(RuleType.MATCH, RuleAction.BLOCK, "555*", true, TEST_ORDER);

        launchActivity();

        onView(withRecyclerView(R.id.rule_list).atPositionOnView(0, R.id.rule_value))
                .check(matches(withText("555*")));
    }

    @Test
    public void enabledSwitch_clicked_disablesRuleInDb() throws InterruptedException {
        dbHelper.insertRule(RuleType.RECOGNIZED, RuleAction.ALLOW, null, true, TEST_ORDER);

        launchActivity();

        onView(withId(R.id.rule_list))
                .perform(actionOnItemAtPosition(0, click()));

        // Wait for the delayed save (250ms in RuleViewHolder) plus propagation
        Thread.sleep(1000);

        List<RuleEntity> rules = dbHelper.getRuleEntries();
        assertFalse("Rule should be disabled after toggle", rules.get(0).isEnabled());
    }

    @Test
    public void ruleEntry_longPressEdit_opensEditDialog() {
        dbHelper.insertRule(RuleType.RECOGNIZED, RuleAction.ALLOW, null, true, TEST_ORDER);

        launchActivity();

        onView(withId(R.id.rule_list))
                .perform(actionOnItemAtPosition(0, longClick()));

        onView(withText(R.string.context_menu_edit_rule))
                .perform(click());

        onView(withText(R.string.rule_form_heading_edit))
                .check(matches(isDisplayed()));
    }

    @Test
    public void ruleEntry_longPressDelete_removesRule() throws InterruptedException {
        dbHelper.insertRule(RuleType.RECOGNIZED, RuleAction.ALLOW, null, true, TEST_ORDER);

        launchActivity();

        int countBefore = dbHelper.getRuleEntries().size();

        onView(withId(R.id.rule_list))
                .perform(actionOnItemAtPosition(0, longClick()));

        onView(withText(R.string.context_menu_delete_rule))
                .perform(click());

        waitForIdle();

        int countAfter = dbHelper.getRuleEntries().size();
        assertEquals("One rule should have been removed", countBefore - 1, countAfter);
    }

    @Test
    public void deleteRule_snackbarShown_showsUndoAction() {
        dbHelper.insertRule(RuleType.RECOGNIZED, RuleAction.ALLOW, null, true, TEST_ORDER);

        launchActivity();

        onView(withId(R.id.rule_list))
                .perform(actionOnItemAtPosition(0, longClick()));

        onView(withText(R.string.context_menu_delete_rule))
                .perform(click());

        onView(withText(R.string.undo))
                .check(matches(isDisplayed()));
    }

    @Test
    public void deleteRule_undoClicked_restoresRule() throws InterruptedException {
        dbHelper.insertRule(RuleType.RECOGNIZED, RuleAction.ALLOW, null, true, TEST_ORDER);

        launchActivity();

        int countBefore = dbHelper.getRuleEntries().size();

        onView(withId(R.id.rule_list))
                .perform(actionOnItemAtPosition(0, longClick()));

        onView(withText(R.string.context_menu_delete_rule))
                .perform(click());

        onView(withText(R.string.undo))
                .perform(click());

        waitForIdle();

        int countAfter = dbHelper.getRuleEntries().size();
        assertEquals("Rule count should be restored after undo", countBefore, countAfter);
    }

    @Test
    public void dragHandle_dragFirstPastSecond_reordersRules() throws InterruptedException {
        // Insert 2 movable rules above the defaults. ORDER DESC: MATCH(30)=pos0, AREA_CODE(20)=pos1
        dbHelper.insertRule(RuleType.AREA_CODE, RuleAction.BLOCK, "212", true, 20);
        dbHelper.insertRule(RuleType.MATCH, RuleAction.BLOCK, "555*", true, 30);

        launchActivity();

        UiDevice device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation());

        // Find all drag handles — includes both test and default rules
        List<UiObject2> handles = device.findObjects(By.desc("Reorder drag handle"));

        // Drag first handle (MATCH at pos 0) past second handle (AREA_CODE at pos 1).
        // Use UiObject2.drag() on the handle itself so ACTION_DOWN fires on the correct view,
        // triggering ItemTouchHelper.startDrag(). Drag to a point below the second handle's
        // center to ensure the item fully crosses the midpoint threshold.
        UiObject2 firstHandle = handles.get(0);
        Rect secondBounds = handles.get(1).getVisibleBounds();
        android.graphics.Point target = new android.graphics.Point(
                secondBounds.centerX(),
                secondBounds.centerY() + secondBounds.height() / 2
        );
        firstHandle.drag(target, 2000);

        // Wait for the reorder to persist to DB
        Thread.sleep(1000);

        // After drag: AREA_CODE should now be first (highest order), MATCH second.
        List<RuleEntity> rules = dbHelper.getRuleEntries();
        assertEquals("First rule should now be AREA_CODE", RuleType.AREA_CODE, rules.get(0).getType());
        assertEquals("Second rule should now be MATCH", RuleType.MATCH, rules.get(1).getType());
    }

    private void waitForIdle() {
        try {
            Thread.sleep(500);
        } catch (InterruptedException ignored) {
        }

        scenario.onActivity(activity -> {
            // Force a pass through the main looper to process pending LiveData updates
        });
    }
}
