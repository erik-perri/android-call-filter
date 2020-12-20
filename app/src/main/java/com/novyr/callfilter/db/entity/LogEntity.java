package com.novyr.callfilter.db.entity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import com.novyr.callfilter.db.converter.LogActionConverter;
import com.novyr.callfilter.db.converter.DateConverter;
import com.novyr.callfilter.db.entity.enums.LogAction;
import com.novyr.callfilter.model.Log;

import java.util.Date;

@Entity(tableName = "log_entity")
public class LogEntity implements Log {
    @PrimaryKey(autoGenerate = true)
    private int id;

    @NonNull
    @TypeConverters(DateConverter.class)
    private Date created;

    @NonNull
    @TypeConverters(LogActionConverter.class)
    private LogAction action;

    @Nullable
    private String number;

    public LogEntity(@NonNull Date created, @NonNull LogAction action, @Nullable String number) {
        this.created = created;
        this.action = action;
        this.number = number;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @NonNull
    public Date getCreated() {
        return created;
    }

    public void setCreated(@NonNull Date created) {
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
