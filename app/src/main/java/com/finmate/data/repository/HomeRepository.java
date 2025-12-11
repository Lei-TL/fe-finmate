package com.finmate.data.repository;

import androidx.annotation.Nullable;

import com.finmate.core.offline.PendingAction;
import com.finmate.core.offline.SyncStatus;
import com.finmate.data.dto.TransactionPageResponse;
import com.finmate.data.dto.TransactionResponse;
import com.finmate.data.dto.WalletResponse;
import com.finmate.data.local.database.entity.TransactionEntity;
import com.finmate.data.local.database.entity.WalletEntity;
import com.finmate.data.local.database.entity.CategoryEntity;
import com.finmate.data.remote.api.ApiCallback;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class HomeRepository {

    private final WalletRemoteRepository walletRemoteRepository;
    private final WalletLocalRepository walletLocalRepository;
    private final TransactionRemoteRepository transactionRemoteRepository;
    private final TransactionLocalRepository transactionLocalRepository;
    private final CategoryLocalRepository categoryLocalRepository;

    @Inject
    public HomeRepository(WalletRemoteRepository walletRemoteRepository, 
                          WalletLocalRepository walletLocalRepository, 
                          TransactionRemoteRepository transactionRemoteRepository, 
                          TransactionLocalRepository transactionLocalRepository,
                          CategoryLocalRepository categoryLocalRepository) {
        this.walletRemoteRepository = walletRemoteRepository;
        this.walletLocalRepository = walletLocalRepository;
        this.transactionRemoteRepository = transactionRemoteRepository;
        this.transactionLocalRepository = transactionLocalRepository;
        this.categoryLocalRepository = categoryLocalRepository;
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
                        // Use initialBalance from backend, or calculate currentBalance if needed
                        double balance = w.getInitialBalance() != null ? 
                                w.getInitialBalance().doubleValue() : w.getCurrentBalance();
                        WalletEntity entity = new WalletEntity(
                                w.getName(),
                                w.getCurrency() != null ? w.getCurrency() : "VND",
                                balance,
                                0
                        );
                        entity.setRemoteId(w.getId());
                        entity.setSyncStatus(SyncStatus.SYNCED);
                        entity.setPendingAction(PendingAction.NONE);
                        entity.setUpdatedAt(System.currentTimeMillis());
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

        // Load wallets and categories for mapping IDs to names
        walletLocalRepository.getAll(new WalletLocalRepository.Callback() {
            @Override
            public void onResult(List<WalletEntity> wallets) {
                // Now fetch transactions with wallet and category lookups
                transactionRemoteRepository.fetchTransactions(
                        walletId, null, null, null, null, 0, 20,
                        new ApiCallback<TransactionPageResponse>() {
                            @Override
                            public void onSuccess(TransactionPageResponse page) {
                                if (page == null || page.getContent() == null || page.getContent().isEmpty()) {
                                    callback.onDataLoaded(new java.util.ArrayList<>());
                                    return;
                                }

                                // Get all categories for all transaction types (INCOME, EXPENSE, TRANSFER)
                                // We'll collect all unique types first
                                Set<String> types = new HashSet<>();
                                for (TransactionResponse t : page.getContent()) {
                                    if (t.getType() != null) {
                                        types.add(t.getType());
                                    }
                                }
                                
                                // If no types found, use default
                                if (types.isEmpty()) {
                                    types.add("EXPENSE");
                                }
                                
                                // Collect all categories from all types
                                final List<CategoryEntity> allCategories = new java.util.ArrayList<>();
                                final int[] remainingTypes = {types.size()};
                                
                                for (String type : types) {
                                    categoryLocalRepository.getByTypeSync(type, 
                                        new CategoryLocalRepository.OnResultCallback<List<CategoryEntity>>() {
                                            @Override
                                            public void onResult(List<CategoryEntity> categories) {
                                                if (categories != null) {
                                                    allCategories.addAll(categories);
                                                }
                                                remainingTypes[0]--;
                                                
                                                // When all categories are loaded, map transactions
                                                if (remainingTypes[0] == 0) {
                                                    mapTransactions(page, wallets, allCategories, callback);
                                                }
                                            }
                                        });
                                }
                            }

                            @Override
                            public void onError(String message, @Nullable Integer code) {
                                // Error from API, do nothing, UI will use local data
                            }
                        });
            }
        });
    }
    
    private void mapTransactions(TransactionPageResponse page, 
                                List<WalletEntity> wallets, 
                                List<CategoryEntity> allCategories,
                                DataCallback<List<TransactionEntity>> callback) {
        List<TransactionEntity> mapped = new java.util.ArrayList<>();
        for (TransactionResponse t : page.getContent()) {
            // Look up wallet name
            String walletName = "";
            for (WalletEntity w : wallets) {
                if (w.getRemoteId() != null && w.getRemoteId().equals(t.getWalletId())) {
                    walletName = w.getName();
                    break;
                }
            }
            
            // Look up category name
            String categoryName = "Unknown";
            if (t.getCategoryId() != null && allCategories != null) {
                for (CategoryEntity c : allCategories) {
                    if (c.getRemoteId() != null && c.getRemoteId().equals(t.getCategoryId())) {
                        categoryName = c.getName();
                        break;
                    }
                }
            }
            
            String title = (t.getNote() != null && !t.getNote().isEmpty()) ? t.getNote() : categoryName;
            
            // Convert BigDecimal to double
            double amount = t.getAmount() != null ? t.getAmount().doubleValue() : 0.0;
            
            TransactionEntity entity = new TransactionEntity(
                    t.getId(),
                    title,
                    categoryName,
                    amount,
                    walletName,
                    t.getOccurredAt() != null ? t.getOccurredAt() : "",
                    t.getType() != null ? t.getType() : "EXPENSE"
            );
            entity.setSyncStatus(SyncStatus.SYNCED);
            entity.setPendingAction(PendingAction.NONE);
            entity.setUpdatedAt(System.currentTimeMillis());
            mapped.add(entity);
        }
        
        transactionLocalRepository.replaceAllSynced(mapped);
        callback.onDataLoaded(mapped);
    }
}
