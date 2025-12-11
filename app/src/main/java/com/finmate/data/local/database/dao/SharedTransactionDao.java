package com.finmate.data.local.database.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.finmate.core.offline.PendingAction;
import com.finmate.core.offline.SyncDao;
import com.finmate.data.local.database.entity.SharedTransactionEntity;

import java.util.List;

@Dao
public interface SharedTransactionDao extends SyncDao<SharedTransactionEntity> {

    @Insert
    void insert(SharedTransactionEntity entity);

    @Update
    void update(SharedTransactionEntity entity);

    @Delete
    void delete(SharedTransactionEntity entity);

    @Query("SELECT * FROM shared_transactions ORDER BY createdAtIso DESC")
    List<SharedTransactionEntity> getAll();

    @Query("DELETE FROM shared_transactions")
    void deleteAll();

    @Query("SELECT * FROM shared_transactions WHERE pendingAction != :noneAction ORDER BY updatedAt ASC")
    List<SharedTransactionEntity> getPendingForSyncInternal(String noneAction);

    @Override
    default List<SharedTransactionEntity> getPendingForSync() {
        return getPendingForSyncInternal(PendingAction.NONE);
    }
}

