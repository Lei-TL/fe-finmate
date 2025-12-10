package com.finmate.data.repository;

import com.finmate.core.network.ApiCallExecutor;
import com.finmate.data.remote.api.ApiCallback;
import com.finmate.data.remote.api.TransactionService;
import com.finmate.data.remote.dto.CreateTransactionRequest;
import com.finmate.data.dto.TransactionPageResponse;
import com.finmate.data.dto.TransactionResponse;
import com.finmate.data.remote.dto.UpdateTransactionRequest;
import com.finmate.data.local.database.entity.TransactionEntity;

import javax.inject.Inject;
import javax.inject.Singleton;

import retrofit2.Call;

@Singleton
public class TransactionRemoteRepository {

    private final TransactionService transactionApi;
    private final ApiCallExecutor apiCallExecutor;

    @Inject
    public TransactionRemoteRepository(TransactionService transactionApi,
                                       ApiCallExecutor apiCallExecutor) {
        this.transactionApi = transactionApi;
        this.apiCallExecutor = apiCallExecutor;
    }

    public void fetchTransactions(String walletId,
                                  ApiCallback<TransactionPageResponse> callback) {
        Call<TransactionPageResponse> call = transactionApi.getTransactions(walletId);
        apiCallExecutor.execute(call, callback);
    }

    public void createFromLocal(TransactionEntity tx, ApiCallback<TransactionResponse> callback) {
        CreateTransactionRequest request = new CreateTransactionRequest(
                tx.name,
                tx.category,
                tx.amount,  // amount đã là double, không cần parse
                "some-wallet-id", // Placeholder
                tx.occurredAt  // occurredAt là ISO string
        );
        apiCallExecutor.execute(transactionApi.createTransaction(request), callback);
    }

    public void updateFromLocal(TransactionEntity tx, ApiCallback<TransactionResponse> callback) {
        if (tx.remoteId == null) {
            callback.onError("Missing remoteId for update", 400);
            return;
        }
        UpdateTransactionRequest request = new UpdateTransactionRequest(
                tx.name,
                tx.category,
                tx.amount  // amount đã là double, không cần parse
        );
        apiCallExecutor.execute(transactionApi.updateTransaction(tx.remoteId, request), callback);
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
