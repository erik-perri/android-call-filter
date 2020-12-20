package com.novyr.callfilter.ui;

import android.view.ContextMenu;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.novyr.callfilter.R;
import com.novyr.callfilter.db.entity.LogEntity;
import com.novyr.callfilter.formatter.DateFormatter;
import com.novyr.callfilter.formatter.MessageFormatter;

class LogViewHolder extends RecyclerView.ViewHolder implements View.OnCreateContextMenuListener {
    private final TextView mMessageView;
    private final TextView mCreatedView;
    private final ImageView mIcon;
    private final MessageFormatter mMessageFormatter;
    private final DateFormatter mDateFormatter;
    private final LogListMenuHandler mMenuHandler;
    private LogEntity mEntity;

    LogViewHolder(
            View itemView,
            MessageFormatter messageFormatter,
            DateFormatter dateFormatter,
            LogListMenuHandler menuHandler
    ) {
        super(itemView);

        mMessageView = itemView.findViewById(R.id.log_list_message);
        mCreatedView = itemView.findViewById(R.id.log_list_created);
        mIcon = itemView.findViewById(R.id.log_list_icon);

        mMessageFormatter = messageFormatter;
        mDateFormatter = dateFormatter;
        mMenuHandler = menuHandler;

        itemView.setOnCreateContextMenuListener(this);
    }

    void setEntity(LogEntity entity) {
        mEntity = entity;

        mMessageView.setText(mMessageFormatter.formatMessage(entity));
        mCreatedView.setText(mDateFormatter.formatDate(entity));

        switch (entity.getAction()) {
            case ALLOWED:
                mIcon.setImageResource(R.drawable.ic_check_green_300_24dp);
                break;
            case BLOCKED:
                mIcon.setImageResource(R.drawable.ic_block_red_900_24dp);
                break;
            case FAILED:
                mIcon.setImageResource(R.drawable.ic_error_outline_black_24dp);
                break;
        }
    }

    @Override
    public void onCreateContextMenu(
            ContextMenu menu,
            View v,
            ContextMenu.ContextMenuInfo menuInfo
    ) {
        if (mEntity != null) {
            mMenuHandler.createMenu(v.getContext(), menu, mEntity);
        }
    }
}
