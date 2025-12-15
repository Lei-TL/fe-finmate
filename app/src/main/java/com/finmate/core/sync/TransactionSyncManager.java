package com.finmate.core.sync;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;

import com.finmate.core.network.ApiCallback;
import com.finmate.core.network.NetworkChecker;
import com.finmate.data.dto.CategoryResponse;
import com.finmate.data.dto.TransactionResponse;
import com.finmate.data.local.database.entity.TransactionEntity;
import com.finmate.data.local.database.entity.WalletEntity;
import com.finmate.data.remote.dto.CreateTransactionRequest;
import com.finmate.data.repository.CategoryRemoteRepository;
import com.finmate.data.repository.TransactionRemoteRepository;
import com.finmate.data.repository.TransactionRepository;
import com.finmate.data.repository.WalletRepository;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import javax.inject.Inject;
import javax.inject.Singleton;

import dagger.hilt.android.qualifiers.ApplicationContext;

/**
 * Manager để tự động sync pending transactions khi có mạng
 */
@Singleton
public class TransactionSyncManager {

    private final TransactionRepository transactionRepository;
    private final TransactionRemoteRepository transactionRemoteRepository;
    private final CategoryRemoteRepository categoryRemoteRepository;
    private final WalletRepository walletRepository;
    private final NetworkChecker networkChecker;
    private final Context context;
    private final SharedPreferences prefs;
    
    private final Handler handler = new Handler(Looper.getMainLooper());
    private static final long SYNC_INTERVAL_MS = 30_000; // 30 giây
    private static final String PREF_SYNCED_TRANSACTION_IDS = "synced_transaction_ids";
    private Runnable syncRunnable;

    @Inject
    public TransactionSyncManager(
            @ApplicationContext Context context,
            TransactionRepository transactionRepository,
            TransactionRemoteRepository transactionRemoteRepository,
            CategoryRemoteRepository categoryRemoteRepository,
            WalletRepository walletRepository,
            NetworkChecker networkChecker) {
        this.context = context;
        this.transactionRepository = transactionRepository;
        this.transactionRemoteRepository = transactionRemoteRepository;
        this.categoryRemoteRepository = categoryRemoteRepository;
        this.walletRepository = walletRepository;
        this.networkChecker = networkChecker;
        this.prefs = context.getSharedPreferences("sync_prefs", Context.MODE_PRIVATE);
    }

    /**
     * Bắt đầu auto-sync định kỳ
     */
    public void startAutoSync() {
        stopAutoSync(); // Đảm bảo không có nhiều runnable chạy cùng lúc
        
        syncRunnable = new Runnable() {
            @Override
            public void run() {
                syncPendingTransactions();
                // Lên lịch sync tiếp theo
                handler.postDelayed(this, SYNC_INTERVAL_MS);
            }
        };
        
        // Sync ngay lập tức
        handler.post(syncRunnable);
    }

    /**
     * Dừng auto-sync
     */
    public void stopAutoSync() {
        if (syncRunnable != null) {
            handler.removeCallbacks(syncRunnable);
            syncRunnable = null;
        }
    }

    // ✅ Limit số transactions sync mỗi lần để tránh tốn memory
    private static final int SYNC_BATCH_SIZE = 50;
    
    /**
     * Sync tất cả pending transactions
     * ✅ Tối ưu: Chỉ load transactions chưa sync, giới hạn số lượng
     */
    public void syncPendingTransactions() {
        if (!networkChecker.isNetworkAvailable()) {
            return; // Không có mạng, không sync
        }

        // ✅ Lấy danh sách transaction IDs đã sync
        String syncedIdsStr = prefs.getString(PREF_SYNCED_TRANSACTION_IDS, "");
        List<Integer> syncedIds = new java.util.ArrayList<>();
        if (!syncedIdsStr.isEmpty()) {
            String[] ids = syncedIdsStr.split(",");
            for (String id : ids) {
                try {
                    syncedIds.add(Integer.parseInt(id.trim()));
                } catch (NumberFormatException e) {
                    // Skip invalid IDs
                }
            }
        }

        // ✅ Chỉ lấy transactions chưa sync, giới hạn số lượng
        transactionRepository.getUnsyncedTransactions(syncedIds, SYNC_BATCH_SIZE, 
            new TransactionRepository.OnResultCallback<List<TransactionEntity>>() {
                @Override
                public void onResult(List<TransactionEntity> transactions) {
                    if (transactions == null || transactions.isEmpty()) {
                        return;
                    }
                    
                    // Sync từng transaction
                    for (TransactionEntity transaction : transactions) {
                        syncSingleTransaction(transaction);
                    }
                }
            });
    }

    /**
     * Sync một transaction cụ thể
     */
    private void syncSingleTransaction(TransactionEntity transaction) {
        // ✅ Skip nếu transaction đã có remoteId (đã được sync rồi)
        if (transaction.remoteId != null && !transaction.remoteId.isEmpty()) {
            return;
        }
        
        // Cần lấy walletId và categoryId từ walletName và categoryName
        final String[] walletId = {null};
        final String[] categoryId = {null};
        
        // 1. Lấy walletId từ walletName
        walletRepository.getAll(new WalletRepository.Callback() {
            @Override
            public void onResult(List<WalletEntity> wallets) {
                if (wallets != null && transaction.wallet != null) {
                    for (WalletEntity w : wallets) {
                        if (w.name.equals(transaction.wallet)) {
                            walletId[0] = w.id;
                            break;
                        }
                    }
                }
                
                // 2. Lấy categoryId từ categoryName
                if (transaction.category != null && transaction.type != null) {
                    String categoryType = transaction.type.toLowerCase();
                    categoryRemoteRepository.fetchCategoriesByType(categoryType, new ApiCallback<List<CategoryResponse>>() {
                        @Override
                        public void onSuccess(List<CategoryResponse> categories) {
                            if (categories != null) {
                                for (CategoryResponse cat : categories) {
                                    if (cat.getName().equals(transaction.category)) {
                                        categoryId[0] = cat.getId();
                                        break;
                                    }
                                }
                            }
                            
                            // 3. Sync nếu có đủ thông tin
                            if (walletId[0] != null && categoryId[0] != null) {
                                syncTransactionWithIds(walletId[0], categoryId[0], transaction, transaction.name);
                            }
                        }

                        @Override
                        public void onError(String message) {
                            // Không thể lấy categoryId, skip transaction này
                        }
                    });
                }
            }
        });
    }

    /**
     * Sync transaction với đầy đủ thông tin
     */
    private void syncTransactionWithIds(String walletId, String categoryId, TransactionEntity transaction, String note) {
        // Convert date từ transaction.date (yyyy-MM-dd) sang ISO datetime
        String occurredAtISO = transaction.date;
        if (occurredAtISO != null && !occurredAtISO.isEmpty()) {
            if (!occurredAtISO.contains("T")) {
                occurredAtISO = occurredAtISO + "T00:00:00";
            }
        } else {
            // Fallback: dùng ngày hiện tại
            Calendar c = Calendar.getInstance();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
            occurredAtISO = sdf.format(c.getTime());
        }

        CreateTransactionRequest request = new CreateTransactionRequest(
                walletId,
                categoryId,
                transaction.type != null ? transaction.type : "EXPENSE",
                BigDecimal.valueOf(transaction.amountDouble),
                "VND",
                occurredAtISO,
                note,
                null
        );

        transactionRemoteRepository.createTransaction(request, new ApiCallback<TransactionResponse>() {
            @Override
            public void onSuccess(TransactionResponse response) {
                // ✅ Update transaction local với remoteId từ backend để tránh duplicate
                if (response != null && response.getId() != null && !response.getId().isEmpty()) {
                    // ✅ Giữ nguyên local id, chỉ update remoteId
                    final int localId = transaction.id;
                    transaction.remoteId = response.getId();
                    // ✅ Đảm bảo id được giữ nguyên khi update
                    transaction.id = localId;
                    transactionRepository.update(transaction);
                }
                
                // ✅ Đánh dấu transaction đã sync (backward compatibility với SharedPreferences)
                String syncedIds = prefs.getString(PREF_SYNCED_TRANSACTION_IDS, "");
                String transactionKey = String.valueOf(transaction.id);
                if (!syncedIds.contains(transactionKey)) {
                    syncedIds = syncedIds.isEmpty() ? transactionKey : syncedIds + "," + transactionKey;
                    prefs.edit().putString(PREF_SYNCED_TRANSACTION_IDS, syncedIds).apply();
                }
            }

            @Override
            public void onError(String message) {
                // Sync failed, sẽ thử lại lần sau
            }
        });
    }
}
