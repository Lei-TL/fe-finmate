package com.finmate.data.local.database.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.finmate.core.offline.PendingAction;
import com.finmate.core.offline.SyncDao;
import com.finmate.data.local.database.entity.CategoryEntity;

import java.util.List;

@Dao
public interface CategoryDao extends SyncDao<CategoryEntity> {

    @Insert
    long insert(CategoryEntity category);

    @Update
    int update(CategoryEntity category);

    @Delete
    int delete(CategoryEntity category);

    @Query("SELECT * FROM categories WHERE type = :type ORDER BY id DESC")
    LiveData<List<CategoryEntity>> getByType(String type);

    @Query("SELECT * FROM categories WHERE type = :type ORDER BY id DESC")
    List<CategoryEntity> getByTypeSync(String type);

    @Query("SELECT * FROM categories ORDER BY id DESC")
    LiveData<List<CategoryEntity>> getAll();

    @Query("SELECT * FROM categories ORDER BY id DESC")
    List<CategoryEntity> getAllSync();

    @Query("DELETE FROM categories WHERE type = :type")
    int deleteByType(String type);

    @Query("SELECT * FROM categories WHERE pendingAction != :noneAction ORDER BY updatedAt ASC")
    List<CategoryEntity> getPendingForSyncInternal(String noneAction);

    @Override
    default List<CategoryEntity> getPendingForSync() {
        return getPendingForSyncInternal(PendingAction.NONE);
    }
}
