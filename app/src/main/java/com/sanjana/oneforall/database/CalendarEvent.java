package com.sanjana.oneforall.database;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "calendar_events")
public class CalendarEvent {
    @PrimaryKey(autoGenerate = true)
    public int id;

    @NonNull
    public String title;

    @NonNull
    public String date; // yyyy-MM-dd

    public String status;

    public CalendarEvent(String title, String date, String status) {
        this.title = title;
        this.date = date;
        this.status = status;
    }
}

