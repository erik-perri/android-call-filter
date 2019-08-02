package com.novyr.callfilter.ui;

import android.view.ContextMenu;
import android.view.View;
import android.widget.TextView;

import com.novyr.callfilter.R;
import com.novyr.callfilter.db.entity.LogEntity;

import androidx.recyclerview.widget.RecyclerView;

class LogViewHolder extends RecyclerView.ViewHolder implements View.OnCreateContextMenuListener {
    final TextView mMessageView;
    final TextView mCreatedView;
    private LogEntity mEntity;
    private final LogListMenuHandler mMenuHandler;

    LogViewHolder(View itemView, LogListMenuHandler menuHandler) {
        super(itemView);

        mMenuHandler = menuHandler;
        mMessageView = itemView.findViewById(R.id.message);
        mCreatedView = itemView.findViewById(R.id.created);

        itemView.setOnCreateContextMenuListener(this);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        final LogEntity entity = getEntity();
        if (entity == null) {
            return;
        }

        mMenuHandler.createMenu(menu, entity);
    }

    LogEntity getEntity() {
        return mEntity;
    }

    void setEntity(LogEntity entity) {
        mEntity = entity;
    }
}
