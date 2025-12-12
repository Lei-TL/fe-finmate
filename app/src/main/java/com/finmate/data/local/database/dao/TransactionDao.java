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
    void insert(TransactionEntity transaction);

    // ✅ Thêm limit để tránh load quá nhiều dữ liệu
    @Query("SELECT * FROM transactions ORDER BY id DESC LIMIT :limit")
    List<TransactionEntity> getAll(int limit);
    
    @Query("SELECT * FROM transactions ORDER BY id DESC")
    List<TransactionEntity> getAll();

    @Query("SELECT * FROM transactions WHERE wallet = :walletName ORDER BY id DESC LIMIT :limit")
    List<TransactionEntity> getByWalletName(String walletName, int limit);
    
    @Query("SELECT * FROM transactions WHERE wallet = :walletName ORDER BY id DESC")
    List<TransactionEntity> getByWalletName(String walletName);

    // ✅ Filter by date range với limit
    @Query("SELECT * FROM transactions WHERE (:startDate IS NULL OR date >= :startDate) AND (:endDate IS NULL OR date <= :endDate) ORDER BY id DESC LIMIT :limit")
    List<TransactionEntity> getByDateRange(String startDate, String endDate, int limit);
    
    @Query("SELECT * FROM transactions WHERE (:startDate IS NULL OR date >= :startDate) AND (:endDate IS NULL OR date <= :endDate) ORDER BY id DESC")
    List<TransactionEntity> getByDateRange(String startDate, String endDate);

    // ✅ Filter by wallet and date range với limit
    @Query("SELECT * FROM transactions WHERE wallet = :walletName AND (:startDate IS NULL OR date >= :startDate) AND (:endDate IS NULL OR date <= :endDate) ORDER BY id DESC LIMIT :limit")
    List<TransactionEntity> getByWalletNameAndDateRange(String walletName, String startDate, String endDate, int limit);
    
    @Query("SELECT * FROM transactions WHERE wallet = :walletName AND (:startDate IS NULL OR date >= :startDate) AND (:endDate IS NULL OR date <= :endDate) ORDER BY id DESC")
    List<TransactionEntity> getByWalletNameAndDateRange(String walletName, String startDate, String endDate);
    
    // ✅ Query để lấy transactions chưa sync (không có remoteId) - tối ưu cho sync manager
    @Query("SELECT * FROM transactions WHERE id NOT IN (:syncedIds) ORDER BY id DESC LIMIT :limit")
    List<TransactionEntity> getUnsyncedTransactions(List<Integer> syncedIds, int limit);

    @Query("SELECT * FROM transactions WHERE id = :id")
    TransactionEntity getById(int id);

    @Update
    void update(TransactionEntity transaction);

    @Delete
    void delete(TransactionEntity transaction);

    @Query("DELETE FROM transactions WHERE id = :id")
    void deleteById(int id);
    
    // ✅ Bulk delete để tối ưu memory
    @Query("DELETE FROM transactions")
    void deleteAll();
}
