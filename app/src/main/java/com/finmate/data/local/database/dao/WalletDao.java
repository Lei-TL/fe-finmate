package com.finmate.data.local.database.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.finmate.core.offline.PendingAction;
import com.finmate.core.offline.SyncDao;
import com.finmate.data.local.database.entity.WalletEntity;

import java.util.List;

@Dao
public interface WalletDao extends SyncDao<WalletEntity> {

    @Insert
    void insert(WalletEntity wallet);

    @Update
    void update(WalletEntity wallet);

    @Delete
    void delete(WalletEntity wallet);

    @Query("SELECT * FROM wallets ORDER BY id DESC")
    List<WalletEntity> getAll();

    @Query("SELECT * FROM wallets WHERE id = :id LIMIT 1")
    WalletEntity getById(int id);

    @Query("SELECT * FROM wallets WHERE pendingAction != :noneAction ORDER BY updatedAt ASC")
    List<WalletEntity> getPendingForSyncInternal(String noneAction);

    @Override
    default List<WalletEntity> getPendingForSync() {
        return getPendingForSyncInternal(PendingAction.NONE);
    }
}
