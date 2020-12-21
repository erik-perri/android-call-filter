package com.novyr.callfilter.ui.rulelist;

import android.annotation.SuppressLint;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.novyr.callfilter.db.entity.RuleEntity;

import java.util.List;

public class RuleListAdapter extends RecyclerView.Adapter<RuleViewHolder> {
    private final RuleListActionHelper mRuleListActionHelper;
    private RuleViewHolderFactory mViewHolderFactory;
    private List<RuleEntity> mEntries;

    RuleListAdapter(RuleListActionHelper ruleListActionHelper) {
        mRuleListActionHelper = ruleListActionHelper;
    }

    public void setViewHolderFactory(RuleViewHolderFactory viewHolderFactory) {
        mViewHolderFactory = viewHolderFactory;
    }

    void setEntities(List<RuleEntity> entities) {
        mEntries = entities;

        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public RuleViewHolder onCreateViewHolder(@Nullable ViewGroup parent, int viewType) {
        return mViewHolderFactory.create(parent);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onBindViewHolder(@NonNull RuleViewHolder holder, int position) {
        if (mEntries == null) {
            return;
        }

        RuleEntity rule = mEntries.get(position);
        if (rule != null) {
            holder.setCurrentRule(rule);
        }
    }

    @Override
    public int getItemCount() {
        return mEntries != null ? mEntries.size() : 0;
    }

    public boolean canMoveItem(int position) {
        return mRuleListActionHelper.canMove(mEntries.get(position));
    }

    public void moveItem(int fromPosition, int toPosition) {
        if (!canMoveItem(fromPosition) || !canMoveItem(toPosition)) {
            return;
        }

        RuleEntity fromRule = mEntries.remove(fromPosition);
        mEntries.add(toPosition, fromRule);
        notifyItemMoved(fromPosition, toPosition);

        // We don't reorder in the DB here, if we did the view would update with the LiveData and
        // cause the drag to stop. Instead we handle the DB reorder in onClearView when ordering is
        // complete.
    }

    public void deleteItem(int position) {
        RuleEntity rule = mEntries.get(position);

        if (!mRuleListActionHelper.canDelete(rule)) {
            return;
        }

        // Delete the item from the database, the item is removed from the list when the LiveData
        // updates
        mRuleListActionHelper.delete(rule);
    }

    public void onClearView() {
        mRuleListActionHelper.reorder(mEntries.toArray(new RuleEntity[0]));
    }
}
