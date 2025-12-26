package com.finmate.data.repository;

import com.finmate.core.network.ApiCallExecutor;
import com.finmate.core.network.ApiCallback;
import com.finmate.data.dto.TransactionPageResponse;
import com.finmate.data.dto.TransactionResponse;
import com.finmate.data.remote.dto.CreateTransactionRequest;

import javax.inject.Inject;
import javax.inject.Singleton;

import retrofit2.Call;

@Singleton
public class TransactionRemoteRepository {

    private final com.finmate.data.remote.api.TransactionService transactionApi;
    private final ApiCallExecutor apiCallExecutor;

    @Inject
    public TransactionRemoteRepository(com.finmate.data.remote.api.TransactionService transactionApi,
                                       ApiCallExecutor apiCallExecutor) {
        this.transactionApi = transactionApi;
        this.apiCallExecutor = apiCallExecutor;
    }

    public void fetchTransactions(String walletId,
                                  ApiCallback<TransactionPageResponse> callback) {
        Call<TransactionPageResponse> call = transactionApi.getTransactions(walletId);
        apiCallExecutor.execute(call, callback);
    }

    public void createTransaction(CreateTransactionRequest request,
                                  ApiCallback<TransactionResponse> callback) {
        Call<TransactionResponse> call = transactionApi.createTransaction(request);
        apiCallExecutor.execute(call, callback);
    }
}
