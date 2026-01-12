package com.sanjana.oneforall.database;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class CalendarEvent {

    @PrimaryKey(autoGenerate = true)
    public int id;

    public String title;       // Drama title
    public String date;        // yyyy-MM-dd

    public int episodeCount;   // X
    public int startEp;        // Y
    public int endEp;          // Z

    public int categoryColor;  // same color as category

    public CalendarEvent(
            String title,
            String date,
            int episodeCount,
            int startEp,
            int endEp,
            int categoryColor
    ) {
        this.title = title;
        this.date = date;
        this.episodeCount = episodeCount;
        this.startEp = startEp;
        this.endEp = endEp;
        this.categoryColor = categoryColor;
    }
}
