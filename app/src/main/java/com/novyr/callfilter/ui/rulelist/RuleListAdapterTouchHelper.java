package com.novyr.callfilter.ui.rulelist;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

public class RuleListAdapterTouchHelper extends ItemTouchHelper.Callback {
    private final RuleListAdapter mAdapter;

    public RuleListAdapterTouchHelper(RuleListAdapter adapter) {
        mAdapter = adapter;
    }

    @Override
    public boolean isLongPressDragEnabled() {
        // Since we're using a context menu we only want the drag to work through the drag handle
        return false;
    }

    @Override
    public int getMovementFlags(
            @NonNull RecyclerView recyclerView,
            @NonNull RecyclerView.ViewHolder viewHolder
    ) {
        if (!mAdapter.canMoveItem(viewHolder.getAdapterPosition())) {
            return makeMovementFlags(0, 0);
        }

        final int dragFlags = mAdapter.canMoveItem(viewHolder.getAdapterPosition())
                ? ItemTouchHelper.UP | ItemTouchHelper.DOWN
                : 0;

        return makeMovementFlags(dragFlags, 0);
    }

    @Override
    public boolean onMove(
            @NonNull RecyclerView recyclerView,
            @NonNull RecyclerView.ViewHolder viewHolder,
            @NonNull RecyclerView.ViewHolder target
    ) {
        if (viewHolder.getItemViewType() != target.getItemViewType()) {
            return false;
        }

        if (mAdapter.canMoveItem(viewHolder.getAdapterPosition())) {
            mAdapter.moveItem(viewHolder.getAdapterPosition(), target.getAdapterPosition());
        }

        return true;
    }

    @Override
    public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
        mAdapter.deleteItem(viewHolder.getAdapterPosition());
    }

    @Override
    public void clearView(
            @NonNull RecyclerView recyclerView,
            @NonNull RecyclerView.ViewHolder viewHolder
    ) {
        super.clearView(recyclerView, viewHolder);

        mAdapter.onClearView();
    }
}
