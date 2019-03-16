package com.novyr.callfilter.models;

import com.orm.SugarRecord;

import java.util.Date;

public class LogEntry extends SugarRecord {
    public Date created;
    public String action;
    public String number;

    public LogEntry() {
    }

    public LogEntry(String action, String number) {
        this(new Date(), action, number);
    }

    public LogEntry(Date created, String action, String number) {
        this.created = created;
        this.action = action;
        this.number = number;
    }
}
