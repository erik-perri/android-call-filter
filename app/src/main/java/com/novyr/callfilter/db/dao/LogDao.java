package com.novyr.callfilter.db.dao;

import com.novyr.callfilter.db.entity.LogEntity;

import java.util.List;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

@Dao
public interface LogDao {
    @Insert
    void insert(LogEntity entity);

    @Delete
    void delete(LogEntity entity);

    @Query("DELETE FROM log_entity")
    void deleteAll();

    @Query("SELECT * from log_entity ORDER BY id DESC")
    LiveData<List<LogEntity>> findAll();
}
