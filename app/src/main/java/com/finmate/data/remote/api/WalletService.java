package com.finmate.data.remote.api;

import com.finmate.data.dto.CreateWalletRequest;
import com.finmate.data.dto.WalletResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;

public interface WalletService {

    // Gắn baseUrl ở Retrofit, chỗ này chỉ cần path
    @GET("wallets")
    Call<List<WalletResponse>> getMyWallets();

    @POST("wallets")
    Call<WalletResponse> createWallet(@Body CreateWalletRequest request);
}
