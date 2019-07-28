package com.novyr.callfilter.model;

import androidx.annotation.NonNull;

public interface Whitelist {
    int getId();

    @NonNull
    String getNumber();
}
