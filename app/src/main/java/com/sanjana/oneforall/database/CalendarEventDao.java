package com.sanjana.oneforall.database;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Update;
import androidx.room.Delete;
import androidx.room.Query;

import java.util.List;

@Dao
public interface CalendarEventDao {

    @Insert
    long insert(CalendarEvent event);

    @Update
    void update(CalendarEvent event);

    @Delete
    void delete(CalendarEvent event);

    @Query("SELECT * FROM CalendarEvent WHERE title = :title AND date = :date LIMIT 1")
    CalendarEvent getEventByTitleAndDate(String title, String date);

    // ---------- NEW: get all events by date ----------
    @Query("SELECT * FROM CalendarEvent WHERE date = :date")
    List<CalendarEvent> getEventsByDate(String date);

    // Optional: for prefix search (for Watching event)
    @Query("SELECT * FROM CalendarEvent WHERE title LIKE :prefix || '%' AND date = :date LIMIT 1")
    CalendarEvent getEventByTitleAndDatePrefix(String prefix, String date);
}
