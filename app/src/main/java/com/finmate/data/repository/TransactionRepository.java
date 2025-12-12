package com.finmate.data.repository;

import android.content.Context;

import com.finmate.data.local.database.AppDatabase;
import com.finmate.data.local.database.dao.TransactionDao;
import com.finmate.data.local.database.entity.TransactionEntity;

import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import javax.inject.Inject;
import javax.inject.Singleton;

import dagger.hilt.android.qualifiers.ApplicationContext;

@Singleton
public class TransactionRepository {

    private static final Executor EXECUTOR = Executors.newSingleThreadExecutor();

    private final TransactionDao dao;

    @Inject
    public TransactionRepository(@ApplicationContext Context context) {
        dao = AppDatabase.getDatabase(context).transactionDao();
    }

    public void insert(TransactionEntity entity) {
        EXECUTOR.execute(() -> dao.insert(entity));
    }

    // ✅ Default limit cho UI (500 items đủ cho hầu hết trường hợp)
    private static final int DEFAULT_LIMIT = 500;
    
    public void getAll(OnResultCallback<List<TransactionEntity>> callback) {
        getAll(DEFAULT_LIMIT, callback);
    }
    
    public void getAll(int limit, OnResultCallback<List<TransactionEntity>> callback) {
        EXECUTOR.execute(() -> callback.onResult(dao.getAll(limit)));
    }

    public void getByWalletName(String walletName, OnResultCallback<List<TransactionEntity>> callback) {
        getByWalletName(walletName, DEFAULT_LIMIT, callback);
    }
    
    public void getByWalletName(String walletName, int limit, OnResultCallback<List<TransactionEntity>> callback) {
        EXECUTOR.execute(() -> callback.onResult(dao.getByWalletName(walletName, limit)));
    }

    // ✅ Filter by date range - dùng SQL thay vì filter in memory
    public void getByDateRange(Long startDate, Long endDate, OnResultCallback<List<TransactionEntity>> callback) {
        getByDateRange(startDate, endDate, DEFAULT_LIMIT, callback);
    }
    
    public void getByDateRange(Long startDate, Long endDate, int limit, OnResultCallback<List<TransactionEntity>> callback) {
        EXECUTOR.execute(() -> {
            String startDateStr = startDate != null ? formatDateForQuery(startDate) : null;
            String endDateStr = endDate != null ? formatDateForQuery(endDate) : null;
            // ✅ Dùng SQL query thay vì load tất cả rồi filter
            callback.onResult(dao.getByDateRange(startDateStr, endDateStr, limit));
        });
    }

    // ✅ Filter by wallet and date range - dùng SQL thay vì filter in memory
    public void getByWalletNameAndDateRange(String walletName, Long startDate, Long endDate, OnResultCallback<List<TransactionEntity>> callback) {
        getByWalletNameAndDateRange(walletName, startDate, endDate, DEFAULT_LIMIT, callback);
    }
    
    public void getByWalletNameAndDateRange(String walletName, Long startDate, Long endDate, int limit, OnResultCallback<List<TransactionEntity>> callback) {
        EXECUTOR.execute(() -> {
            String startDateStr = startDate != null ? formatDateForQuery(startDate) : null;
            String endDateStr = endDate != null ? formatDateForQuery(endDate) : null;
            // ✅ Dùng SQL query thay vì load tất cả rồi filter
            callback.onResult(dao.getByWalletNameAndDateRange(walletName, startDateStr, endDateStr, limit));
        });
    }
    
    // ✅ Method tối ưu cho sync manager - chỉ lấy transactions chưa sync
    public void getUnsyncedTransactions(List<Integer> syncedIds, int limit, OnResultCallback<List<TransactionEntity>> callback) {
        EXECUTOR.execute(() -> {
            if (syncedIds == null || syncedIds.isEmpty()) {
                // Nếu không có synced IDs, lấy tất cả (giới hạn)
                callback.onResult(dao.getAll(limit));
            } else {
                callback.onResult(dao.getUnsyncedTransactions(syncedIds, limit));
            }
        });
    }

    // ✅ Convert timestamp (milliseconds) to ISO date string for comparison
    private String formatDateForQuery(Long timestamp) {
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault());
        return sdf.format(new java.util.Date(timestamp));
    }

    public void update(TransactionEntity entity) {
        EXECUTOR.execute(() -> dao.update(entity));
    }

    public void delete(TransactionEntity entity) {
        EXECUTOR.execute(() -> dao.delete(entity));
    }

    /**
     * Ghi đè toàn bộ transaction local = data mới từ server.
     * ✅ Tối ưu: Không load tất cả existing vào memory, dùng bulk delete
     */
    public void replaceAll(List<TransactionEntity> transactions) {
        EXECUTOR.execute(() -> {
            // ✅ Xóa tất cả bằng SQL query thay vì load vào memory
            dao.deleteAll();
            // ✅ Insert batch
            for (TransactionEntity t : transactions) {
                dao.insert(t);
            }
        });
    }

    public interface OnResultCallback<T> {
        void onResult(T data);
    }
}
