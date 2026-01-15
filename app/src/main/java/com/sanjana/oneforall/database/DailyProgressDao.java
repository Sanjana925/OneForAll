package com.sanjana.oneforall.database;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Update;
import androidx.room.Delete;
import androidx.room.Query;

import java.util.List;

@Dao
public interface DailyProgressDao {

    @Insert
    long insert(DailyProgress dp);

    @Update
    void update(DailyProgress dp);

    @Delete
    void delete(DailyProgress dp);

    @Query("DELETE FROM DailyProgress WHERE itemId = :itemId")
    void deleteByItemId(int itemId);

    @Query("SELECT * FROM DailyProgress WHERE itemId = :itemId AND date = :date ORDER BY id DESC LIMIT 1")
    DailyProgress getByItemAndDate(int itemId, String date);

    @Query("SELECT * FROM DailyProgress WHERE itemId = :itemId ORDER BY date ASC")
    List<DailyProgress> getAllByItem(int itemId);

    @Query("DELETE FROM DailyProgress WHERE itemId = :itemId")
    void deleteByItem(int itemId);
}
