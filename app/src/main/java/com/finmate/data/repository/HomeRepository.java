package com.finmate.data.repository;

import com.finmate.core.network.ApiCallback;
import com.finmate.core.network.NetworkChecker;
import com.finmate.data.dto.TransactionPageResponse;
import com.finmate.data.dto.TransactionResponse;
import com.finmate.data.dto.WalletResponse;
import com.finmate.data.local.database.entity.TransactionEntity;
import com.finmate.data.local.database.entity.WalletEntity;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class HomeRepository {

    private final WalletRemoteRepository walletRemoteRepository;
    private final WalletRepository walletLocalRepository;
    private final TransactionRemoteRepository transactionRemoteRepository;
    private final TransactionRepository transactionLocalRepository;
    private final CategoryRepository categoryRepository; // ✅ Thêm để map categoryId -> categoryName
    private final NetworkChecker networkChecker;

    @Inject
    public HomeRepository(WalletRemoteRepository walletRemoteRepository, 
                         WalletRepository walletLocalRepository, 
                         TransactionRemoteRepository transactionRemoteRepository, 
                         TransactionRepository transactionLocalRepository,
                         CategoryRepository categoryRepository,
                         NetworkChecker networkChecker) {
        this.walletRemoteRepository = walletRemoteRepository;
        this.walletLocalRepository = walletLocalRepository;
        this.transactionRemoteRepository = transactionRemoteRepository;
        this.transactionLocalRepository = transactionLocalRepository;
        this.categoryRepository = categoryRepository;
        this.networkChecker = networkChecker;
    }

    public interface DataCallback<T> {
        void onDataLoaded(T data);
        void onError(String message);
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

                // 2.1) Ghi đè vào local cache
                walletLocalRepository.replaceAll(mapped);

                // 2.2) Đẩy list mới lên UI
                callback.onDataLoaded(mapped);
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

        // 1) Luôn load local trước → offline vẫn xem được
        if (finalWalletName != null) {
            // ✅ Filter theo walletName và time
            transactionLocalRepository.getByWalletNameAndDateRange(finalWalletName, finalStartDate, finalEndDate, new TransactionRepository.OnResultCallback<List<TransactionEntity>>() {
                @Override
                public void onResult(List<TransactionEntity> data) {
                    callback.onDataLoaded(data);
                }
            });
        } else {
            // ✅ Tất cả ví, filter theo time
            transactionLocalRepository.getByDateRange(finalStartDate, finalEndDate, new TransactionRepository.OnResultCallback<List<TransactionEntity>>() {
                @Override
                public void onResult(List<TransactionEntity> data) {
                    callback.onDataLoaded(data);
                }
            });
        }

        // 2) Chỉ gọi BE nếu có mạng để sync dữ liệu mới nhất về local
        if (!networkChecker.isNetworkAvailable()) {
            // Không có mạng → chỉ dùng data local đã load ở bước 1
            return;
        }

        // ✅ Chỉ sync từ backend khi chọn "Tất cả ví" (walletId == null)
        // Khi chọn ví cụ thể, chỉ filter ở local để không mất transactions của ví khác
        if (walletId != null) {
            // Chọn ví cụ thể → chỉ dùng data local đã filter ở bước 1, không sync từ backend
            return;
        }

        // ✅ Chỉ khi chọn "Tất cả ví" mới sync từ backend
        transactionRemoteRepository.fetchTransactions(null, new ApiCallback<TransactionPageResponse>() {
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
                                TransactionEntity entity = new TransactionEntity(
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

                            // 2.1) Ghi đè vào local cache (chỉ khi sync tất cả ví)
                            transactionLocalRepository.replaceAll(mapped);

                            // 2.2) Đẩy list mới lên UI (filter theo walletName nếu cần)
                            if (finalWalletName != null) {
                                // Filter lại theo walletName
                                List<TransactionEntity> filtered = new java.util.ArrayList<>();
                                for (TransactionEntity e : mapped) {
                                    if (e.wallet != null && e.wallet.equals(finalWalletName)) {
                                        filtered.add(e);
                                    }
                                }
                                callback.onDataLoaded(filtered);
                            } else {
                                callback.onDataLoaded(mapped);
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

}
