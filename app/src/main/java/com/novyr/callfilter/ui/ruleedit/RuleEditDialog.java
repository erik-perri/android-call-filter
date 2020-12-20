package com.novyr.callfilter.ui.ruleedit;

import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import com.novyr.callfilter.ContactFinder;
import com.novyr.callfilter.R;
import com.novyr.callfilter.db.entity.RuleEntity;
import com.novyr.callfilter.db.entity.enums.RuleAction;
import com.novyr.callfilter.db.entity.enums.RuleType;
import com.novyr.callfilter.rules.RuleHandlerInterface;
import com.novyr.callfilter.rules.RuleHandlerManager;
import com.novyr.callfilter.rules.RuleHandlerWithFormInterface;
import com.novyr.callfilter.rules.exception.InvalidValueException;

import java.util.LinkedList;
import java.util.List;

// TODO Figure out a better way to structure this
public class RuleEditDialog {
    private final Context mContext;
    private final RuleHandlerManager mHandlerManager;
    private final LayoutInflater mLayoutInflater;

    public RuleEditDialog(Context context) {
        mContext = context;
        mHandlerManager = new RuleHandlerManager(new ContactFinder(context));
        mLayoutInflater = LayoutInflater.from(mContext);
    }

    public interface RuleEditComplete {
        void onEditComplete(RuleEntity entity);
    }

    public void show(RuleEntity rule, RuleEditComplete editComplete) {
        RuleEntity localRule = new RuleEntity(rule);

        View formView = mLayoutInflater.inflate(R.layout.form_rule, null);

        final TextView formHeading = formView.findViewById(R.id.form_heading);
        final Spinner actionSpinner = formView.findViewById(R.id.action_spinner);
        final Spinner typeSpinner = formView.findViewById(R.id.type_spinner);
        final Spinner enabledSpinner = formView.findViewById(R.id.enabled_spinner);

        formHeading.setText(
                rule.getId() > 0
                        ? R.string.rule_form_heading_edit
                        : R.string.rule_form_heading_create
        );

        setupSpinner(actionSpinner, getActionValues(), getActionDisplayName(localRule.getAction()));

        setupSpinner(
                enabledSpinner,
                getEnabledValues(),
                localRule.isEnabled()
                        ? mContext.getResources().getString(R.string.yes)
                        : mContext.getResources().getString(R.string.no)
        );

        setupSpinner(
                typeSpinner,
                getTypeValues(localRule.getType() == RuleType.UNMATCHED),
                mContext.getResources().getString(localRule.getType().getDisplayNameResource())
        );

        updateTypeForm(formView, localRule.getType(), localRule);

        // The unmatched type is the system created rule that should always be at the end. We don't
        // want the user to change it.
        typeSpinner.setEnabled(localRule.getType() != RuleType.UNMATCHED);

        // When the type spinner is changed we need to update the loaded type layout
        typeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(
                    AdapterView<?> parentView,
                    View selectedItemView,
                    int position,
                    long id
            ) {
                RuleType type = getTypeSpinnerValue(typeSpinner);

                if (type != localRule.getType()) {
                    localRule.setValue(null);
                }

                updateTypeForm(formView, type, localRule);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
            }
        });

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(mContext);
        alertDialogBuilder.setView(formView);
        alertDialogBuilder.setCancelable(true)
                          .setPositiveButton("OK", (dialog, id) -> {
                              // Handled below so we can prevent saving if there are errors
                          })
                          .setNegativeButton("Cancel", (dialog, id) -> dialog.cancel());

        AlertDialog alert = alertDialogBuilder.create();
        alert.setOnShowListener(dialog -> {
            // Since we're creating the EditText after the dialog is created we need to clear the
            // FLAG_ALT_FOCUSABLE_IM flag or the keyboard will not show when activating the input
            // https://stackoverflow.com/a/62767205
            ((Dialog) dialog).getWindow()
                             .clearFlags(WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);
        });
        alert.show();
        alert.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            localRule.setAction(getActionSpinnerValue(actionSpinner));
            localRule.setEnabled(getEnabledSpinnerValue(enabledSpinner));
            localRule.setType(getTypeSpinnerValue(typeSpinner));

            RuleHandlerInterface ruleHandler = mHandlerManager.findHandler(localRule.getType());
            if (ruleHandler instanceof RuleHandlerWithFormInterface) {
                try {
                    ((RuleHandlerWithFormInterface) ruleHandler).saveFormValues(
                            formView,
                            localRule
                    );
                } catch (InvalidValueException e) {
                    Toast.makeText(
                            mContext,
                            String.format(
                                    mContext.getResources().getString(R.string.rule_form_invalid),
                                    mContext.getResources().getString(e.getLabelResource())
                            ),
                            Toast.LENGTH_LONG
                    ).show();
                    return;
                }
            }

            editComplete.onEditComplete(localRule);

            alert.dismiss();
        });
    }

    private void updateTypeForm(View formView, RuleType type, RuleEntity rule) {
        loadTypeForm(formView, type);

        RuleHandlerInterface ruleHandler = mHandlerManager.findHandler(type);
        if (ruleHandler instanceof RuleHandlerWithFormInterface) {
            ((RuleHandlerWithFormInterface) ruleHandler).loadFormValues(formView, rule);
        }
    }

    private void loadTypeForm(View formView, RuleType type) {
        LinearLayout layout = formView.findViewById(R.id.form_rule_extra);
        layout.removeAllViews();

        RuleHandlerInterface ruleHandler = mHandlerManager.findHandler(type);
        if (ruleHandler instanceof RuleHandlerWithFormInterface) {
            int viewId = ((RuleHandlerWithFormInterface) ruleHandler).getEditDialogLayout();
            layout.addView(mLayoutInflater.inflate(viewId, null));
        }
    }

    private void setupSpinner(Spinner spinner, List<CharSequence> items, String activeItem) {
        ArrayAdapter<CharSequence> adapter = new ArrayAdapter<>(
                mContext,
                android.R.layout.simple_spinner_item,
                items
        );

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        if (activeItem != null) {
            int index = items.indexOf(activeItem);
            if (index != -1) {
                spinner.setSelection(items.indexOf(activeItem));
            }
        }
    }

    public List<CharSequence> getActionValues() {
        List<CharSequence> actions = new LinkedList<>();
        for (RuleAction action : RuleAction.values()) {
            actions.add(getActionDisplayName(action));
        }
        return actions;
    }

    public RuleAction getActionSpinnerValue(Spinner spinner) {
        CharSequence selection = (CharSequence) spinner.getSelectedItem();
        for (RuleAction action : RuleAction.values()) {
            if (selection == getActionDisplayName(action)) {
                return action;
            }
        }

        throw new RuntimeException("Unknown selection");
    }

    public String getActionDisplayName(RuleAction action) {
        switch (action) {
            case ALLOW:
                return mContext.getResources().getString(R.string.rule_action_allow);
            case BLOCK:
                return mContext.getResources().getString(R.string.rule_action_block);
        }

        throw new RuntimeException("Unknown action");
    }

    public List<CharSequence> getTypeValues(boolean includeUnmatched) {
        List<CharSequence> types = new LinkedList<>();
        for (RuleType type : RuleType.values()) {
            if (type == RuleType.UNMATCHED && !includeUnmatched) {
                continue;
            }

            types.add(mContext.getResources().getString(type.getDisplayNameResource()));
        }
        return types;
    }

    public RuleType getTypeSpinnerValue(Spinner spinner) {
        CharSequence selection = (CharSequence) spinner.getSelectedItem();
        for (RuleType type : RuleType.values()) {
            if (selection == mContext.getResources().getString(type.getDisplayNameResource())) {
                return type;
            }
        }

        throw new RuntimeException("Unknown selection");
    }

    public List<CharSequence> getEnabledValues() {
        List<CharSequence> values = new LinkedList<>();
        values.add(mContext.getResources().getString(R.string.yes));
        values.add(mContext.getResources().getString(R.string.no));
        return values;
    }

    public boolean getEnabledSpinnerValue(Spinner spinner) {
        return spinner.getSelectedItem().equals(mContext.getResources().getString(R.string.yes));
    }
}
