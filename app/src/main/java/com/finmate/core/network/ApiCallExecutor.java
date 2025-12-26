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
        executeInternal(call, callback, false);
    }

    private <T> void executeInternal(Call<T> call,
                                     ApiCallback<T> callback,
                                     boolean isRetry) {

        call.enqueue(new Callback<T>() {
            @Override
            public void onResponse(@NonNull Call<T> call,
                                   @NonNull Response<T> response) {

                if (response.isSuccessful()) {
                    callback.onSuccess(response.body());
                    return;
                }

                if (response.code() == 401 && !isRetry) {
                    authRepository.refreshToken(new AuthRepository.RefreshCallback() {
                        @Override
                        public void onSuccess() {
                            Call<T> retryCall = call.clone();
                            executeInternal(retryCall, callback, true);
                        }

                        @Override
                        public void onError(String message) {
                            callback.onError("Refresh token failed: " + message);
                        }
                    });
                    return;
                }

                callback.onError("Request failed: " + response.code());
            }

            @Override
            public void onFailure(@NonNull Call<T> call,
                                  @NonNull Throwable t) {
                callback.onError(t.getMessage());
            }
        });
    }
}
