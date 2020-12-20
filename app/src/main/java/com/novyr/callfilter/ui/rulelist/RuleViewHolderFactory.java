package com.novyr.callfilter.ui.rulelist;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;

import com.novyr.callfilter.R;

public class RuleViewHolderFactory {
    private final LayoutInflater mInflater;
    private final RuleListViewModel mRuleListViewModel;
    private final RuleViewHolder.OnStartDragListener mDragListener;

    RuleViewHolderFactory(
            Context context,
            RuleListViewModel ruleListViewModel,
            RuleViewHolder.OnStartDragListener dragListener
    ) {
        mInflater = LayoutInflater.from(context);
        mRuleListViewModel = ruleListViewModel;
        mDragListener = dragListener;
    }

    public RuleViewHolder create(@Nullable ViewGroup parent) {
        View itemView = mInflater.inflate(R.layout.content_rule_entity, parent, false);

        return new RuleViewHolder(itemView, mRuleListViewModel, mDragListener);
    }
}
