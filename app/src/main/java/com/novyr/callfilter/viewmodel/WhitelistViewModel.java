package com.novyr.callfilter.viewmodel;

import android.app.Application;

import com.novyr.callfilter.CallFilterApplication;
import com.novyr.callfilter.db.WhitelistRepository;
import com.novyr.callfilter.db.entity.WhitelistEntity;

import java.util.List;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

public class WhitelistViewModel extends AndroidViewModel {
    private final WhitelistRepository mRepository;
    private List<WhitelistEntity> mCurrentEntities;

    public WhitelistViewModel(Application application) {
        super(application);

        mRepository = ((CallFilterApplication) application).getWhitelistRepository();
    }

    public LiveData<List<WhitelistEntity>> findAll() {
        return mRepository.findAll();
    }

    public void insert(WhitelistEntity entry) {
        mRepository.insert(entry);
    }

    public void delete(WhitelistEntity whitelist) {
        mRepository.delete(whitelist);
    }

    public void setCurrentEntities(List<WhitelistEntity> entities) {
        mCurrentEntities = entities;
    }

    public List<WhitelistEntity> getCurrentEntities() {
        return mCurrentEntities;
    }
}
