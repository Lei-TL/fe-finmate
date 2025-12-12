package com.finmate.data.local.database.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import com.finmate.data.local.database.entity.PendingSyncEntity;

import java.util.List;

@Dao
public interface PendingSyncDao {

    @Insert
    void insert(PendingSyncEntity pendingSync);

    @Query("SELECT * FROM pending_sync ORDER BY createdAt ASC")
    List<PendingSyncEntity> getAll();

    @Query("SELECT * FROM pending_sync WHERE entityType = :entityType ORDER BY createdAt ASC")
    List<PendingSyncEntity> getByEntityType(String entityType);

    @Query("SELECT * FROM pending_sync WHERE entityType = :entityType AND localEntityId = :localEntityId")
    PendingSyncEntity getByEntityTypeAndId(String entityType, int localEntityId);

    @Delete
    void delete(PendingSyncEntity pendingSync);

    @Query("DELETE FROM pending_sync WHERE id = :id")
    void deleteById(long id);

    @Query("DELETE FROM pending_sync WHERE entityType = :entityType AND localEntityId = :localEntityId")
    void deleteByEntityTypeAndId(String entityType, int localEntityId);
}

