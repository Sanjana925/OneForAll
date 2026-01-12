package com.sanjana.oneforall.database;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class CalendarEvent {

    @PrimaryKey(autoGenerate = true)
    public int id;

    public String title;    // e.g., "My Show (Watching) 3 eps (1-3)"
    public String date;     // yyyy-MM-dd
    public String status;   // "Started", "Watching", "Completed"

    public CalendarEvent(String title, String date, String status) {
        this.title = title;
        this.date = date;
        this.status = status;
    }
}
