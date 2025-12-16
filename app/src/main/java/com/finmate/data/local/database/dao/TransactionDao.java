package com.finmate.data.local.database.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.finmate.data.local.database.entity.TransactionEntity;
import com.finmate.data.local.database.entity.MonthlyAggregate;

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
    // ✅ Sử dụng SUBSTR để extract date part (yyyy-MM-dd) từ ISO format (yyyy-MM-ddTHH:mm:ssZ)
    // ✅ Hỗ trợ cả format ISO và format date đơn giản
    @Query("SELECT * FROM transactions WHERE wallet = :walletName AND SUBSTR(date, 1, 10) >= :startDate AND SUBSTR(date, 1, 10) <= :endDate ORDER BY id DESC LIMIT :limit")
    List<TransactionEntity> getByWalletNameAndDateRange(String walletName, String startDate, String endDate, int limit);

    // ✅ Pagination: với offset
    @Query("SELECT * FROM transactions WHERE wallet = :walletName AND SUBSTR(date, 1, 10) >= :startDate AND SUBSTR(date, 1, 10) <= :endDate ORDER BY id DESC LIMIT :limit OFFSET :offset")
    List<TransactionEntity> getByWalletNameAndDateRange(String walletName, String startDate, String endDate, int limit, int offset);

    @Query("SELECT * FROM transactions WHERE wallet = :walletName AND SUBSTR(date, 1, 10) >= :startDate AND SUBSTR(date, 1, 10) <= :endDate ORDER BY id DESC")
    List<TransactionEntity> getByWalletNameAndDateRange(String walletName, String startDate, String endDate);

    // ✅ Filter by Date only
    // ✅ Sử dụng SUBSTR để extract date part (yyyy-MM-dd) từ ISO format (yyyy-MM-ddTHH:mm:ssZ)
    // ✅ Hỗ trợ cả format ISO và format date đơn giản
    @Query("SELECT * FROM transactions WHERE SUBSTR(date, 1, 10) >= :startDate AND SUBSTR(date, 1, 10) <= :endDate ORDER BY id DESC LIMIT :limit")
    List<TransactionEntity> getByDateRange(String startDate, String endDate, int limit);

    // ✅ Pagination: với offset
    @Query("SELECT * FROM transactions WHERE SUBSTR(date, 1, 10) >= :startDate AND SUBSTR(date, 1, 10) <= :endDate ORDER BY id DESC LIMIT :limit OFFSET :offset")
    List<TransactionEntity> getByDateRange(String startDate, String endDate, int limit, int offset);

    @Query("SELECT * FROM transactions WHERE SUBSTR(date, 1, 10) >= :startDate AND SUBSTR(date, 1, 10) <= :endDate ORDER BY id DESC")
    List<TransactionEntity> getByDateRange(String startDate, String endDate);
    
    // ✅ Filter by Wallet only (used for balance calculation)
    @Query("SELECT * FROM transactions WHERE wallet = :walletName ORDER BY id DESC LIMIT :limit")
    List<TransactionEntity> getByWalletName(String walletName, int limit);

    @Query("SELECT * FROM transactions WHERE wallet = :walletName ORDER BY id DESC")
    List<TransactionEntity> getByWalletName(String walletName);
    
    // ✅ Aggregate query: Tính tổng income/expense theo tháng (tối ưu cho chart)
    // ✅ Chỉ trả về tối đa 6 rows (6 tháng) thay vì hàng nghìn transactions
    // ✅ Room sẽ tự động map vào MonthlyAggregate nếu constructor parameters match column names
    // ✅ SUBSTR(date, 1, 7) để lấy "yyyy-MM" từ cả format "yyyy-MM-dd" và "yyyy-MM-ddTHH:mm:ssZ"
    @Query("SELECT " +
           "SUBSTR(date, 1, 7) as month, " +
           "COALESCE(SUM(CASE WHEN type = 'INCOME' THEN amountDouble ELSE 0 END), 0.0) as totalIncome, " +
           "COALESCE(SUM(CASE WHEN type = 'EXPENSE' THEN amountDouble ELSE 0 END), 0.0) as totalExpense " +
           "FROM transactions " +
           "WHERE (SUBSTR(date, 1, 10) >= :startDate AND SUBSTR(date, 1, 10) <= :endDate) " +
           "  AND date IS NOT NULL AND date != '' " +
           "  AND type IS NOT NULL " +
           "GROUP BY SUBSTR(date, 1, 7) " +
           "ORDER BY month ASC")
    List<MonthlyAggregate> getMonthlyAggregate(String startDate, String endDate);
}
