package com.novyr.callfilter.ui;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.novyr.callfilter.NumberHelper;
import com.novyr.callfilter.R;
import com.novyr.callfilter.db.entity.LogEntity;
import com.novyr.callfilter.viewmodel.WhitelistViewModel;

import java.text.DateFormat;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

public class LogListAdapter extends RecyclerView.Adapter<LogViewHolder> {
    private final LayoutInflater mInflater;
    private final WhitelistViewModel mWhitelistViewModel;

    private NumberHelper mNumberFormatter;
    private List<LogEntity> mEntries;

    public LogListAdapter(Context context, WhitelistViewModel whitelistViewModel) {
        mInflater = LayoutInflater.from(context);
        mWhitelistViewModel = whitelistViewModel;
    }

    public void setNumberFormatter(NumberHelper numberHelper) {
        mNumberFormatter = numberHelper;
    }

    public void setEntities(List<LogEntity> entities) {
        mEntries = entities;

        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public LogViewHolder onCreateViewHolder(@Nullable ViewGroup parent, int viewType) {
        View itemView = mInflater.inflate(R.layout.log_item, parent, false);
        return new LogViewHolder(itemView, mWhitelistViewModel);
    }

    @Override
    public void onBindViewHolder(@NonNull LogViewHolder holder, int position) {
        if (mEntries == null) {
            return;
        }

        LogEntity entity = mEntries.get(position);

        holder.setEntity(entity);
        holder.mMessageView.setText(formatMessage(entity));
        holder.mCreatedView.setText(formatDate(entity));
    }

    @Override
    public int getItemCount() {
        return mEntries != null ? mEntries.size() : 0;
    }

    private String formatMessage(LogEntity entity) {
        String action;
        switch (entity.getAction()) {
            case BLOCKED:
                action = "Blocked call";
                break;
            case ALLOWED:
                action = "Allowed call";
                break;
            case FAILED:
                action = "Failed to block call";
                break;
            default:
                action = String.format("Unknown (%s)", entity.getAction().getCode());
                break;
        }

        String number = entity.getNumber();

        if (mNumberFormatter != null) {
            String formattedNumber = mNumberFormatter.formatNumber(number);
            if (formattedNumber != null) {
                number = formattedNumber;
            }
        }

        return String.format("%s: %s", action, number);
    }

    private String formatDate(LogEntity entity) {
        DateFormat format = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT);
        return format.format(entity.getCreated());
    }
}
