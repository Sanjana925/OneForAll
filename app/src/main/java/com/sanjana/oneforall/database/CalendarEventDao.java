package com.sanjana.oneforall.database;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Delete;
import java.util.List;

@Dao
public interface CalendarEventDao {

    @Query("SELECT * FROM calendar_events WHERE date = :date")
    List<CalendarEvent> getEventsByDate(String date);

    @Query("SELECT * FROM calendar_events WHERE title = :title AND date = :date LIMIT 1")
    CalendarEvent getEventByTitleAndDate(String title, String date);

    @Insert
    void insert(CalendarEvent event);

    @Delete
    void delete(CalendarEvent event);
}
