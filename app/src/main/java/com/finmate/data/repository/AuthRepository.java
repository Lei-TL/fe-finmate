package com.finmate.data.repository;

import com.finmate.core.session.SessionManager;
import com.finmate.data.dto.LoginRequest;
import com.finmate.data.dto.RegisterRequest;
import com.finmate.data.dto.RefreshTokenRequest;
import com.finmate.data.dto.TokenResponse;
import com.finmate.data.local.datastore.AuthLocalDataSource;
import com.finmate.data.remote.api.AuthService;

import javax.inject.Inject;
import javax.inject.Singleton;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

@Singleton
public class AuthRepository {

    private final AuthService authApi;
    private final AuthLocalDataSource localDataSource;
    private final SessionManager sessionManager;

    @Inject
    public AuthRepository(AuthService authApi, AuthLocalDataSource localDataSource, SessionManager sessionManager) {
        this.authApi = authApi;
        this.localDataSource = localDataSource;
        this.sessionManager = sessionManager;
    }

    public interface LoginCallback {
        void onSuccess();
        void onError(String message);
    }
    
    public interface RegisterCallback {
        void onSuccess();
        void onError(String message);
    }

    public interface RefreshCallback {
        void onSuccess();
        void onError(String message);
    }

    public void login(String email, String password, LoginCallback callback) {
        authApi.login(new LoginRequest(email, password)).enqueue(new Callback<TokenResponse>() {
            @Override
            public void onResponse(Call<TokenResponse> call, Response<TokenResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    TokenResponse tokens = response.body();
                    
                    // Save to DataStore
                    localDataSource.saveTokens(tokens.getAccessToken(), tokens.getRefreshToken());
                    
                    // Save to SessionManager
                    sessionManager.saveAccessToken(tokens.getAccessToken()); // FIX: saveToken -> saveAccessToken
                    
                    callback.onSuccess();
                } else {
                    callback.onError("Login failed: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<TokenResponse> call, Throwable t) {
                callback.onError(t.getMessage());
            }
        });
    }
    
    public void register(String email, String password, RegisterCallback callback) {
        authApi.register(new RegisterRequest(email, password)).enqueue(new Callback<TokenResponse>() {
            @Override
            public void onResponse(Call<TokenResponse> call, Response<TokenResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    TokenResponse tokens = response.body();

                    // Auto-login: Save tokens
                    localDataSource.saveTokens(tokens.getAccessToken(), tokens.getRefreshToken());
                    sessionManager.saveAccessToken(tokens.getAccessToken()); // FIX: saveToken -> saveAccessToken

                    callback.onSuccess();
                } else {
                    callback.onError("Registration failed: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<TokenResponse> call, Throwable t) {
                callback.onError(t.getMessage());
            }
        });
    }

    public void refreshToken(RefreshCallback callback) {
        // Lấy refresh token từ DataStore
        localDataSource.getRefreshTokenSingle()
                .subscribe(refreshToken -> {
                    if (refreshToken == null || refreshToken.isEmpty()) {
                        callback.onError("No refresh token found");
                        return;
                    }

                    // Gọi API /auth/refresh
                    authApi.refresh(new RefreshTokenRequest(refreshToken))
                            .enqueue(new Callback<TokenResponse>() {
                                @Override
                                public void onResponse(Call<TokenResponse> call, Response<TokenResponse> response) {
                                    if (response.isSuccessful() && response.body() != null) {
                                        TokenResponse tokens = response.body();

                                        // Lưu lại token mới
                                        localDataSource.saveTokens(
                                                tokens.getAccessToken(),
                                                tokens.getRefreshToken()
                                        );
                                        sessionManager.saveAccessToken(tokens.getAccessToken());

                                        callback.onSuccess();
                                    } else {
                                        callback.onError("Refresh failed: " + response.code());
                                    }
                                }

                                @Override
                                public void onFailure(Call<TokenResponse> call, Throwable t) {
                                    callback.onError(t.getMessage());
                                }
                            });

                }, throwable -> callback.onError(throwable.getMessage()));
    }


    public void checkLoginStatus(LoginCallback callback) {
        // Check DataStore for token
        localDataSource.getAccessTokenSingle()
                .subscribe(token -> {
                    if (token != null && !token.isEmpty()) {
                        sessionManager.saveAccessToken(token); // FIX: saveToken -> saveAccessToken
                        callback.onSuccess();
                    } else {
                        callback.onError("No token found");
                    }
                }, throwable -> callback.onError(throwable.getMessage()));
    }

    public void logout() {
        localDataSource.clearTokens();
        sessionManager.clear();
    }
}
