package com.novyr.callfilter.viewmodel;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.novyr.callfilter.CallFilterApplication;
import com.novyr.callfilter.db.RuleRepository;
import com.novyr.callfilter.db.entity.RuleEntity;

import java.util.List;

public class RuleViewModel extends AndroidViewModel {
    private final RuleRepository mRepository;

    public RuleViewModel(Application application) {
        super(application);

        mRepository = ((CallFilterApplication) application).getRuleRepository();
    }

    public LiveData<List<RuleEntity>> findAll() {
        return mRepository.findAll();
    }

    public void delete(RuleEntity entity) {
        mRepository.delete(entity);
    }

    public void save(RuleEntity entity) {
        if (entity.getId() > 0) {
            mRepository.update(entity);
        } else {
            mRepository.insert(entity);
        }
    }

    public void reorder(RuleEntity[] entities) {
        // We increment the order by 2, leaving a space between each, so when we restore a deleted
        // item it is easier to put it back in place (by subtracting 1 from the order it was)
        for (int index = entities.length - 1, order = 0; index >= 0; index--, order += 2) {
            RuleEntity entity = entities[index];

            if (entity.getOrder() != order) {
                entity.setOrder(order);
                save(entity);
            }
        }
    }

    public LiveData<Integer> highestOrder() {
        return mRepository.highestOrder();
    }
}
