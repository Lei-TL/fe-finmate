package com.finmate.core.network;

import androidx.annotation.NonNull;

import com.finmate.data.dto.TokenResponse;
import com.finmate.data.repository.AuthRepository;

import javax.inject.Inject;
import javax.inject.Singleton;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

@Singleton
public class ApiCallExecutor {

    private final AuthRepository authRepository;

    @Inject
    public ApiCallExecutor(AuthRepository authRepository) {
        this.authRepository = authRepository;
    }

    public <T> void execute(Call<T> call, ApiCallback<T> callback) {
        android.util.Log.d("ApiCallExecutor", "Executing API call: " + call.request().method() + " " + call.request().url());
        executeInternal(call, callback, false);
    }

    private <T> void executeInternal(Call<T> call,
                                     ApiCallback<T> callback,
                                     boolean isRetry) {

        call.enqueue(new Callback<T>() {
            @Override
            public void onResponse(@NonNull Call<T> call,
                                   @NonNull Response<T> response) {
                android.util.Log.d("ApiCallExecutor", "Response received: code=" + response.code() + ", isSuccessful=" + response.isSuccessful() + ", url=" + call.request().url());

                if (response.isSuccessful()) {
                    android.util.Log.d("ApiCallExecutor", "Request successful, calling onSuccess");
                    callback.onSuccess(response.body());
                    return;
                }

                android.util.Log.w("ApiCallExecutor", "Request failed with code: " + response.code());

                if (response.code() == 401 && !isRetry) {
                    android.util.Log.d("ApiCallExecutor", "401 Unauthorized, attempting token refresh...");
                    authRepository.refreshToken(new AuthRepository.RefreshCallback() {
                        @Override
                        public void onSuccess() {
                            android.util.Log.d("ApiCallExecutor", "Token refreshed, retrying request...");
                            Call<T> retryCall = call.clone();
                            executeInternal(retryCall, callback, true);
                        }

                        @Override
                        public void onError(String message) {
                            android.util.Log.e("ApiCallExecutor", "Refresh token failed: " + message);
                            callback.onError("Refresh token failed: " + message);
                        }
                    });
                    return;
                }

                android.util.Log.e("ApiCallExecutor", "Request failed: " + response.code() + ", error body: " + (response.errorBody() != null ? response.errorBody().toString() : "null"));
                callback.onError("Request failed: " + response.code());
            }

            @Override
            public void onFailure(@NonNull Call<T> call,
                                  @NonNull Throwable t) {
                android.util.Log.e("ApiCallExecutor", "Request failure: " + t.getMessage(), t);
                callback.onError(t.getMessage());
            }
        });
    }
}
