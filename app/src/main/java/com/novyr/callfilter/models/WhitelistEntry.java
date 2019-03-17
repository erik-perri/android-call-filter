package com.novyr.callfilter.models;

import com.orm.SugarRecord;

public class WhitelistEntry extends SugarRecord {
    @SuppressWarnings("WeakerAccess")
    public String number;

    @SuppressWarnings("unused")
    public WhitelistEntry() {
        // This is needed by Sugar ORM
    }

    public WhitelistEntry(String number) {
        this.number = number;
    }
}
