package com.sanjana.oneforall.database;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface CategoryDao {

    @Insert
    void insert(Category category);

    @Update
    void update(Category category);

    @Delete
    void delete(Category category);

    @Query("SELECT * FROM Category ORDER BY name ASC")
    List<Category> getAllCategories();

    @Query("SELECT * FROM Category WHERE id = :id LIMIT 1")
    Category getCategoryById(int id);
}
