package com.finmate.data.remote.api;

import com.finmate.data.dto.WalletResponse;
import com.finmate.data.dto.WalletRequest;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.DELETE;
import retrofit2.http.Body;
import retrofit2.http.Path;

public interface WalletService {

    // Gắn baseUrl ở Retrofit, chỗ này chỉ cần path
    @GET("wallets")
    Call<List<WalletResponse>> getMyWallets();

    @GET("wallets/{id}")
    Call<WalletResponse> getWalletById(@Path("id") String id);

    @POST("wallets")
    Call<WalletResponse> createWallet(@Body WalletRequest request);

    @PUT("wallets/{id}")
    Call<WalletResponse> updateWallet(@Path("id") String id, @Body WalletRequest request);

    @DELETE("wallets/{id}")
    Call<Void> deleteWallet(@Path("id") String id);
}
