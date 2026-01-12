package com.sanjana.oneforall.database;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface ListItemDao {

    @Query("SELECT * FROM ListItem WHERE folderId = :folderId")
    List<ListItem> getByFolder(int folderId);

    @Insert
    void insert(ListItem item);

    @Update
    void update(ListItem item);

    @Delete
    void delete(ListItem item);

    @Query("SELECT * FROM ListItem WHERE id = :itemId LIMIT 1")
    ListItem getById(int itemId);

    @Query("SELECT * FROM ListItem ORDER BY updatedAt DESC")
    List<ListItem> getAll();
}
