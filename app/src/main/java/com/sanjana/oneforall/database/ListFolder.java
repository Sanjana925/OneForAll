package com.sanjana.oneforall.database;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class ListFolder {

    @PrimaryKey(autoGenerate = true)
    public int id;

    public String name;
    public long createdAt;

    public ListFolder(String name) {
        this.name = name;
        this.createdAt = createdAt;
    }
}
