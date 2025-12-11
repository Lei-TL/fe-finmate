package com.finmate.data.local.database.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.finmate.core.offline.PendingAction;
import com.finmate.core.offline.SyncDao;
import com.finmate.data.local.database.entity.GoalEntity;

import java.util.List;

@Dao
public interface GoalDao extends SyncDao<GoalEntity> {

    @Insert
    void insert(GoalEntity goal);

    @Update
    void update(GoalEntity goal);

    @Delete
    void delete(GoalEntity goal);

    @Query("SELECT * FROM goals ORDER BY id DESC")
    List<GoalEntity> getAll();

    @Query("SELECT * FROM goals WHERE pendingAction != :noneAction ORDER BY updatedAt ASC")
    List<GoalEntity> getPendingForSyncInternal(String noneAction);

    @Override
    default List<GoalEntity> getPendingForSync() {
        return getPendingForSyncInternal(PendingAction.NONE);
    }
}
