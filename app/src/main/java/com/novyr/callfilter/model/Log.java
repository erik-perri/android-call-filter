package com.novyr.callfilter.model;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.Date;

public interface Log {
    int getId();

    @NonNull
    Date getCreated();

    @Nullable
    String getNumber();
}
