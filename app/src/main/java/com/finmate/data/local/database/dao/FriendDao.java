package com.finmate.data.local.database.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.finmate.core.offline.PendingAction;
import com.finmate.core.offline.SyncDao;
import com.finmate.data.local.database.entity.FriendEntity;

import java.util.List;

@Dao
public interface FriendDao extends SyncDao<FriendEntity> {

    @Insert
    void insert(FriendEntity entity);

    @Update
    void update(FriendEntity entity);

    @Delete
    void delete(FriendEntity entity);

    @Query("SELECT * FROM friends ORDER BY updatedAt DESC")
    List<FriendEntity> getAll();

    @Query("DELETE FROM friends")
    void deleteAll();

    @Query("SELECT * FROM friends WHERE pendingAction != :noneAction ORDER BY updatedAt ASC")
    List<FriendEntity> getPendingForSyncInternal(String noneAction);

    @Override
    default List<FriendEntity> getPendingForSync() {
        return getPendingForSyncInternal(PendingAction.NONE);
    }
}

