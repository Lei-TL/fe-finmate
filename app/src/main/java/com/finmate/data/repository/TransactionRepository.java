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
        EXECUTOR.execute(() -> {
            long id = dao.insert(entity);
            entity.id = (int) id; // ✅ Set id sau khi insert
        });
    }
    
    // ✅ Insert với callback để lấy id sau khi insert
    public void insert(TransactionEntity entity, OnResultCallback<Long> callback) {
        EXECUTOR.execute(() -> {
            long id = dao.insert(entity);
            entity.id = (int) id; // ✅ Set id sau khi insert
            if (callback != null) {
                callback.onResult(id);
            }
        });
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
            try {
                // ✅ Nếu cả startDate và endDate đều null → load tất cả
                if (startDate == null && endDate == null) {
                    android.util.Log.d("TransactionRepository", "getByDateRange: loading all transactions (no date filter)");
                    callback.onResult(dao.getAll(limit));
                    return;
                }
                
                String startDateStr = startDate != null ? formatDateForQuery(startDate) : null;
                String endDateStr = endDate != null ? formatDateForQuery(endDate) : null;
                
                // ✅ Dùng SQL query thay vì load tất cả rồi filter
                List<TransactionEntity> result = dao.getByDateRange(startDateStr, endDateStr, limit);
                android.util.Log.d("TransactionRepository", "getByDateRange(" + startDateStr + ", " + endDateStr + "): loaded " + (result != null ? result.size() : 0) + " transactions");
                callback.onResult(result);
            } catch (Exception e) {
                android.util.Log.e("TransactionRepository", "Error in getByDateRange: " + e.getMessage(), e);
                callback.onResult(new java.util.ArrayList<>()); // Return empty list on error
            }
        });
    }

    // ✅ Filter by wallet and date range - dùng SQL thay vì filter in memory
    public void getByWalletNameAndDateRange(String walletName, Long startDate, Long endDate, OnResultCallback<List<TransactionEntity>> callback) {
        getByWalletNameAndDateRange(walletName, startDate, endDate, DEFAULT_LIMIT, callback);
    }
    
    public void getByWalletNameAndDateRange(String walletName, Long startDate, Long endDate, int limit, OnResultCallback<List<TransactionEntity>> callback) {
        EXECUTOR.execute(() -> {
            try {
                // ✅ Nếu cả startDate và endDate đều null → chỉ filter theo wallet
                if (startDate == null && endDate == null) {
                    android.util.Log.d("TransactionRepository", "getByWalletNameAndDateRange: loading by wallet only (" + walletName + ")");
                    callback.onResult(dao.getByWalletName(walletName, limit));
                    return;
                }
                
                String startDateStr = startDate != null ? formatDateForQuery(startDate) : null;
                String endDateStr = endDate != null ? formatDateForQuery(endDate) : null;
                
                // ✅ Dùng SQL query thay vì load tất cả rồi filter
                List<TransactionEntity> result = dao.getByWalletNameAndDateRange(walletName, startDateStr, endDateStr, limit);
                android.util.Log.d("TransactionRepository", "getByWalletNameAndDateRange(" + walletName + ", " + startDateStr + ", " + endDateStr + "): loaded " + (result != null ? result.size() : 0) + " transactions");
                callback.onResult(result);
            } catch (Exception e) {
                android.util.Log.e("TransactionRepository", "Error in getByWalletNameAndDateRange: " + e.getMessage(), e);
                callback.onResult(new java.util.ArrayList<>()); // Return empty list on error
            }
        });
    }
    
    // ✅ Method tối ưu cho sync manager - chỉ lấy transactions chưa sync
    // ✅ Ưu tiên check remoteId: chỉ lấy transactions không có remoteId
    public void getUnsyncedTransactions(List<Integer> syncedIds, int limit, OnResultCallback<List<TransactionEntity>> callback) {
        EXECUTOR.execute(() -> {
            // ✅ Lấy transactions không có remoteId (chưa sync) - ưu tiên hơn syncedIds
            List<TransactionEntity> unsynced = dao.getUnsyncedTransactionsByRemoteId(limit);
            callback.onResult(unsynced);
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
    
    public void deleteByLocalId(long localId) {
        EXECUTOR.execute(() -> dao.deleteById((int) localId));
    }

    /**
     * Ghi đè toàn bộ transaction local = data mới từ server.
     * ✅ Tối ưu: Không load tất cả existing vào memory, dùng bulk delete
     * ⚠️ CẢNH BÁO: Method này sẽ XÓA TẤT CẢ transactions cũ, chỉ giữ lại danh sách mới.
     * Nếu API chỉ trả về một phần dữ liệu (phân trang), sẽ mất dữ liệu cũ.
     * Nên dùng upsertAll() thay vì method này.
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

    /**
     * Upsert transactions: Insert nếu mới, Update nếu đã tồn tại (dựa trên remoteId).
     * ✅ An toàn hơn replaceAll: Không xóa transactions cũ, chỉ cập nhật/thêm mới.
     * ✅ Giữ nguyên các transactions tạo local (không có remoteId) và các transactions không có trong danh sách mới.
     * 
     * @param transactions Danh sách transactions từ backend (phải có remoteId)
     */
    public void upsertAll(List<TransactionEntity> transactions) {
        EXECUTOR.execute(() -> {
            for (TransactionEntity newTransaction : transactions) {
                // Chỉ upsert những transactions có remoteId (từ backend)
                if (newTransaction.remoteId == null || newTransaction.remoteId.isEmpty()) {
                    continue; // Bỏ qua transactions không có remoteId
                }
                
                // Tìm transaction đã tồn tại theo remoteId
                TransactionEntity existing = dao.getByRemoteId(newTransaction.remoteId);
                
                if (existing != null) {
                    // Đã tồn tại → Update (giữ nguyên local id)
                    newTransaction.id = existing.id;
                    dao.update(newTransaction);
                } else {
                    // Chưa tồn tại → Insert mới
                    dao.insert(newTransaction);
                }
            }
        });
    }

    public interface OnResultCallback<T> {
        void onResult(T data);
    }
}
