package com.finmate.data.repository;

import com.finmate.core.network.ApiCallExecutor;
import com.finmate.data.dto.WalletResponse;
import com.finmate.data.remote.api.ApiCallback;
import com.finmate.data.remote.api.WalletService;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import retrofit2.Call;

@Singleton
public class WalletRemoteRepository {

    private final WalletService walletApi;
    private final ApiCallExecutor apiCallExecutor;

    @Inject
    public WalletRemoteRepository(WalletService walletApi, ApiCallExecutor apiCallExecutor) {
        this.walletApi = walletApi;
        this.apiCallExecutor = apiCallExecutor;
    }

    public void fetchMyWallets(ApiCallback<List<WalletResponse>> callback) {
        Call<List<WalletResponse>> call = walletApi.getMyWallets();
        apiCallExecutor.execute(call, callback);
    }
}
