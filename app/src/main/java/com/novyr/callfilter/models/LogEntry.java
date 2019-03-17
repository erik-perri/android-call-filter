package com.novyr.callfilter.models;

import com.orm.SugarRecord;

import java.util.Date;

public class LogEntry extends SugarRecord {
    public Date created;
    public String action;
    public String number;

    @SuppressWarnings("unused")
    public LogEntry() {
        // This is needed by Sugar ORM
    }

    public LogEntry(Date created, String action, String number) {
        this.created = created;
        this.action = action;
        this.number = number;
    }
}
