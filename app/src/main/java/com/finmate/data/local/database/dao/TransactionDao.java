package com.finmate.data.local.database.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.finmate.data.local.database.entity.TransactionEntity;

import java.util.List;

@Dao
public interface TransactionDao {

    @Insert
    void insert(TransactionEntity entity);

    @Update
    void update(TransactionEntity entity);

    @Delete
    void delete(TransactionEntity entity);

    @Query("SELECT * FROM transactions ORDER BY id DESC")
    List<TransactionEntity> getAll();

    @Query("DELETE FROM transactions WHERE syncStatus = :status")
    void deleteByStatus(String status);

    @Query("SELECT * FROM transactions WHERE syncStatus != 'SYNCED'")
    List<TransactionEntity> getPendingTransactions();
}
