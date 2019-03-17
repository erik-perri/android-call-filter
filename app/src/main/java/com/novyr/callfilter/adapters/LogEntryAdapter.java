package com.novyr.callfilter.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.novyr.callfilter.CallFilterApplication;
import com.novyr.callfilter.R;
import com.novyr.callfilter.models.LogEntry;

import java.text.DateFormat;

public class LogEntryAdapter extends ArrayAdapter<LogEntry> {
    public LogEntryAdapter(Context context) {
        super(context, 0, LogEntry.listAll(LogEntry.class, "created DESC"));
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        LogEntry item = getItem(position);

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_log_item, parent, false);
        }

        if (item == null) {
            return convertView;
        }

        TextView tvDate = convertView.findViewById(R.id.created);
        TextView tvMessage = convertView.findViewById(R.id.message);

        String formattedNumber = CallFilterApplication.formatNumber(getContext(), item.number);

        DateFormat format = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT);
        String formattedDate = format.format(item.created);
        /*if (DateUtils.isToday(item.created.getTime())) {
            formattedDate = DateUtils.getRelativeTimeSpanString(
                    item.created.getTime(),
                    new Date().getTime(),
                    0L,
                    0 //DateUtils.FORMAT_ABBREV_ALL
            ).toString();
        }*/

        String action;
        switch (item.action) {
            case "blocked":
                action = "Blocked call";
                break;
            case "allowed":
                action = "Allowed call";
                break;
            case "error":
                action = "Failed to block call";
                break;
            default:
                action = String.format("Unknown (%s)", item.action);
                break;
        }

        tvDate.setText(formattedDate);
        tvMessage.setText(String.format("%s: %s", action, formattedNumber));

        return convertView;
    }
}
