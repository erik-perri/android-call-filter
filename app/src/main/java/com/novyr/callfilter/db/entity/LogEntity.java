package com.novyr.callfilter.db.entity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import com.novyr.callfilter.db.converter.CalendarConverter;
import com.novyr.callfilter.db.converter.LogActionConverter;
import com.novyr.callfilter.db.entity.enums.LogAction;
import com.novyr.callfilter.model.Log;

import java.util.Calendar;
import java.util.TimeZone;

@Entity(tableName = "log_entity")
public class LogEntity implements Log {
    @PrimaryKey(autoGenerate = true)
    private int id;

    @NonNull
    @TypeConverters(CalendarConverter.class)
    private Calendar created;

    @NonNull
    @TypeConverters(LogActionConverter.class)
    private LogAction action;

    @Nullable
    private String number;

    @Ignore
    public LogEntity(@NonNull LogAction action, @Nullable String number) {
        this(createCalendar(), action, number);
    }

    public LogEntity(
            @NonNull Calendar created,
            @NonNull LogAction action,
            @Nullable String number
    ) {
        this.created = created;
        this.action = action;
        this.number = number;
    }

    private static Calendar createCalendar() {
        return Calendar.getInstance(TimeZone.getTimeZone("UTC"));
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @NonNull
    public Calendar getCreated() {
        return created;
    }

    public void setCreated(@NonNull Calendar created) {
        this.created = created;
    }

    @NonNull
    public LogAction getAction() {
        return action;
    }

    public void setAction(@NonNull LogAction action) {
        this.action = action;
    }

    @Nullable
    public String getNumber() {
        return number;
    }

    public void setNumber(@Nullable String number) {
        this.number = number;
    }
}
