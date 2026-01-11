// Category.java
package com.sanjana.oneforall.database;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class Category {

    @PrimaryKey(autoGenerate = true)
    public int id;

    public String name;
    public int color;

    public Category(String name, int color) {
        this.name = name;
        this.color = color;
    }
    @Override
    public String toString() {
        return name; // Spinner will display the category name
    }
}
