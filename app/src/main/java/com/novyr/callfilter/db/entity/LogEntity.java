package com.novyr.callfilter.db.entity;

import com.novyr.callfilter.db.converter.ActionConverter;
import com.novyr.callfilter.db.converter.DateConverter;
import com.novyr.callfilter.db.entity.enums.Action;
import com.novyr.callfilter.model.Log;

import java.util.Date;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

@Entity(tableName = "log_entity")
public class LogEntity implements Log {
    @PrimaryKey(autoGenerate = true)
    private int id;

    @NonNull
    @TypeConverters(DateConverter.class)
    private Date created;

    @NonNull
    @TypeConverters(ActionConverter.class)
    private Action action;

    @Nullable
    private String number;

    public LogEntity(@NonNull Date created, @NonNull Action action, @Nullable String number) {
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

    @SuppressWarnings("unused")
    public void setCreated(@NonNull Date created) {
        this.created = created;
    }

    @NonNull
    public Action getAction() {
        return action;
    }

    @SuppressWarnings("unused")
    public void setAction(@NonNull Action action) {
        this.action = action;
    }

    @Nullable
    public String getNumber() {
        return number;
    }

    @SuppressWarnings("unused")
    public void setNumber(@Nullable String number) {
        this.number = number;
    }
}
