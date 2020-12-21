package com.novyr.callfilter.model;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.Calendar;

public interface Log {
    int getId();

    @NonNull
    Calendar getCreated();

    @Nullable
    String getNumber();
}
