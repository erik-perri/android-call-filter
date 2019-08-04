package com.novyr.callfilter.permissions.checker;

import androidx.annotation.Nullable;

import java.util.List;

public interface CheckerWithErrorsInterface {
    @Nullable
    List<String> getErrors();
}
