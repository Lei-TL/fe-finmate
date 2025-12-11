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
import retrofit2.http.Query;

public interface TransactionService {

    @GET("transactions")
    Call<TransactionPageResponse> getTransactions(
            @Query("from") String from,
            @Query("to") String to,
            @Query("walletId") String walletId,
            @Query("categoryId") String categoryId,
            @Query("type") String type,
            @Query("page") int page,
            @Query("size") int size
    );

    @GET("transactions/{id}")
    Call<TransactionResponse> getTransactionById(@Path("id") String id);

    @POST("transactions")
    Call<TransactionResponse> createTransaction(@Body CreateTransactionRequest request);

    @PUT("transactions/{id}")
    Call<TransactionResponse> updateTransaction(@Path("id") String id, @Body UpdateTransactionRequest request);

    @DELETE("transactions/{id}")
    Call<Void> deleteTransaction(@Path("id") String id);
}
