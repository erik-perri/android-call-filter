package com.novyr.callfilter.db.dao;

import com.novyr.callfilter.db.entity.WhitelistEntity;

import java.util.List;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

@Dao
public interface WhitelistDao {
    @Insert
    void insert(WhitelistEntity entity);

    @Delete
    void delete(WhitelistEntity entity);

    @Query("SELECT * from whitelist_entity")
    LiveData<List<WhitelistEntity>> findAll();

    @Query("SELECT * from whitelist_entity WHERE number = :number LIMIT 1")
    WhitelistEntity findAll(String number);
}
