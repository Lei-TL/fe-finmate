package com.finmate.data.remote.api;

import com.finmate.data.dto.TransactionPageResponse;
import com.finmate.data.dto.TransactionResponse;
import com.finmate.data.remote.dto.CreateTransactionRequest;
import com.finmate.data.remote.dto.UpdateTransactionRequest;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;

public interface TransactionService {

    @GET("transaction/byWallet/{walletId}")
    Call<TransactionPageResponse> getTransactions(@Path("walletId") String walletId);

    @POST("transaction")
    Call<TransactionResponse> createTransaction(@Body CreateTransactionRequest request);

    @PUT("transaction/{id}")
    Call<TransactionResponse> updateTransaction(@Path("id") String id, @Body UpdateTransactionRequest request);

    @DELETE("transaction/{id}")
    Call<Void> deleteTransaction(@Path("id") String id);
}
