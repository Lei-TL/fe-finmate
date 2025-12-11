package com.finmate.data.local.database.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.finmate.core.offline.PendingAction;
import com.finmate.core.offline.SyncDao;
import com.finmate.data.local.database.entity.TransactionEntity;
import com.finmate.data.local.database.model.StatisticPoint;

import java.util.List;

@Dao
public interface TransactionDao extends SyncDao<TransactionEntity> {

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

    @Query("SELECT * FROM transactions WHERE pendingAction != :noneAction ORDER BY updatedAt ASC")
    List<TransactionEntity> getPendingForSyncInternal(String noneAction);

    @Override
    default List<TransactionEntity> getPendingForSync() {
        return getPendingForSyncInternal(PendingAction.NONE);
    }

    @Query("SELECT IFNULL(SUM(amount), 0) FROM transactions " +
            "WHERE type = :type " +
            "AND pendingAction != :ignoredAction " +
            "AND (:startDate IS NULL OR occurredAt >= :startDate) " +
            "AND (:endDate IS NULL OR occurredAt <= :endDate) " +
            "AND (:walletName IS NULL OR :walletName = '' OR walletName = :walletName)")
    double sumAmount(String type, String startDate, String endDate, String walletName, String ignoredAction);

    @Query("SELECT IFNULL(SUM(amount), 0) FROM transactions " +
            "WHERE type = :type " +
            "AND pendingAction != :ignoredAction " +
            "AND (:startDate IS NULL OR occurredAt >= :startDate) " +
            "AND (:endDate IS NULL OR occurredAt <= :endDate) " +
            "AND (:walletName IS NULL OR :walletName = '' OR walletName = :walletName) " +
            "AND (:categoryName IS NULL OR :categoryName = '' OR categoryName = :categoryName)")
    double sumAmountWithCategory(String type, String startDate, String endDate, String walletName, String categoryName, String ignoredAction);

    @Query("SELECT IFNULL(SUM(amount), 0) FROM transactions " +
            "WHERE type = :type " +
            "AND pendingAction != :ignoredAction " +
            "AND (:startDate IS NULL OR occurredAt >= :startDate) " +
            "AND (:endDate IS NULL OR occurredAt <= :endDate) " +
            "AND (:walletName IS NULL OR :walletName = '' OR walletName = :walletName) " +
            "AND (:categoryName IS NULL OR :categoryName = '' OR categoryName = :categoryName)")
    double sumAmountForBudget(String type, String startDate, String endDate, String walletName, String categoryName, String ignoredAction);

    @Query("SELECT strftime('%Y-%m-%d', replace(occurredAt, 'T', ' ')) AS label, SUM(amount) AS total FROM transactions " +
            "WHERE type = :type " +
            "AND pendingAction != :ignoredAction " +
            "AND (:startDate IS NULL OR occurredAt >= :startDate) " +
            "AND (:endDate IS NULL OR occurredAt <= :endDate) " +
            "AND (:walletName IS NULL OR :walletName = '' OR walletName = :walletName) " +
            "GROUP BY label " +
            "ORDER BY label ASC")
    List<StatisticPoint> sumByDay(String type, String startDate, String endDate, String walletName, String ignoredAction);

    @Query("SELECT strftime('%Y-%W', replace(occurredAt, 'T', ' ')) AS label, SUM(amount) AS total FROM transactions " +
            "WHERE type = :type " +
            "AND pendingAction != :ignoredAction " +
            "AND (:startDate IS NULL OR occurredAt >= :startDate) " +
            "AND (:endDate IS NULL OR occurredAt <= :endDate) " +
            "AND (:walletName IS NULL OR :walletName = '' OR walletName = :walletName) " +
            "GROUP BY label " +
            "ORDER BY label ASC")
    List<StatisticPoint> sumByWeek(String type, String startDate, String endDate, String walletName, String ignoredAction);

    @Query("SELECT strftime('%Y-%m', replace(occurredAt, 'T', ' ')) AS label, SUM(amount) AS total FROM transactions " +
            "WHERE type = :type " +
            "AND pendingAction != :ignoredAction " +
            "AND (:startDate IS NULL OR occurredAt >= :startDate) " +
            "AND (:endDate IS NULL OR occurredAt <= :endDate) " +
            "AND (:walletName IS NULL OR :walletName = '' OR walletName = :walletName) " +
            "GROUP BY label " +
            "ORDER BY label ASC")
    List<StatisticPoint> sumByMonth(String type, String startDate, String endDate, String walletName, String ignoredAction);

    @Query("SELECT categoryName AS label, SUM(amount) AS total FROM transactions " +
            "WHERE type = :type " +
            "AND pendingAction != :ignoredAction " +
            "AND (:startDate IS NULL OR occurredAt >= :startDate) " +
            "AND (:endDate IS NULL OR occurredAt <= :endDate) " +
            "AND (:walletName IS NULL OR :walletName = '' OR walletName = :walletName) " +
            "GROUP BY categoryName " +
            "ORDER BY total DESC")
    List<StatisticPoint> sumByCategory(String type, String startDate, String endDate, String walletName, String ignoredAction);

    @Query("SELECT walletName AS label, SUM(amount) AS total FROM transactions " +
            "WHERE type = :type " +
            "AND pendingAction != :ignoredAction " +
            "AND (:startDate IS NULL OR occurredAt >= :startDate) " +
            "AND (:endDate IS NULL OR occurredAt <= :endDate) " +
            "AND (:walletName IS NULL OR :walletName = '' OR walletName = :walletName) " +
            "GROUP BY walletName " +
            "ORDER BY total DESC")
    List<StatisticPoint> sumByWallet(String type, String startDate, String endDate, String walletName, String ignoredAction);
}
