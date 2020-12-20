package com.novyr.callfilter.db.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.novyr.callfilter.db.entity.RuleEntity;

import java.util.List;

@Dao
public interface RuleDao {
    @Insert
    void insert(RuleEntity entity);

    @Update
    void update(RuleEntity entity);

    @Delete
    void delete(RuleEntity entity);

    @Query("DELETE FROM rule_entity")
    void deleteAll();

    @Query("SELECT * from rule_entity ORDER BY `order` DESC")
    LiveData<List<RuleEntity>> findAll();

    @Query("SELECT * from rule_entity WHERE `enabled` = 1 ORDER BY `order` DESC")
    List<RuleEntity> findEnabled();
}
