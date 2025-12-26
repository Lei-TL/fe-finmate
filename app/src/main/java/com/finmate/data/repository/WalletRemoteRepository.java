package com.finmate.data.repository;

import com.finmate.core.network.ApiCallExecutor;
import com.finmate.core.network.ApiCallback;
import com.finmate.data.dto.WalletResponse;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import retrofit2.Call;

@Singleton
public class WalletRemoteRepository {

    private final com.finmate.data.remote.api.WalletService walletApi;
    private final ApiCallExecutor apiCallExecutor;

    @Inject
    public WalletRemoteRepository(com.finmate.data.remote.api.WalletService walletApi,
                                  ApiCallExecutor apiCallExecutor) {
        this.walletApi = walletApi;
        this.apiCallExecutor = apiCallExecutor;
    }

    public void fetchMyWallets(ApiCallback<List<WalletResponse>> callback) {
        Call<List<WalletResponse>> call = walletApi.getMyWallets();
        apiCallExecutor.execute(call, callback);
    }
}
