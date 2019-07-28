package com.novyr.callfilter.db.entity;

import com.novyr.callfilter.model.Whitelist;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "whitelist_entity")
public class WhitelistEntity implements Whitelist {
    @PrimaryKey(autoGenerate = true)
    private int id;

    @NonNull
    private String number;

    public WhitelistEntity(@NonNull String number) {
        this.number = number;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @NonNull
    public String getNumber() {
        return number;
    }

    @SuppressWarnings("unused")
    public void setNumber(@NonNull String number) {
        this.number = number;
    }
}
