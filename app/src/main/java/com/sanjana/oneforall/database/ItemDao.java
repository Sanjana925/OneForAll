// ItemDao.java
package com.sanjana.oneforall.database;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;
@Dao
public interface ItemDao {
    @Insert
    long insert(Item item);

    @Update
    void update(Item item);

    @Delete
    void delete(Item item);

    @Query("SELECT * FROM Item")
    List<Item> getAllItems();

    @Query("SELECT * FROM Item WHERE id = :id")
    Item getItemById(int id);

    @Query("SELECT * FROM Item WHERE categoryId = :catId")
    List<Item> getItemsByCategory(int catId);

    @Query("SELECT * FROM Item WHERE status = :status")
    List<Item> getItemsByStatus(String status);

    @Query("SELECT * FROM Item WHERE categoryId = :catId AND status = :status")
    List<Item> getItemsByCategoryAndStatus(int catId, String status);

    // ----------------- NEW -----------------
    @Query("SELECT * FROM Item WHERE title = :title LIMIT 1")
    Item getItemByTitle(String title);
}
