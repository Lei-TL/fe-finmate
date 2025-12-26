package com.finmate.data.local.database.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.finmate.data.local.database.entity.CategoryEntity;

import java.util.List;

@Dao
public interface CategoryDao {

    @Insert
    long insert(CategoryEntity category);

    @Update
    int update(CategoryEntity category);

    @Delete
    int delete(CategoryEntity category);

    @Query("SELECT * FROM categories WHERE type = :type ORDER BY id DESC")
    LiveData<List<CategoryEntity>> getByType(String type);

    @Query("SELECT * FROM categories ORDER BY id DESC")
    LiveData<List<CategoryEntity>> getAll();

    @Query("SELECT * FROM categories WHERE name = :name LIMIT 1")
    CategoryEntity getByName(String name);

    @Query("SELECT COUNT(*) FROM categories")
    int getCount();

    @Query("DELETE FROM categories WHERE type = :type")
    int deleteByType(String type);

    @Query("DELETE FROM categories")
    void deleteAll();
}
