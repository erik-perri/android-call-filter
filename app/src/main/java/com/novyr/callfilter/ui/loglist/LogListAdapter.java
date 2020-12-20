package com.novyr.callfilter.ui.loglist;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.novyr.callfilter.R;
import com.novyr.callfilter.db.entity.LogEntity;
import com.novyr.callfilter.formatter.DateFormatter;
import com.novyr.callfilter.formatter.MessageFormatter;

import java.util.List;

class LogListAdapter extends RecyclerView.Adapter<LogViewHolder> {
    private final LayoutInflater mInflater;
    private final MessageFormatter mMessageFormatter;
    private final DateFormatter mDateFormatter;
    private final LogListMenuHandler mMenuHandler;
    private List<LogEntity> mEntries;

    LogListAdapter(
            Context context,
            MessageFormatter messageFormatter,
            DateFormatter dateFormatter,
            LogListMenuHandler menuHandler
    ) {
        mInflater = LayoutInflater.from(context);
        mMessageFormatter = messageFormatter;
        mDateFormatter = dateFormatter;
        mMenuHandler = menuHandler;
    }

    void setEntities(List<LogEntity> entities) {
        mEntries = entities;

        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public LogViewHolder onCreateViewHolder(@Nullable ViewGroup parent, int viewType) {
        View itemView = mInflater.inflate(R.layout.content_log_entity, parent, false);

        return new LogViewHolder(itemView, mMessageFormatter, mDateFormatter, mMenuHandler);
    }

    @Override
    public void onBindViewHolder(@NonNull LogViewHolder holder, int position) {
        if (mEntries == null) {
            return;
        }

        LogEntity entity = mEntries.get(position);
        if (entity != null) {
            holder.setEntity(entity);
        }
    }

    @Override
    public int getItemCount() {
        return mEntries != null ? mEntries.size() : 0;
    }
}
