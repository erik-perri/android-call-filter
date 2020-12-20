package com.novyr.callfilter.ui.rulelist;

import android.annotation.SuppressLint;
import android.os.Handler;
import android.os.Looper;
import android.view.ContextMenu;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.widget.SwitchCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.novyr.callfilter.R;
import com.novyr.callfilter.db.entity.RuleEntity;

public class RuleViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnCreateContextMenuListener {
    private final Handler mHandler = new Handler(Looper.getMainLooper());

    private final RuleListViewModel mRuleListViewModel;

    private RuleEntity mCurrentRule;
    private final ImageView mDragHandle;
    private final TextView mTypeView;
    private final TextView mValueView;
    private final TextView mAllowView;
    private final TextView mBlockView;
    private final SwitchCompat mEnabledSwitch;

    @SuppressLint("ClickableViewAccessibility")
    RuleViewHolder(
            View itemView,
            RuleListViewModel ruleListViewModel,
            OnStartDragListener dragListener
    ) {
        super(itemView);

        itemView.setOnClickListener(this);
        itemView.setOnCreateContextMenuListener(this);

        mRuleListViewModel = ruleListViewModel;

        mDragHandle = itemView.findViewById(R.id.rule_drag_handle);
        mAllowView = itemView.findViewById(R.id.rule_action_allow);
        mBlockView = itemView.findViewById(R.id.rule_action_block);
        mTypeView = itemView.findViewById(R.id.rule_type);
        mValueView = itemView.findViewById(R.id.rule_value);
        mEnabledSwitch = itemView.findViewById(R.id.rule_enabled_switch);

        // Setup a listener so the switch updates the entity when toggled
        mEnabledSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            final RuleEntity rule = mCurrentRule;
            // We need to give the switch animation time to run or when we save the entity the
            // view will automatically refresh causing the animation to jump to the next state
            mHandler.postDelayed(() -> mRuleListViewModel.enable(rule, isChecked), 250);
        });

        mDragHandle.setOnTouchListener((v, event) -> {
            if (event.getActionMasked() == MotionEvent.ACTION_DOWN) {
                dragListener.onStartDrag(this);
                return true;
            }

            return false;
        });
    }

    public interface OnStartDragListener {
        void onStartDrag(RecyclerView.ViewHolder viewHolder);
    }

    public void setCurrentRule(RuleEntity currentRule) {
        mCurrentRule = currentRule;

        mTypeView.setText(currentRule.getType().getDisplayNameResource());

        if (currentRule.getValue() != null) {
            mValueView.setText(currentRule.getValue());
        } else {
            mValueView.setText("");
        }

        if (!mRuleListViewModel.canMove(currentRule)) {
            mDragHandle.setImageResource(R.drawable.ic_drag_indicator_disabled_18dp);
        } else {
            mDragHandle.setImageResource(R.drawable.ic_drag_indicator_18dp);
        }

        mEnabledSwitch.setChecked(currentRule.isEnabled());

        switch (currentRule.getAction()) {
            case ALLOW:
                mAllowView.setVisibility(View.VISIBLE);
                mBlockView.setVisibility(View.GONE);
                break;
            case BLOCK:
                mAllowView.setVisibility(View.GONE);
                mBlockView.setVisibility(View.VISIBLE);
                break;
        }
    }

    @Override
    public void onClick(View view) {
        mEnabledSwitch.toggle();
    }

    @Override
    public void onCreateContextMenu(
            ContextMenu contextMenu,
            View view,
            ContextMenu.ContextMenuInfo contextMenuInfo
    ) {
        mRuleListViewModel.createContextMenu(contextMenu, mCurrentRule);
    }
}
