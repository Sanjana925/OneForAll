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
    public long updatedAt;
    public long timestamp;


    public ListItem(int folderId, String title, String content, long updatedAt) {
        this.folderId = folderId;
        this.title = title;
        this.content = content;
        this.updatedAt = updatedAt;
    }
}
