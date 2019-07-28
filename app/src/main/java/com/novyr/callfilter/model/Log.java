package com.novyr.callfilter.model;

import java.util.Date;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public interface Log {
    int getId();

    @NonNull
    Date getCreated();

    @Nullable
    String getNumber();
}
