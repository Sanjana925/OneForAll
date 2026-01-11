package com.sanjana.oneforall.database;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface ListFolderDao {

    @Query("SELECT * FROM ListFolder")
    List<ListFolder> getAll();

    @Insert
    void insert(ListFolder folder);
}
