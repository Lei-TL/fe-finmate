package com.finmate.data.repository;

import com.finmate.core.network.ApiCallExecutor;
import com.finmate.data.remote.api.ApiCallback;
import com.finmate.data.remote.api.TransactionService;
import com.finmate.data.remote.dto.CreateTransactionRequest;
import com.finmate.data.dto.TransactionPageResponse;
import com.finmate.data.dto.TransactionResponse;
import com.finmate.data.remote.dto.UpdateTransactionRequest;
import com.finmate.data.local.database.entity.TransactionEntity;
import com.finmate.data.local.database.entity.WalletEntity;
import com.finmate.data.local.database.entity.CategoryEntity;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.Executors;

import javax.inject.Inject;
import javax.inject.Singleton;

import retrofit2.Call;

@Singleton
public class TransactionRemoteRepository {

    private final TransactionService transactionApi;
    private final ApiCallExecutor apiCallExecutor;
    private final WalletLocalRepository walletLocalRepository;
    private final CategoryLocalRepository categoryLocalRepository;

    @Inject
    public TransactionRemoteRepository(TransactionService transactionApi,
                                       ApiCallExecutor apiCallExecutor,
                                       WalletLocalRepository walletLocalRepository,
                                       CategoryLocalRepository categoryLocalRepository) {
        this.transactionApi = transactionApi;
        this.apiCallExecutor = apiCallExecutor;
        this.walletLocalRepository = walletLocalRepository;
        this.categoryLocalRepository = categoryLocalRepository;
    }

    public void fetchTransactions(String walletId,
                                  String from,
                                  String to,
                                  String categoryId,
                                  String type,
                                  int page,
                                  int size,
                                  ApiCallback<TransactionPageResponse> callback) {
        Call<TransactionPageResponse> call = transactionApi.getTransactions(
                from, to, walletId, categoryId, type, page, size
        );
        apiCallExecutor.execute(call, callback);
    }

    public void createFromLocal(TransactionEntity tx, ApiCallback<TransactionResponse> callback) {
        // Look up wallet and category IDs by name
        walletLocalRepository.getAll(new WalletLocalRepository.Callback() {
            @Override
            public void onResult(List<WalletEntity> wallets) {
                final String[] walletId = {null};
                final String[] currency = {"VND"}; // default
                for (WalletEntity w : wallets) {
                    if (w.getName().equals(tx.walletName)) {
                        if (w.getRemoteId() != null) {
                            walletId[0] = w.getRemoteId();
                        }
                        currency[0] = w.getCurrency();
                        break;
                    }
                }
                if (walletId[0] == null) {
                    callback.onError("Wallet not found or not synced: " + tx.walletName, 400);
                    return;
                }

                // Look up category synchronously
                categoryLocalRepository.getByTypeSync(tx.type != null ? tx.type : "EXPENSE", 
                    new CategoryLocalRepository.OnResultCallback<List<CategoryEntity>>() {
                        @Override
                        public void onResult(List<CategoryEntity> categories) {
                            if (categories == null || categories.isEmpty()) {
                                callback.onError("Category not found: " + tx.categoryName, 400);
                                return;
                            }
                            String categoryId = null;
                            for (CategoryEntity c : categories) {
                                if (c.getName().equals(tx.categoryName) && c.getRemoteId() != null) {
                                    categoryId = c.getRemoteId();
                                    break;
                                }
                            }
                            if (categoryId == null) {
                                callback.onError("Category not found or not synced: " + tx.categoryName, 400);
                                return;
                            }

                            CreateTransactionRequest request = new CreateTransactionRequest(
                                    walletId[0],
                                    categoryId,
                                    tx.type != null ? tx.type : "EXPENSE",
                                    BigDecimal.valueOf(tx.amount),
                                    currency[0],
                                    tx.occurredAt,
                                    tx.name, // use name as note
                                    null // transferRefId
                            );
                            apiCallExecutor.execute(transactionApi.createTransaction(request), callback);
                        }
                    });
            }
        });
    }

    public void updateFromLocal(TransactionEntity tx, ApiCallback<TransactionResponse> callback) {
        if (tx.remoteId == null) {
            callback.onError("Missing remoteId for update", 400);
            return;
        }

        // Look up wallet and category IDs by name
        walletLocalRepository.getAll(new WalletLocalRepository.Callback() {
            @Override
            public void onResult(List<WalletEntity> wallets) {
                final String[] walletId = {null};
                final String[] currency = {"VND"};
                for (WalletEntity w : wallets) {
                    if (w.getName().equals(tx.walletName)) {
                        if (w.getRemoteId() != null) {
                            walletId[0] = w.getRemoteId();
                        }
                        currency[0] = w.getCurrency();
                        break;
                    }
                }
                if (walletId[0] == null) {
                    callback.onError("Wallet not found or not synced: " + tx.walletName, 400);
                    return;
                }

                // Look up category synchronously
                categoryLocalRepository.getByTypeSync(tx.type != null ? tx.type : "EXPENSE",
                    new CategoryLocalRepository.OnResultCallback<List<CategoryEntity>>() {
                        @Override
                        public void onResult(List<CategoryEntity> categories) {
                            if (categories == null || categories.isEmpty()) {
                                callback.onError("Category not found: " + tx.categoryName, 400);
                                return;
                            }
                            String categoryId = null;
                            for (CategoryEntity c : categories) {
                                if (c.getName().equals(tx.categoryName) && c.getRemoteId() != null) {
                                    categoryId = c.getRemoteId();
                                    break;
                                }
                            }
                            if (categoryId == null) {
                                callback.onError("Category not found or not synced: " + tx.categoryName, 400);
                                return;
                            }

                            UpdateTransactionRequest request = new UpdateTransactionRequest(
                                    walletId[0],
                                    categoryId,
                                    tx.type != null ? tx.type : "EXPENSE",
                                    BigDecimal.valueOf(tx.amount),
                                    currency[0],
                                    tx.occurredAt,
                                    tx.name, // use name as note
                                    null // transferRefId
                            );
                            apiCallExecutor.execute(transactionApi.updateTransaction(tx.remoteId, request), callback);
                        }
                    });
            }
        });
    }

    public void deleteFromLocal(TransactionEntity tx, ApiCallback<Void> callback) {
        if (tx.remoteId == null) {
            // If remoteId is null, it's a pending create, just delete it locally
            callback.onSuccess(null);
            return;
        }
        apiCallExecutor.execute(transactionApi.deleteTransaction(tx.remoteId), callback);
    }
}
