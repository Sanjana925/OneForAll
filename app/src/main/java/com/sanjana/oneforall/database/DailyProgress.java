package com.sanjana.oneforall.database;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class DailyProgress {
    @PrimaryKey(autoGenerate = true)
    public int id;

    public int itemId;       // reference to Item
    public String date;      // yyyy-MM-dd
    public int firstEp;
    public int lastEp;

    public DailyProgress(int itemId, String date, int firstEp, int lastEp) {
        this.itemId = itemId;
        this.date = date;
        this.firstEp = firstEp;
        this.lastEp = lastEp;
    }
}
