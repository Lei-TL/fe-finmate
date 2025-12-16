package com.finmate.data.repository;

import android.content.Context;

import com.finmate.core.network.ApiCallback;
import com.finmate.core.network.NetworkChecker;
import com.finmate.data.dto.TransactionPageResponse;
import com.finmate.data.dto.TransactionResponse;
import com.finmate.data.dto.WalletResponse;
import com.finmate.data.local.database.AppDatabase;
import com.finmate.data.local.database.entity.TransactionEntity;
import com.finmate.data.local.database.entity.WalletEntity;
import com.finmate.data.local.database.entity.MonthlyAggregate;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Singleton;

import dagger.hilt.android.qualifiers.ApplicationContext;
import io.reactivex.rxjava3.core.Completable;

@Singleton
public class HomeRepository {

    private final WalletRemoteRepository walletRemoteRepository;
    private final WalletRepository walletLocalRepository;
    private final TransactionRemoteRepository transactionRemoteRepository;
    private final TransactionRepository transactionLocalRepository;
    private final CategoryRepository categoryRepository; // ✅ Thêm để map categoryId -> categoryName
    private final NetworkChecker networkChecker;
    private final Context context;

    @Inject
    public HomeRepository(WalletRemoteRepository walletRemoteRepository, 
                         WalletRepository walletLocalRepository, 
                         TransactionRemoteRepository transactionRemoteRepository, 
                         TransactionRepository transactionLocalRepository,
                         CategoryRepository categoryRepository,
                         NetworkChecker networkChecker,
                         @ApplicationContext Context context) {
        this.walletRemoteRepository = walletRemoteRepository;
        this.walletLocalRepository = walletLocalRepository;
        this.transactionRemoteRepository = transactionRemoteRepository;
        this.transactionLocalRepository = transactionLocalRepository;
        this.categoryRepository = categoryRepository;
        this.networkChecker = networkChecker;
        this.context = context;
    }

    public interface DataCallback<T> {
        void onDataLoaded(T data);
        void onError(String message);
    }
    
    /**
     * ✅ Callback cho sync operation
     */
    public interface SyncCallback {
        void onSyncComplete(); // Sync thành công
        void onSyncSkipped(); // Skip sync (không có mạng hoặc đã sync)
        void onSyncError(String message); // Sync lỗi
    }

    /**
     * ✅ Sync từ backend (wallets và transactions) - chỉ gọi một lần khi khởi tạo Home
     */
    public void syncFromBackend(SyncCallback callback) {
        if (!networkChecker.isNetworkAvailable()) {
            android.util.Log.d("HomeRepository", "No network, skipping sync");
            if (callback != null) {
                callback.onSyncSkipped();
            }
            return;
        }
        
        android.util.Log.d("HomeRepository", "Starting sync from backend...");
        
        // Sync wallets trước
        walletRemoteRepository.fetchMyWallets(new ApiCallback<List<WalletResponse>>() {
            @Override
            public void onSuccess(List<WalletResponse> body) {
                // Map và upsert wallets
                List<WalletEntity> mapped = new java.util.ArrayList<>();
                if (body != null) {
                    for (WalletResponse w : body) {
                        if (w.isDeleted() || w.getId() == null || w.getId().isEmpty()) {
                            continue;
                        }
                        
                        double currentBalance = w.getCurrentBalance() != 0.0 ? w.getCurrentBalance() : w.getInitialBalance();
                        double initialBalance = w.getInitialBalance();
                        
                        String formattedBalance = String.format(
                                "%,.0f %s",
                                currentBalance,
                                w.getCurrency() != null ? w.getCurrency() : ""
                        );

                        WalletEntity entity = new WalletEntity(
                                w.getId(),
                                w.getName(),
                                formattedBalance,
                                currentBalance,
                                initialBalance,
                                0
                        );
                        mapped.add(entity);
                    }
                }
                
                walletLocalRepository.upsertAll(mapped);
                android.util.Log.d("HomeRepository", "Wallets synced: " + mapped.size() + " items");
                
                // ✅ Sync transactions: Fetch tất cả pages để có đủ data cho chart (6 tháng gần nhất)
                // ✅ Tính date range cho 6 tháng gần nhất để filter
                java.util.Calendar cal = java.util.Calendar.getInstance();
                cal.set(java.util.Calendar.DAY_OF_MONTH, cal.getActualMaximum(java.util.Calendar.DAY_OF_MONTH));
                cal.set(java.util.Calendar.HOUR_OF_DAY, 23);
                cal.set(java.util.Calendar.MINUTE, 59);
                cal.set(java.util.Calendar.SECOND, 59);
                cal.set(java.util.Calendar.MILLISECOND, 999);
                long endDate = cal.getTimeInMillis();
                
                cal = java.util.Calendar.getInstance();
                cal.add(java.util.Calendar.MONTH, -5);
                cal.set(java.util.Calendar.DAY_OF_MONTH, 1);
                cal.set(java.util.Calendar.HOUR_OF_DAY, 0);
                cal.set(java.util.Calendar.MINUTE, 0);
                cal.set(java.util.Calendar.SECOND, 0);
                cal.set(java.util.Calendar.MILLISECOND, 0);
                long startDate = cal.getTimeInMillis();
                
                // ✅ Fetch tất cả transactions (không filter, vì API không hỗ trợ date range)
                // ✅ Sẽ filter và chỉ lưu transactions trong 6 tháng gần nhất
                // ✅ Lấy wallets từ local để map walletId -> walletName
                walletLocalRepository.getAll(new WalletRepository.Callback() {
                    @Override
                    public void onResult(List<WalletEntity> wallets) {
                        fetchAllTransactionsRecursive(null, 0, startDate, endDate, wallets, callback);
                    }
                });
            }

            @Override
            public void onError(String message) {
                android.util.Log.e("HomeRepository", "Error syncing wallets: " + message);
                if (callback != null) {
                    callback.onSyncError(message);
                }
            }
        });
    }
    
    /**
     * ✅ Load wallets từ local Room database (không sync)
     */
    public void loadWalletsFromLocal(DataCallback<List<WalletEntity>> callback) {
        walletLocalRepository.getAll(new WalletRepository.Callback() {
            @Override
            public void onResult(List<WalletEntity> list) {
                if (callback != null) {
                    callback.onDataLoaded(list);
                }
            }
        });
    }
    
    /**
     * ✅ Load transactions từ local Room database (không sync) - với pagination
     */
    public void loadTransactionsFromLocal(String walletId, String walletName, Long startDate, Long endDate, int limit, int offset, DataCallback<List<TransactionEntity>> callback) {
        final String finalWalletName = walletName;
        final Long finalStartDate = startDate;
        final Long finalEndDate = endDate;

        if (finalWalletName != null) {
            transactionLocalRepository.getByWalletNameAndDateRange(finalWalletName, finalStartDate, finalEndDate, limit, offset, new TransactionRepository.OnResultCallback<List<TransactionEntity>>() {
                @Override
                public void onResult(List<TransactionEntity> data) {
                    if (callback != null) {
                        callback.onDataLoaded(data != null ? data : new java.util.ArrayList<>());
                    }
                }
            });
        } else {
            transactionLocalRepository.getByDateRange(finalStartDate, finalEndDate, limit, offset, new TransactionRepository.OnResultCallback<List<TransactionEntity>>() {
                @Override
                public void onResult(List<TransactionEntity> data) {
                    if (callback != null) {
                        callback.onDataLoaded(data != null ? data : new java.util.ArrayList<>());
                    }
                }
            });
        }
    }
    
    /**
     * ✅ Tối ưu: Load aggregate data theo tháng cho chart
     * Chỉ trả về tối đa 6 rows (6 tháng) thay vì hàng nghìn transactions
     */
    public void loadMonthlyAggregateForChart(DataCallback<List<MonthlyAggregate>> callback) {
        // ✅ Tính date range cho 6 tháng gần nhất
        // ✅ Bao gồm cả tháng hiện tại và 5 tháng trước đó = 6 tháng
        java.util.Calendar cal = java.util.Calendar.getInstance();
        
        // ✅ End date: Cuối tháng hiện tại (ngày cuối cùng của tháng)
        cal.set(java.util.Calendar.DAY_OF_MONTH, cal.getActualMaximum(java.util.Calendar.DAY_OF_MONTH));
        cal.set(java.util.Calendar.HOUR_OF_DAY, 23);
        cal.set(java.util.Calendar.MINUTE, 59);
        cal.set(java.util.Calendar.SECOND, 59);
        cal.set(java.util.Calendar.MILLISECOND, 999);
        long endDate = cal.getTimeInMillis();
        
        // ✅ Start date: Đầu tháng của 6 tháng trước (ngày 1 của tháng đó)
        cal = java.util.Calendar.getInstance();
        cal.add(java.util.Calendar.MONTH, -5); // -5 để có 6 tháng: -5, -4, -3, -2, -1, 0 (hiện tại)
        cal.set(java.util.Calendar.DAY_OF_MONTH, 1); // Ngày đầu tiên của tháng
        cal.set(java.util.Calendar.HOUR_OF_DAY, 0);
        cal.set(java.util.Calendar.MINUTE, 0);
        cal.set(java.util.Calendar.SECOND, 0);
        cal.set(java.util.Calendar.MILLISECOND, 0);
        long startDate = cal.getTimeInMillis();
        
        // ✅ Log để debug
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault());
        android.util.Log.d("HomeRepository", "loadMonthlyAggregateForChart: startDate=" + sdf.format(new java.util.Date(startDate)) + ", endDate=" + sdf.format(new java.util.Date(endDate)));
        
        // ✅ Load aggregate data (chỉ 6 rows thay vì hàng nghìn transactions)
        transactionLocalRepository.getMonthlyAggregate(startDate, endDate, new TransactionRepository.OnResultCallback<List<MonthlyAggregate>>() {
            @Override
            public void onResult(List<MonthlyAggregate> data) {
                android.util.Log.d("HomeRepository", "Aggregate data loaded: " + (data != null ? data.size() : 0) + " months");
                if (data != null && !data.isEmpty()) {
                    for (MonthlyAggregate agg : data) {
                        android.util.Log.d("HomeRepository", "  - " + agg.month + ": Income=" + agg.totalIncome + ", Expense=" + agg.totalExpense);
                    }
                }
                if (callback != null) {
                    callback.onDataLoaded(data != null ? data : new java.util.ArrayList<>());
                }
            }
        });
    }
    
    /**
     * ✅ Load transactions từ local Room database (không sync) - không pagination (backward compatible)
     */
    public void loadTransactionsFromLocal(String walletId, String walletName, Long startDate, Long endDate, DataCallback<List<TransactionEntity>> callback) {
        loadTransactionsFromLocal(walletId, walletName, startDate, endDate, 500, 0, callback); // Default limit 500
    }

    // Offline-first: Đọc local trước, chỉ sync khi có mạng
    public void fetchWallets(DataCallback<List<WalletEntity>> callback) {
        // 1) Luôn load local trước để UI có gì đó hiển thị (offline vẫn chạy được)
        walletLocalRepository.getAll(new WalletRepository.Callback() {
            @Override
            public void onResult(List<WalletEntity> list) {
                callback.onDataLoaded(list);
            }
        });

        // 2) Chỉ gọi BE nếu có mạng để sync dữ liệu mới nhất về local
        if (!networkChecker.isNetworkAvailable()) {
            // Không có mạng → chỉ dùng data local đã load ở bước 1
            return;
        }

        walletRemoteRepository.fetchMyWallets(new ApiCallback<List<WalletResponse>>() {
            @Override
            public void onSuccess(List<WalletResponse> body) {
                // Map WalletResponse -> WalletEntity
                // ✅ Backend đã filter deleted=false, nên chỉ lưu wallets chưa bị xóa
                List<WalletEntity> mapped = new java.util.ArrayList<>();
                if (body != null) {
                    for (WalletResponse w : body) {
                        // ✅ Chỉ lưu wallets chưa bị xóa (backend đã filter, nhưng double-check để an toàn)
                        if (w.isDeleted()) {
                            continue; // Bỏ qua wallets đã bị xóa
                        }
                        
                        // ✅ Chỉ lưu wallets có id (UUID từ backend)
                        // Bỏ qua nếu không có ID (không nên xảy ra, nhưng để an toàn)
                        if (w.getId() == null || w.getId().isEmpty()) {
                            continue;
                        }
                        
                        // ✅ Nếu backend chưa trả về currentBalance, dùng initialBalance làm fallback
                        double currentBalance = w.getCurrentBalance() != 0.0 ? w.getCurrentBalance() : w.getInitialBalance();
                        double initialBalance = w.getInitialBalance();
                        
                        String formattedBalance = String.format(
                                "%,.0f %s",
                                currentBalance, // ✅ Dùng currentBalance (hoặc initialBalance nếu chưa có)
                                w.getCurrency() != null ? w.getCurrency() : ""
                        );

                        WalletEntity entity = new WalletEntity(
                                w.getId(), // ✅ Lưu ID từ backend (UUID)
                                w.getName(),
                                formattedBalance,
                                currentBalance, // ✅ Lưu currentBalance
                                initialBalance, // ✅ Lưu initialBalance
                                0 // iconRes tạm thời = 0, sau này anh map theo type
                        );
                        mapped.add(entity);
                    }
                }

                // 2.1) ✅ Upsert vào local cache (insert nếu mới, update nếu đã có)
                // ✅ An toàn: Không xóa wallets cũ, chỉ cập nhật/thêm mới
                // ✅ Giữ nguyên wallets tạo local (nếu có id khác với danh sách từ server)
                walletLocalRepository.upsertAll(mapped);

                // 2.2) Load lại từ local để đảm bảo có đầy đủ dữ liệu (bao gồm cả wallets tạo local)
                walletLocalRepository.getAll(new WalletRepository.Callback() {
                    @Override
                    public void onResult(List<WalletEntity> list) {
                        callback.onDataLoaded(list);
                    }
                });
            }

            @Override
            public void onError(String message) {
                // BE lỗi (mất mạng, 401 refresh fail, server down...)
                // -> cứ để UI dùng data local đã load ở bước 1
                // optional: callback.onError(message);
            }
        });
    }



    public void fetchTransactions(String walletId, String walletName, Long startDate, Long endDate, DataCallback<List<TransactionEntity>> callback) {
        // ✅ walletName được truyền từ HomeActivity (từ wallets đã có, không cần query DB)
        final String finalWalletName = walletName;
        final Long finalStartDate = startDate;
        final Long finalEndDate = endDate;

        android.util.Log.d("HomeRepository", "fetchTransactions called: walletId=" + walletId + ", walletName=" + walletName + ", startDate=" + startDate + ", endDate=" + endDate);

        // 1) Luôn load local trước → offline vẫn xem được
        if (finalWalletName != null) {
            // ✅ Filter theo walletName và time
            android.util.Log.d("HomeRepository", "Loading transactions for wallet: " + finalWalletName);
            transactionLocalRepository.getByWalletNameAndDateRange(finalWalletName, finalStartDate, finalEndDate, new TransactionRepository.OnResultCallback<List<TransactionEntity>>() {
                @Override
                public void onResult(List<TransactionEntity> data) {
                    android.util.Log.d("HomeRepository", "Local transactions loaded: " + (data != null ? data.size() : 0) + " items");
                    if (callback != null) {
                        callback.onDataLoaded(data != null ? data : new java.util.ArrayList<>());
                    }
                }
            });
        } else {
            // ✅ Tất cả ví, filter theo time
            android.util.Log.d("HomeRepository", "Loading transactions for all wallets");
            transactionLocalRepository.getByDateRange(finalStartDate, finalEndDate, new TransactionRepository.OnResultCallback<List<TransactionEntity>>() {
                @Override
                public void onResult(List<TransactionEntity> data) {
                    android.util.Log.d("HomeRepository", "Local transactions loaded: " + (data != null ? data.size() : 0) + " items");
                    if (callback != null) {
                        callback.onDataLoaded(data != null ? data : new java.util.ArrayList<>());
                    }
                }
            });
        }

        // 2) Chỉ gọi BE nếu có mạng để sync dữ liệu mới nhất về local
        if (!networkChecker.isNetworkAvailable()) {
            // Không có mạng → chỉ dùng data local đã load ở bước 1
            return;
        }

        // ✅ Sync từ backend cho cả "Tất cả ví" và ví cụ thể
        // Khi chọn ví cụ thể, vẫn sync để cập nhật dữ liệu mới nhất của ví đó
        // (API sẽ filter theo walletId, và upsertAll sẽ chỉ cập nhật transactions có remoteId, không xóa transactions của ví khác)
        transactionRemoteRepository.fetchTransactions(walletId, new ApiCallback<TransactionPageResponse>() {
            @Override
            public void onSuccess(TransactionPageResponse page) {
                if (page == null || page.getContent() == null) {
                    return;
                }

                // ✅ Lấy danh sách wallets và categories để map
                walletLocalRepository.getAll(new WalletRepository.Callback() {
                    @Override
                    public void onResult(List<WalletEntity> wallets) {
                        List<TransactionEntity> mapped = new java.util.ArrayList<>();
                        for (TransactionResponse t : page.getContent()) {
                            // ✅ Chỉ lưu transactions có remoteId (từ backend)
                            // Bỏ qua nếu không có ID (không nên xảy ra, nhưng để an toàn)
                            if (t.getId() == null || t.getId().isEmpty()) {
                                continue;
                            }
                            
                            // ✅ Tìm walletName từ walletId
                            String walletNameForTransaction = null;
                            if (t.getWalletId() != null && wallets != null) {
                                for (WalletEntity w : wallets) {
                                    if (w.id.equals(t.getWalletId())) {
                                        walletNameForTransaction = w.name;
                                        break;
                                    }
                                }
                            }

                            // ✅ Backend đã trả về categoryName trong TransactionResponse
                            // ✅ Nếu categoryName null, dùng note làm categoryName (vì note thường chứa category name)
                            String categoryName = t.getCategoryName();
                            if (categoryName == null || categoryName.isEmpty()) {
                                // ✅ Fallback: dùng note làm categoryName nếu categoryName null
                                // Note thường chứa category name khi categoryId=null
                                categoryName = (t.getNote() != null && !t.getNote().isEmpty()) ? t.getNote() : "";
                            }

                            // Format số tiền + thời gian → map vào TransactionEntity hiện tại
                            double amountValue = t.getAmount() != null ? t.getAmount().doubleValue() : 0.0;
                            String amountFormatted = String.format(
                                    "%,.0f",
                                    amountValue
                            );

                            // ✅ Format occurredAt từ Instant (backend) hoặc String
                            // ✅ Convert ISO format (2025-12-15T00:00:00Z) sang yyyy-MM-dd để query đúng
                            String dateDisplay = "";
                            if (t.getOccurredAt() != null) {
                                try {
                                    // Parse ISO format: "2025-12-15T00:00:00Z" hoặc "2025-12-15T00:00:00"
                                    String occurredAtStr = t.getOccurredAt();
                                    // Extract date part (yyyy-MM-dd) từ ISO string
                                    if (occurredAtStr.length() >= 10) {
                                        dateDisplay = occurredAtStr.substring(0, 10); // "2025-12-15"
                                    } else {
                                        dateDisplay = occurredAtStr; // Fallback nếu format không đúng
                                    }
                                } catch (Exception e) {
                                    android.util.Log.e("HomeRepository", "Error parsing occurredAt: " + t.getOccurredAt(), e);
                                    dateDisplay = t.getOccurredAt(); // Fallback
                                }
                            }

                            // Ở HomeRepository cũ anh đang dùng ctor:
                            // new TransactionEntity(title, category, amountText, walletName, dateText)
                            // ✅ Title = note (nếu có) hoặc categoryName (nếu note null)
                            String title = (t.getNote() != null && !t.getNote().isEmpty())
                                    ? t.getNote()
                                    : (categoryName.isEmpty() ? "" : categoryName);

                            // ✅ Lưu type và amountDouble để tính toán
                            // ✅ Lưu remoteId từ backend để identify transaction khi upsert
                            TransactionEntity entity = new TransactionEntity(
                                    t.getId(), // ✅ remoteId từ backend
                                    title,
                                    categoryName, // ✅ Dùng category name thực tế, không dùng "Unknown"
                                    amountFormatted,
                                    walletNameForTransaction != null ? walletNameForTransaction : "",
                                    dateDisplay,
                                    t.getType(), // ✅ Lưu type từ backend
                                    amountValue  // ✅ Lưu amount (double) để tính toán
                            );

                            mapped.add(entity);
                        }

                        // 2.1) ✅ Upsert vào local cache (insert nếu mới, update nếu đã có)
                        // ✅ An toàn: Không xóa transactions cũ, chỉ cập nhật/thêm mới
                        // ✅ Giữ nguyên transactions tạo local (không có remoteId) và transactions của ví khác
                        transactionLocalRepository.upsertAll(mapped);

                        // ✅ Update wallet balance cho tất cả wallets có transactions trong danh sách
                        if (wallets != null) {
                            AppDatabase db = AppDatabase.getDatabase(context);
                            Set<String> walletNamesToUpdate = new HashSet<>();
                            
                            // Lấy danh sách wallet names cần update balance
                            for (TransactionEntity tx : mapped) {
                                if (tx.wallet != null && !tx.wallet.isEmpty()) {
                                    walletNamesToUpdate.add(tx.wallet);
                                }
                            }
                            
                            // Update balance cho từng wallet
                            for (String walletName : walletNamesToUpdate) {
                                walletLocalRepository.updateWalletBalance(walletName, db.transactionDao());
                            }
                        }
                        
                        // 2.2) Load lại từ local để callback với dữ liệu mới nhất
                        if (finalWalletName != null) {
                            transactionLocalRepository.getByWalletNameAndDateRange(finalWalletName, finalStartDate, finalEndDate, new TransactionRepository.OnResultCallback<List<TransactionEntity>>() {
                                @Override
                                public void onResult(List<TransactionEntity> data) {
                                    android.util.Log.d("HomeRepository", "Transactions reloaded after sync: " + (data != null ? data.size() : 0) + " items");
                                    if (callback != null) {
                                        callback.onDataLoaded(data != null ? data : new java.util.ArrayList<>());
                                    }
                                }
                            });
                        } else {
                            transactionLocalRepository.getByDateRange(finalStartDate, finalEndDate, new TransactionRepository.OnResultCallback<List<TransactionEntity>>() {
                                @Override
                                public void onResult(List<TransactionEntity> data) {
                                    android.util.Log.d("HomeRepository", "Transactions reloaded after sync: " + (data != null ? data.size() : 0) + " items");
                                    if (callback != null) {
                                        callback.onDataLoaded(data != null ? data : new java.util.ArrayList<>());
                                    }
                                }
                            });
                        }
                    }
                });
            }

            @Override
            public void onError(String message) {
                // BE lỗi (mất mạng, refresh fail, server down...)
                // → cứ để UI dùng data local đã load ở bước 1
                // optional: callback.onError(message);
            }
        });
    }

    public Completable deleteTransaction(long localId) {
        return Completable.fromAction(() -> transactionLocalRepository.deleteByLocalId(localId));
    }
    
    /**
     * ✅ Recursive method để fetch tất cả pages của transactions
     * Chỉ lưu transactions trong 6 tháng gần nhất để tối ưu
     */
    private void fetchAllTransactionsRecursive(String walletId, int pageNumber, long startDate, long endDate, 
                                               List<WalletEntity> wallets, SyncCallback callback) {
        transactionRemoteRepository.fetchTransactions(walletId, pageNumber, 20, new ApiCallback<TransactionPageResponse>() {
            @Override
            public void onSuccess(TransactionPageResponse page) {
                if (page == null || page.getContent() == null || page.getContent().isEmpty()) {
                    // Không còn data → hoàn thành
                    android.util.Log.d("HomeRepository", "All transactions synced (no more pages)");
                    if (callback != null) {
                        callback.onSyncComplete();
                    }
                    return;
                }
                
                // ✅ Map và filter transactions trong 6 tháng gần nhất
                List<TransactionEntity> mapped = new java.util.ArrayList<>();
                java.text.SimpleDateFormat dateFormat = new java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault());
                String startDateStr = dateFormat.format(new java.util.Date(startDate));
                String endDateStr = dateFormat.format(new java.util.Date(endDate));
                
                for (TransactionResponse t : page.getContent()) {
                    if (t.getId() == null || t.getId().isEmpty()) {
                        continue;
                    }
                    
                    // ✅ Check date: chỉ lưu transactions trong 6 tháng gần nhất
                    String dateDisplay = "";
                    if (t.getOccurredAt() != null) {
                        try {
                            String occurredAtStr = t.getOccurredAt();
                            if (occurredAtStr.length() >= 10) {
                                dateDisplay = occurredAtStr.substring(0, 10); // "yyyy-MM-dd"
                            } else {
                                dateDisplay = occurredAtStr;
                            }
                        } catch (Exception e) {
                            dateDisplay = t.getOccurredAt();
                        }
                    }
                    
                    // ✅ Filter: chỉ lưu transactions trong date range
                    if (!dateDisplay.isEmpty() && (dateDisplay.compareTo(startDateStr) < 0 || dateDisplay.compareTo(endDateStr) > 0)) {
                        continue; // Bỏ qua transactions ngoài 6 tháng gần nhất
                    }
                    
                    String walletNameForTransaction = null;
                    if (t.getWalletId() != null && wallets != null) {
                        for (WalletEntity w : wallets) {
                            if (w.id.equals(t.getWalletId())) {
                                walletNameForTransaction = w.name;
                                break;
                            }
                        }
                    }

                    // ✅ Nếu categoryName null, dùng note làm categoryName (vì note thường chứa category name)
                    String categoryName = t.getCategoryName();
                    if (categoryName == null || categoryName.isEmpty()) {
                        // ✅ Fallback: dùng note làm categoryName nếu categoryName null
                        categoryName = (t.getNote() != null && !t.getNote().isEmpty()) ? t.getNote() : "";
                    }
                    
                    double amountValue = t.getAmount() != null ? t.getAmount().doubleValue() : 0.0;
                    String amountFormatted = String.format("%,.0f", amountValue);

                    // ✅ Title = note (nếu có) hoặc categoryName (nếu note null)
                    String title = (t.getNote() != null && !t.getNote().isEmpty())
                            ? t.getNote()
                            : (categoryName.isEmpty() ? "" : categoryName);

                    TransactionEntity entity = new TransactionEntity(
                            t.getId(),
                            title,
                            categoryName,
                            amountFormatted,
                            walletNameForTransaction != null ? walletNameForTransaction : "",
                            dateDisplay,
                            t.getType(),
                            amountValue
                    );

                    mapped.add(entity);
                }
                
                // ✅ Upsert transactions vào local DB
                if (!mapped.isEmpty()) {
                    transactionLocalRepository.upsertAll(mapped);
                    android.util.Log.d("HomeRepository", "Transactions synced (page " + pageNumber + "): " + mapped.size() + " items (filtered from " + page.getContent().size() + " items)");
                }
                
                // ✅ Check xem còn pages không
                // ✅ Fetch tất cả pages để đảm bảo có đủ data cho 6 tháng gần nhất
                // ✅ Backend có thể sort theo createdAt DESC, nên các transactions mới nhất ở đầu
                // ✅ Các transactions của tháng 7-11 có thể nằm ở các pages sau
                if (page.isLast() || page.getNumber() >= page.getTotalPages() - 1) {
                    // Đã fetch hết → hoàn thành
                    android.util.Log.d("HomeRepository", "All transactions synced (last page reached, page " + pageNumber + ", totalPages=" + page.getTotalPages() + ")");
                    if (callback != null) {
                        callback.onSyncComplete();
                    }
                } else {
                    // ✅ Fetch page tiếp theo để lấy đủ data cho 6 tháng
                    android.util.Log.d("HomeRepository", "Fetching next page: " + (pageNumber + 1) + " (totalPages=" + page.getTotalPages() + ")");
                    fetchAllTransactionsRecursive(walletId, pageNumber + 1, startDate, endDate, wallets, callback);
                }
            }

            @Override
            public void onError(String message) {
                android.util.Log.e("HomeRepository", "Error syncing transactions (page " + pageNumber + "): " + message);
                if (callback != null) {
                    callback.onSyncError(message);
                }
            }
        });
    }

}
