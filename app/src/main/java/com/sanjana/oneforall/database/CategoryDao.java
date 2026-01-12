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

    // ✅ REQUIRED for CategoryFragment
    @Query("SELECT * FROM Category ORDER BY name ASC")
    List<Category> getAllCategories();

    // ✅ REQUIRED for Edit Category
    @Query("SELECT * FROM Category WHERE id = :id LIMIT 1")
    Category getCategoryById(int id);
}
