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

    @Query("DELETE FROM CalendarEvent WHERE id = :itemId")
    void deleteByItemId(int itemId);

    @Query("SELECT * FROM CalendarEvent WHERE title = :title AND date = :date LIMIT 1")
    CalendarEvent getEventByTitleAndDate(String title, String date);

    @Query("SELECT * FROM CalendarEvent WHERE date = :date")
    List<CalendarEvent> getEventsByDate(String date);

    @Query("SELECT * FROM CalendarEvent WHERE title LIKE :prefix || '%' AND date = :date LIMIT 1")
    CalendarEvent getEventByTitleAndDatePrefix(String prefix, String date);

    @Query("SELECT * FROM CalendarEvent WHERE title = :title")
    List<CalendarEvent> getEventsByTitle(String title);

    // ✅ New method for drag & drop
    @Query("UPDATE CalendarEvent SET date = :newDate WHERE id = :eventId")
    void updateEventDate(int eventId, String newDate);
}
