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
    long insert(TransactionEntity transaction);

    @Query("SELECT * FROM transactions ORDER BY id DESC LIMIT :limit")
    List<TransactionEntity> getAll(int limit);

    @Query("SELECT * FROM transactions ORDER BY id DESC")
    List<TransactionEntity> getAll();

    @Query("SELECT * FROM transactions WHERE id = :id")
    TransactionEntity getById(int id);

    @Query("SELECT * FROM transactions WHERE remoteId = :remoteId LIMIT 1")
    TransactionEntity getByRemoteId(String remoteId);

    @Query("SELECT * FROM transactions WHERE remoteId IS NULL OR remoteId = '' ORDER BY id DESC LIMIT :limit")
    List<TransactionEntity> getUnsyncedTransactionsByRemoteId(int limit);

    @Update
    void update(TransactionEntity transaction);

    @Delete
    void delete(TransactionEntity transaction);

    @Query("DELETE FROM transactions WHERE id = :id")
    void deleteById(int id);

    @Query("DELETE FROM transactions")
    void deleteAll();
    
    // ✅ Filter by Wallet and Date
    @Query("SELECT * FROM transactions WHERE wallet = :walletName AND date >= :startDate AND date <= :endDate ORDER BY id DESC LIMIT :limit")
    List<TransactionEntity> getByWalletNameAndDateRange(String walletName, String startDate, String endDate, int limit);

    @Query("SELECT * FROM transactions WHERE wallet = :walletName AND date >= :startDate AND date <= :endDate ORDER BY id DESC")
    List<TransactionEntity> getByWalletNameAndDateRange(String walletName, String startDate, String endDate);

    // ✅ Filter by Date only
    @Query("SELECT * FROM transactions WHERE date >= :startDate AND date <= :endDate ORDER BY id DESC LIMIT :limit")
    List<TransactionEntity> getByDateRange(String startDate, String endDate, int limit);

    @Query("SELECT * FROM transactions WHERE date >= :startDate AND date <= :endDate ORDER BY id DESC")
    List<TransactionEntity> getByDateRange(String startDate, String endDate);
    
    // ✅ Filter by Wallet only (used for balance calculation)
    @Query("SELECT * FROM transactions WHERE wallet = :walletName ORDER BY id DESC LIMIT :limit")
    List<TransactionEntity> getByWalletName(String walletName, int limit);

    @Query("SELECT * FROM transactions WHERE wallet = :walletName ORDER BY id DESC")
    List<TransactionEntity> getByWalletName(String walletName);
}
