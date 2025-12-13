package com.finmate.data.repository;

import android.content.Context;
import android.content.SharedPreferences;

import com.finmate.core.session.SessionManager;
import com.finmate.data.dto.LoginRequest;
import com.finmate.data.dto.RegisterRequest;
import com.finmate.data.dto.RefreshTokenRequest;
import com.finmate.data.dto.TokenResponse;
import com.finmate.data.local.datastore.AuthLocalDataSource;
import com.finmate.data.remote.api.AuthService;

import javax.inject.Inject;
import javax.inject.Singleton;

import dagger.hilt.android.qualifiers.ApplicationContext;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

@Singleton
public class AuthRepository {

    private final AuthService authApi;
    private final AuthLocalDataSource localDataSource;
    private final SessionManager sessionManager;
    private final Context context;

    @Inject
    public AuthRepository(AuthService authApi, AuthLocalDataSource localDataSource, SessionManager sessionManager, @ApplicationContext Context context) {
        this.authApi = authApi;
        this.localDataSource = localDataSource;
        this.sessionManager = sessionManager;
        this.context = context;
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
                    sessionManager.saveAccessToken(tokens.getAccessToken());
                    
                    // ✅ Gọi API /auth/me để lấy fullName và lưu vào SharedPreferences
                    fetchAndSaveUserInfo();
                    
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
    
    public void register(String email, String password, String fullName, String avatarUrl, RegisterCallback callback) {
        authApi.register(new RegisterRequest(email, password, fullName, avatarUrl)).enqueue(new Callback<TokenResponse>() {
            @Override
            public void onResponse(Call<TokenResponse> call, Response<TokenResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    TokenResponse tokens = response.body();

                    // Auto-login: Save tokens
                    localDataSource.saveTokens(tokens.getAccessToken(), tokens.getRefreshToken());
                    sessionManager.saveAccessToken(tokens.getAccessToken());

                    // ✅ Lưu fullName vào SharedPreferences (từ đăng ký)
                    SharedPreferences prefs = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
                    prefs.edit().putString("full_name", fullName != null ? fullName : "").apply();

                    // ✅ Gọi API /auth/me để lấy fullName từ server và cập nhật
                    fetchAndSaveUserInfo();

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
        // ✅ Xóa fullName khỏi SharedPreferences
        SharedPreferences prefs = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
        prefs.edit().remove("full_name").apply();
    }

    // ✅ Gọi API /auth/me để lấy user info và lưu fullName vào SharedPreferences
    private void fetchAndSaveUserInfo() {
        authApi.getCurrentUser().enqueue(new Callback<com.finmate.data.dto.UserInfoResponse>() {
            @Override
            public void onResponse(Call<com.finmate.data.dto.UserInfoResponse> call, Response<com.finmate.data.dto.UserInfoResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    com.finmate.data.dto.UserInfoResponse userInfo = response.body();
                    // ✅ Lưu fullName vào SharedPreferences
                    SharedPreferences prefs = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
                    if (userInfo.getFullName() != null && !userInfo.getFullName().isEmpty()) {
                        prefs.edit().putString("full_name", userInfo.getFullName()).apply();
                    }
                }
            }

            @Override
            public void onFailure(Call<com.finmate.data.dto.UserInfoResponse> call, Throwable t) {
                // Ignore errors - fullName sẽ được lấy từ đăng ký hoặc giữ nguyên giá trị cũ
            }
        });
    }
}
