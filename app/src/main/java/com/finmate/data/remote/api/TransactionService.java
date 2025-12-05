package com.finmate.data.remote.api;

import com.finmate.data.dto.TransactionPageResponse;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface TransactionService {

    /**
     * Gọi tới GET /transactions bên BE.
     * Anh chỉnh lại query param cho khớp với TransactionController (walletId, from, to...).
     */
    @GET("transactions")
    Call<TransactionPageResponse> getTransactions(
            @Query("walletId") String walletId
            // có thể thêm @Query("from") String from, @Query("to") String to nếu cần
    );
}
