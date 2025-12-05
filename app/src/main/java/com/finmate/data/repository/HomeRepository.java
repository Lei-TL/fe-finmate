package com.finmate.data.repository;

import com.finmate.core.network.ApiCallback;
import com.finmate.data.dto.TransactionPageResponse;
import com.finmate.data.dto.TransactionResponse;
import com.finmate.data.dto.WalletResponse;
import com.finmate.entities.TransactionEntity;
import com.finmate.entities.WalletEntity;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class HomeRepository {

    private final WalletRemoteRepository walletRemoteRepository;
    private final WalletRepository walletLocalRepository;
    private final TransactionRemoteRepository transactionRemoteRepository;
    private final TransactionRepository transactionLocalRepository;

    @Inject
    public HomeRepository(WalletRemoteRepository walletRemoteRepository, WalletRepository walletLocalRepository, TransactionRemoteRepository transactionRemoteRepository, TransactionRepository transactionLocalRepository) {
        this.walletRemoteRepository = walletRemoteRepository;
        this.walletLocalRepository = walletLocalRepository;
        this.transactionRemoteRepository = transactionRemoteRepository;
        this.transactionLocalRepository = transactionLocalRepository;
    }

    public interface DataCallback<T> {
        void onDataLoaded(T data);
        void onError(String message);
    }

    // Tạm thời dùng dữ liệu giả
    public void fetchWallets(DataCallback<List<WalletEntity>> callback) {
        // 1) Luôn load local trước để UI có gì đó hiển thị (offline vẫn chạy được)
        walletLocalRepository.getAll(new WalletRepository.Callback() {
            @Override
            public void onResult(List<WalletEntity> list) {
                callback.onDataLoaded(list);
            }
        });

        // 2) Sau đó, thử gọi BE để lấy dữ liệu mới nhất
        walletRemoteRepository.fetchMyWallets(new ApiCallback<List<WalletResponse>>() {
            @Override
            public void onSuccess(List<WalletResponse> body) {
                // Map WalletResponse -> WalletEntity
                List<WalletEntity> mapped = new java.util.ArrayList<>();
                if (body != null) {
                    for (WalletResponse w : body) {
                        String formattedBalance = String.format(
                                "%,.0f %s",
                                w.getCurrentBalance(),
                                w.getCurrency() != null ? w.getCurrency() : ""
                        );

                        WalletEntity entity = new WalletEntity(
                                w.getName(),
                                formattedBalance,
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



    public void fetchTransactions(String walletId, DataCallback<List<TransactionEntity>> callback) {
        // 1) Luôn load local trước → offline vẫn xem được
        transactionLocalRepository.getAll(new TransactionRepository.OnResultCallback<List<TransactionEntity>>() {
            @Override
            public void onResult(List<TransactionEntity> data) {
                // Nếu anh muốn filter theo walletId ở local luôn thì filter thêm ở đây
                callback.onDataLoaded(data);
            }
        });

        // 2) Gọi BE để lấy dữ liệu mới nhất nếu có mạng
        transactionRemoteRepository.fetchTransactions(walletId, new ApiCallback<TransactionPageResponse>() {
            @Override
            public void onSuccess(TransactionPageResponse page) {
                if (page == null || page.getContent() == null) {
                    return;
                }

                List<TransactionEntity> mapped = new java.util.ArrayList<>();
                for (TransactionResponse t : page.getContent()) {
                    // Format số tiền + thời gian → map vào TransactionEntity hiện tại
                    String amountFormatted = String.format(
                            "%,.0f",
                            t.getAmount()
                    );

                    // TODO: format occurredAt (ISO) thành "dd/MM/yyyy" nếu muốn
                    String dateDisplay = t.getOccurredAt(); // tạm thời dùng raw string

                    // Ở HomeRepository cũ anh đang dùng ctor:
                    // new TransactionEntity(title, category, amountText, walletName, dateText)
                    String title = (t.getNote() != null && !t.getNote().isEmpty())
                            ? t.getNote()
                            : t.getCategoryName();

                    TransactionEntity entity = new TransactionEntity(
                            title,
                            t.getCategoryName(),
                            amountFormatted,
                            t.getWalletName(),
                            dateDisplay
                    );

                    mapped.add(entity);
                }

                // 2.1) Ghi đè vào local cache
                transactionLocalRepository.replaceAll(mapped);

                // 2.2) Đẩy list mới lên UI
                callback.onDataLoaded(mapped);
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
