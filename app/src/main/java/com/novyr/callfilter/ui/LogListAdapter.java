package com.novyr.callfilter.ui;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.novyr.callfilter.R;
import com.novyr.callfilter.db.entity.LogEntity;
import com.novyr.callfilter.formatter.DateFormatter;
import com.novyr.callfilter.formatter.MessageFormatter;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

public class LogListAdapter extends RecyclerView.Adapter<LogViewHolder> {
    private final LayoutInflater mInflater;
    private final MessageFormatter mMessageFormatter;
    private final DateFormatter mDateFormatter;
    private final LogListMenuHandler mMenuHandler;

    private List<LogEntity> mEntries;

    public LogListAdapter(
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

    public void setEntities(List<LogEntity> entities) {
        mEntries = entities;

        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public LogViewHolder onCreateViewHolder(@Nullable ViewGroup parent, int viewType) {
        View itemView = mInflater.inflate(R.layout.log_item, parent, false);
        return new LogViewHolder(itemView, mMenuHandler);
    }

    @Override
    public void onBindViewHolder(@NonNull LogViewHolder holder, int position) {
        if (mEntries == null) {
            return;
        }

        LogEntity entity = mEntries.get(position);

        holder.setEntity(entity);
        holder.mMessageView.setText(mMessageFormatter.formatMessage(entity));
        holder.mCreatedView.setText(mDateFormatter.formatDate(entity));
    }

    @Override
    public int getItemCount() {
        return mEntries != null ? mEntries.size() : 0;
    }
}
