package com.finmate.data.repository;

import androidx.annotation.Nullable;

import com.finmate.data.local.database.entity.SyncStatus;
import com.finmate.data.dto.TransactionPageResponse;
import com.finmate.data.dto.TransactionResponse;
import com.finmate.data.dto.WalletResponse;
import com.finmate.data.local.database.entity.TransactionEntity;
import com.finmate.data.local.database.entity.WalletEntity;
import com.finmate.data.remote.api.ApiCallback;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class HomeRepository {

    private final WalletRemoteRepository walletRemoteRepository;
    private final WalletLocalRepository walletLocalRepository;
    private final TransactionRemoteRepository transactionRemoteRepository;
    private final TransactionLocalRepository transactionLocalRepository;

    @Inject
    public HomeRepository(WalletRemoteRepository walletRemoteRepository, WalletLocalRepository walletLocalRepository, TransactionRemoteRepository transactionRemoteRepository, TransactionLocalRepository transactionLocalRepository) {
        this.walletRemoteRepository = walletRemoteRepository;
        this.walletLocalRepository = walletLocalRepository;
        this.transactionRemoteRepository = transactionRemoteRepository;
        this.transactionLocalRepository = transactionLocalRepository;
    }

    public interface DataCallback<T> {
        void onDataLoaded(T data);
        void onError(String message);
    }

    public void fetchWallets(DataCallback<List<WalletEntity>> callback) {
        walletLocalRepository.getAll(new WalletLocalRepository.Callback() {
            @Override
            public void onResult(List<WalletEntity> list) {
                callback.onDataLoaded(list);
            }
        });

        walletRemoteRepository.fetchMyWallets(new ApiCallback<List<WalletResponse>>() {
            @Override
            public void onSuccess(List<WalletResponse> body) {
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
                                0
                        );
                        mapped.add(entity);
                    }
                }
                walletLocalRepository.replaceAll(mapped);
                callback.onDataLoaded(mapped);
            }

            @Override
            public void onError(String message, @Nullable Integer code) {
                // Error from API, do nothing, UI will use local data
            }
        });
    }

    public void fetchTransactions(String walletId, DataCallback<List<TransactionEntity>> callback) {
        transactionLocalRepository.getAll(new TransactionLocalRepository.OnResultCallback<List<TransactionEntity>>() {
            @Override
            public void onResult(List<TransactionEntity> data) {
                callback.onDataLoaded(data);
            }
        });

        transactionRemoteRepository.fetchTransactions(walletId, new ApiCallback<TransactionPageResponse>() {
            @Override
            public void onSuccess(TransactionPageResponse page) {
                if (page == null || page.getContent() == null) {
                    return;
                }

                List<TransactionEntity> mapped = new java.util.ArrayList<>();
                for (TransactionResponse t : page.getContent()) {
                    String title = (t.getNote() != null && !t.getNote().isEmpty()) ? t.getNote() : t.getCategoryName();
                    // Giữ raw data: amount là double, occurredAt là ISO string từ BE
                    TransactionEntity entity = new TransactionEntity(
                            title,
                            t.getCategoryName(),
                            t.getAmount(),
                            t.getWalletName(),
                            t.getOccurredAt()
                    );
                    entity.remoteId = t.getId();
                    entity.syncStatus = SyncStatus.SYNCED;
                    entity.pendingAction = "NONE";
                    mapped.add(entity);
                }

                transactionLocalRepository.replaceAllSynced(mapped);
                callback.onDataLoaded(mapped);
            }

            @Override
            public void onError(String message, @Nullable Integer code) {
                // Error from API, do nothing, UI will use local data
            }
        });
    }
}
