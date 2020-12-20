package com.novyr.callfilter.ui.rulelist;

import android.app.Activity;
import android.view.ContextMenu;
import android.view.MenuItem;

import com.google.android.material.snackbar.Snackbar;

import com.novyr.callfilter.R;
import com.novyr.callfilter.db.entity.RuleEntity;
import com.novyr.callfilter.db.entity.enums.RuleAction;
import com.novyr.callfilter.db.entity.enums.RuleType;
import com.novyr.callfilter.ui.ruleedit.RuleEditDialog;
import com.novyr.callfilter.viewmodel.RuleViewModel;

public class RuleListViewModel {
    private final Activity mParent;
    private final RuleViewModel mRuleViewModel;
    private int mNextOrder;

    public RuleListViewModel(Activity parent, RuleViewModel ruleViewModel) {
        mParent = parent;
        mRuleViewModel = ruleViewModel;
    }

    public void setNextOrder(int nextOrder) {
        mNextOrder = nextOrder;
    }

    public void enable(RuleEntity rule, boolean enabled) {
        if (rule.isEnabled() != enabled) {
            rule.setEnabled(enabled);
            mRuleViewModel.save(rule);
        }
    }

    public void showEditDialog(RuleEntity rule) {
        new RuleEditDialog(mParent).show(rule, mRuleViewModel::save);
    }

    public void showEditDialog() {
        new RuleEditDialog(mParent).show(
                new RuleEntity(
                        RuleType.RECOGNIZED,
                        RuleAction.ALLOW,
                        null,
                        true,
                        mNextOrder
                ),
                mRuleViewModel::save
        );
    }

    public boolean canMove(RuleEntity rule) {
        return rule.getType() != RuleType.UNMATCHED;
    }

    public void reorder(RuleEntity[] rules) {
        mRuleViewModel.reorder(rules);
    }

    public boolean canDelete(RuleEntity rule) {
        return rule.getType() != RuleType.UNMATCHED;
    }

    public void delete(RuleEntity rule) {
        mRuleViewModel.delete(rule);

        Snackbar.make(
                mParent.findViewById(android.R.id.content),
                "Rule deleted",
                Snackbar.LENGTH_LONG
        )
                .setAction(R.string.undo, v -> {
                    // Remove the ID so when it is re-added on undo the view model does
                    // not attempt to update the now deleted row
                    rule.setId(0);

                    // Reduce the order, placing it before whatever replaced it in the
                    // delete reorder
                    rule.setOrder(Math.max(1, rule.getOrder() - 1));

                    mRuleViewModel.save(rule);
                }).show();
    }

    public void createContextMenu(final ContextMenu contextMenu, final RuleEntity rule) {
        mParent.getMenuInflater().inflate(R.menu.menu_rule_context, contextMenu);

        final MenuItem.OnMenuItemClickListener listener = menuItem -> {
            int itemId = menuItem.getItemId();
            if (itemId == R.id.rule_context_edit) {
                showEditDialog(rule);
                return true;
            } else if (itemId == R.id.rule_context_delete) {
                delete(rule);
                return true;
            }
            return false;
        };

        contextMenu.findItem(R.id.rule_context_edit).setOnMenuItemClickListener(listener);

        contextMenu.findItem(R.id.rule_context_delete)
                   .setEnabled(canDelete(rule))
                   .setOnMenuItemClickListener(listener);
    }
}
