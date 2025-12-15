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
    // ✅ Sort theo date DESC (mới nhất lên trên), nếu date giống nhau thì sort theo id DESC
    @Query("SELECT * FROM transactions ORDER BY date DESC, id DESC LIMIT :limit")
    List<TransactionEntity> getAll(int limit);
    
    @Query("SELECT * FROM transactions ORDER BY date DESC, id DESC")
    List<TransactionEntity> getAll();

    @Query("SELECT * FROM transactions WHERE wallet = :walletName ORDER BY date DESC, id DESC LIMIT :limit")
    List<TransactionEntity> getByWalletName(String walletName, int limit);
    
    @Query("SELECT * FROM transactions WHERE wallet = :walletName ORDER BY date DESC, id DESC")
    List<TransactionEntity> getByWalletName(String walletName);

    // ✅ Filter by date range với limit
    @Query("SELECT * FROM transactions WHERE (:startDate IS NULL OR date >= :startDate) AND (:endDate IS NULL OR date <= :endDate) ORDER BY date DESC, id DESC LIMIT :limit")
    List<TransactionEntity> getByDateRange(String startDate, String endDate, int limit);
    
    @Query("SELECT * FROM transactions WHERE (:startDate IS NULL OR date >= :startDate) AND (:endDate IS NULL OR date <= :endDate) ORDER BY date DESC, id DESC")
    List<TransactionEntity> getByDateRange(String startDate, String endDate);

    // ✅ Filter by wallet and date range với limit
    @Query("SELECT * FROM transactions WHERE wallet = :walletName AND (:startDate IS NULL OR date >= :startDate) AND (:endDate IS NULL OR date <= :endDate) ORDER BY date DESC, id DESC LIMIT :limit")
    List<TransactionEntity> getByWalletNameAndDateRange(String walletName, String startDate, String endDate, int limit);
    
    @Query("SELECT * FROM transactions WHERE wallet = :walletName AND (:startDate IS NULL OR date >= :startDate) AND (:endDate IS NULL OR date <= :endDate) ORDER BY date DESC, id DESC")
    List<TransactionEntity> getByWalletNameAndDateRange(String walletName, String startDate, String endDate);
    
    // ✅ Query để lấy transactions chưa sync (không có remoteId) - tối ưu cho sync manager
    @Query("SELECT * FROM transactions WHERE id NOT IN (:syncedIds) ORDER BY date DESC, id DESC LIMIT :limit")
    List<TransactionEntity> getUnsyncedTransactions(List<Integer> syncedIds, int limit);
    
    // ✅ Query để lấy transactions không có remoteId (chưa sync) - ưu tiên hơn syncedIds
    @Query("SELECT * FROM transactions WHERE remoteId IS NULL OR remoteId = '' ORDER BY date DESC, id DESC LIMIT :limit")
    List<TransactionEntity> getUnsyncedTransactionsByRemoteId(int limit);

    @Query("SELECT * FROM transactions WHERE id = :id")
    TransactionEntity getById(int id);
    
    // ✅ Query transaction theo remoteId để check xem đã tồn tại chưa
    @Query("SELECT * FROM transactions WHERE remoteId = :remoteId LIMIT 1")
    TransactionEntity getByRemoteId(String remoteId);

    @Update
    void update(TransactionEntity transaction);

    @Delete
    void delete(TransactionEntity transaction);

    @Query("DELETE FROM transactions WHERE id = :id")
    void deleteById(long id);
    
    // ✅ Bulk delete để tối ưu memory
    @Query("DELETE FROM transactions")
    void deleteAll();
    
    // ✅ Upsert: Insert nếu chưa có (dựa trên remoteId), Update nếu đã có
    // Room không có upsert built-in, nên sẽ implement trong Repository
}
