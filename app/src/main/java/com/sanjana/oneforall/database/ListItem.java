package com.sanjana.oneforall.database;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class ListItem {

    @PrimaryKey(autoGenerate = true)
    public int id;

    public int folderId;      // belongs to folder
    public String title;
    public String content;    // free text / bullets
    public long updatedAt;    // last edited
    public long timestamp;    // creation time

    public ListItem(int folderId, String title, String content, long timestamp) {
        this.folderId = folderId;
        this.title = title;
        this.content = content;
        this.updatedAt = timestamp;
        this.timestamp = timestamp;
    }
}
