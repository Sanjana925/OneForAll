// Item.java
package com.sanjana.oneforall.database;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
@Entity
public class Item {
    @PrimaryKey(autoGenerate = true)
    public int id;

    public String title;
    public int categoryId;
    public String status;
    public int currentProgress;
    public int totalProgress;
    public String startDate;
    public String endDate;
    public int score;
    public String notes;

    public Item(String title, int categoryId, String status,
                int currentProgress, int totalProgress,
                String startDate, String endDate, int score, String notes) {
        this.title = title;
        this.categoryId = categoryId;
        this.status = status;
        this.currentProgress = currentProgress;
        this.totalProgress = totalProgress;
        this.startDate = startDate;
        this.endDate = endDate;
        this.score = score;
        this.notes = notes;
    }
}
