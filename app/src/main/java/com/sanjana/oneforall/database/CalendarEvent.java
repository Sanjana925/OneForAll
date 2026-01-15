package com.sanjana.oneforall.database;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class CalendarEvent {

    @PrimaryKey(autoGenerate = true)
    public int id;

    public String title;
    public String date;

    public int episodeCount;
    public int startEp;
    public int endEp;

    public int categoryColor;

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
