package com.sanjana.oneforall.database;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Update;
import androidx.room.Delete;
import androidx.room.Query;

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

    // Get today's progress for an item; if multiple exist (shouldn't usually happen), pick latest by id
    @Query("SELECT * FROM DailyProgress WHERE itemId = :itemId AND date = :date ORDER BY id DESC LIMIT 1")
    DailyProgress getByItemAndDate(int itemId, String date);

    // Optional: get all daily progress for an item (useful if you want history)
    @Query("SELECT * FROM DailyProgress WHERE itemId = :itemId ORDER BY date ASC")
    java.util.List<DailyProgress> getAllByItem(int itemId);
}
