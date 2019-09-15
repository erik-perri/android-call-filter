package com.novyr.callfilter.viewmodel;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.novyr.callfilter.CallFilterApplication;
import com.novyr.callfilter.db.LogRepository;
import com.novyr.callfilter.db.entity.LogEntity;

import java.util.List;

public class LogViewModel extends AndroidViewModel {
    private final LogRepository mRepository;

    public LogViewModel(Application application) {
        super(application);

        mRepository = ((CallFilterApplication) application).getLogRepository();
    }

    public LiveData<List<LogEntity>> findAll() {
        return mRepository.findAll();
    }

    public void deleteAll() {
        mRepository.deleteAll();
    }

    public void delete(LogEntity entity) {
        mRepository.delete(entity);
    }
}
