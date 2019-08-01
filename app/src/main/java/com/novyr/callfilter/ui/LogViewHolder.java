package com.novyr.callfilter.ui;

import android.view.View;
import android.widget.TextView;

import com.novyr.callfilter.R;
import com.novyr.callfilter.db.entity.LogEntity;
import com.novyr.callfilter.viewmodel.LogViewModel;
import com.novyr.callfilter.viewmodel.WhitelistViewModel;

import androidx.recyclerview.widget.RecyclerView;

class LogViewHolder extends RecyclerView.ViewHolder {
    final TextView mMessageView;
    final TextView mCreatedView;
    private LogEntity mEntity;

    LogViewHolder(View itemView, LogViewModel logViewModel, WhitelistViewModel whitelistViewModel) {
        super(itemView);

        itemView.setOnCreateContextMenuListener(new LogListMenuHandler(
                itemView.getContext(),
                this,
                logViewModel,
                whitelistViewModel
        ));

        mMessageView = itemView.findViewById(R.id.message);
        mCreatedView = itemView.findViewById(R.id.created);
    }

    LogEntity getEntity() {
        return mEntity;
    }

    void setEntity(LogEntity entity) {
        mEntity = entity;
    }
}
