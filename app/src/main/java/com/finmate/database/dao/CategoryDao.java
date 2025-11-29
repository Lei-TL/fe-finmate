package com.finmate.database.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.finmate.entities.CategoryEntity;

import java.util.List;

@Dao
public interface CategoryDao {

    @Insert
    void insert(CategoryEntity category);

    @Update
    void update(CategoryEntity category);

    @Delete
    void delete(CategoryEntity category);

    @Query("SELECT * FROM categories WHERE type = :type ORDER BY id DESC")
    List<CategoryEntity> getByType(String type);

    @Query("SELECT * FROM categories ORDER BY id DESC")
    List<CategoryEntity> getAll();
}
