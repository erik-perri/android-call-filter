package com.novyr.callfilter.models;

import com.orm.SugarRecord;

public class WhitelistEntry extends SugarRecord
{
    public String number;

    public WhitelistEntry()
    {
    }

    public WhitelistEntry(String number)
    {
        this.number = number;
    }
}
