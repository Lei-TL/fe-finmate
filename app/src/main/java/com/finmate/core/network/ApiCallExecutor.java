package com.finmate.core.network;

import androidx.annotation.NonNull;

import com.finmate.data.remote.api.ApiCallback;

import javax.inject.Inject;
import javax.inject.Singleton;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

@Singleton
public class ApiCallExecutor {

    @Inject
    public ApiCallExecutor() {
    }

    public <T> void execute(Call<T> call, ApiCallback<T> callback) {
        call.enqueue(new Callback<T>() {
            @Override
            public void onResponse(@NonNull Call<T> call, @NonNull Response<T> response) {
                if (response.isSuccessful()) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError("Request failed: " + response.code(), response.code());
                }
            }

            @Override
            public void onFailure(@NonNull Call<T> call, @NonNull Throwable t) {
                callback.onError(t.getMessage(), null);
            }
        });
    }
}
