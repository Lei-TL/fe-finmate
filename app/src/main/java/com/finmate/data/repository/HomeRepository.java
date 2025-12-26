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

    // Offline-first: Đọc local trước, chỉ sync khi có mạng
    public void fetchWallets(DataCallback<List<WalletEntity>> callback) {
        walletLocalRepository.getAll(new WalletRepository.Callback() {
            @Override
            public void onResult(List<WalletEntity> list) {
                callback.onDataLoaded(list);
            }
        });

        if (!networkChecker.isNetworkAvailable()) {
            // Không có mạng → chỉ dùng data local đã load ở bước 1
            return;
        }

        walletRemoteRepository.fetchMyWallets(new ApiCallback<List<WalletResponse>>() {
            @Override
            public void onSuccess(List<WalletResponse> body) {
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

        if (!networkChecker.isNetworkAvailable()) {
            return;
        }

        // ✅ Sync từ backend cho cả "Tất cả ví" và ví cụ thể
        transactionRemoteRepository.fetchTransactions(walletId, new ApiCallback<TransactionPageResponse>() {
            @Override
            public void onSuccess(TransactionPageResponse page) {
                if (page == null || page.getContent() == null) {
                    return;
                }

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
                            String categoryName = t.getCategoryName() != null ? t.getCategoryName() : "";

                            // Format số tiền + thời gian → map vào TransactionEntity hiện tại
                            double amountValue = t.getAmount() != null ? t.getAmount().doubleValue() : 0.0;
                            String amountFormatted = String.format(
                                    "%,.0f",
                                    amountValue
                            );

                            // ✅ Format occurredAt từ Instant (backend) hoặc String
                            String dateDisplay = "";
                            if (t.getOccurredAt() != null) {
                                dateDisplay = t.getOccurredAt(); // Gson sẽ parse Instant thành String ISO format
                            }

                            // Ở HomeRepository cũ anh đang dùng ctor:
                            // new TransactionEntity(title, category, amountText, walletName, dateText)
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

                            transactionLocalRepository.upsertAll(mapped);

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

}
