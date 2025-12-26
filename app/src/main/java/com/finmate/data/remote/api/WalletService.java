package com.finmate.data.remote.api;

import com.finmate.data.dto.WalletResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;

public interface WalletService {

    // Gắn baseUrl ở Retrofit, chỗ này chỉ cần path
    @GET("wallets")
    Call<List<WalletResponse>> getMyWallets();

    // Sau này có thể thêm: create/update/delete nếu cần
}
