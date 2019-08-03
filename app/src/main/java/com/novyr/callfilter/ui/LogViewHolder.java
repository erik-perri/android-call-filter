package com.novyr.callfilter.ui;

import android.view.ContextMenu;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.novyr.callfilter.R;
import com.novyr.callfilter.db.entity.LogEntity;

class LogViewHolder extends RecyclerView.ViewHolder implements View.OnCreateContextMenuListener {
    final TextView mMessageView;
    final TextView mCreatedView;
    final ImageView mIcon;
    private final LogListMenuHandler mMenuHandler;
    private LogEntity mEntity;

    LogViewHolder(View itemView, LogListMenuHandler menuHandler) {
        super(itemView);

        mMenuHandler = menuHandler;
        mMessageView = itemView.findViewById(R.id.log_list_message);
        mCreatedView = itemView.findViewById(R.id.log_list_created);
        mIcon = itemView.findViewById(R.id.log_list_icon);

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

    private LogEntity getEntity() {
        return mEntity;
    }

    void setEntity(LogEntity entity) {
        mEntity = entity;
    }
}
