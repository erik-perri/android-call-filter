package com.novyr.callfilter.viewmodel;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.novyr.callfilter.CallFilterApplication;
import com.novyr.callfilter.db.WhitelistRepository;
import com.novyr.callfilter.db.entity.WhitelistEntity;

import java.util.List;

public class WhitelistViewModel extends AndroidViewModel {
    private final WhitelistRepository mRepository;

    public WhitelistViewModel(Application application) {
        super(application);

        mRepository = ((CallFilterApplication) application).getWhitelistRepository();
    }

    public LiveData<List<WhitelistEntity>> findAll() {
        return mRepository.findAll();
    }

    public void insert(WhitelistEntity entity) {
        mRepository.insert(entity);
    }

    public void delete(WhitelistEntity entity) {
        mRepository.delete(entity);
    }
}
