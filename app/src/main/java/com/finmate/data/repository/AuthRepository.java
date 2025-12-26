package com.finmate.data.repository;

import android.content.Context;
import android.content.SharedPreferences;

import com.finmate.core.network.NetworkChecker;
import com.finmate.core.session.SessionManager;
import com.finmate.data.dto.LoginRequest;
import com.finmate.data.dto.RegisterRequest;
import com.finmate.data.dto.RefreshTokenRequest;
import com.finmate.data.dto.TokenResponse;
import com.finmate.data.local.database.AppDatabase;
import com.finmate.data.local.datastore.AuthLocalDataSource;
import com.finmate.data.remote.api.AuthService;
import com.finmate.data.repository.WalletRemoteRepository;
import com.finmate.data.repository.TransactionRemoteRepository;
import com.finmate.data.repository.TransactionRepository;

import dagger.Lazy;

import java.util.concurrent.Executors;

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
    private final Lazy<WalletRemoteRepository> walletRemoteRepositoryLazy;
    private final Lazy<com.finmate.data.repository.WalletRepository> walletRepositoryLazy;
    private final NetworkChecker networkChecker;
    private final Lazy<TransactionRemoteRepository> transactionRemoteRepositoryLazy;
    private final Lazy<TransactionRepository> transactionRepositoryLazy;

    @Inject
    public AuthRepository(AuthService authApi,
                          AuthLocalDataSource localDataSource,
                          SessionManager sessionManager,
                          @ApplicationContext Context context,
                          Lazy<WalletRemoteRepository> walletRemoteRepositoryLazy,
                          Lazy<com.finmate.data.repository.WalletRepository> walletRepositoryLazy,
                          NetworkChecker networkChecker,
                          Lazy<TransactionRemoteRepository> transactionRemoteRepositoryLazy,
                          Lazy<TransactionRepository> transactionRepositoryLazy) {
        this.authApi = authApi;
        this.localDataSource = localDataSource;
        this.sessionManager = sessionManager;
        this.context = context;
        this.walletRemoteRepositoryLazy = walletRemoteRepositoryLazy;
        this.walletRepositoryLazy = walletRepositoryLazy;
        this.networkChecker = networkChecker;
        this.transactionRemoteRepositoryLazy = transactionRemoteRepositoryLazy;
        this.transactionRepositoryLazy = transactionRepositoryLazy;
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

                    // ✅ Sau khi đăng nhập thành công: sync ví + giao dịch rồi mới callback onSuccess
                    syncWalletsFromBackend(() ->
                            syncTransactionsFromBackend(callback::onSuccess)
                    );
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
                    
                    // ✅ Sau khi đăng ký (auto-login): sync ví + giao dịch rồi mới callback onSuccess
                    syncWalletsFromBackend(() ->
                            syncTransactionsFromBackend(callback::onSuccess)
                    );
                } else {
                    // ✅ FIX: Xử lý lỗi 409 Conflict
                    if (response.code() == 409) {
                        callback.onError("Email đã tồn tại. Vui lòng sử dụng email khác.");
                    } else {
                        callback.onError("Đăng ký thất bại: " + response.code());
                    }
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
                        sessionManager.saveAccessToken(token);
                        callback.onSuccess();
                    } else {
                        callback.onError("No token found");
                    }
                }, throwable -> callback.onError(throwable.getMessage()));
    }

    public void logout() {
        // ✅ 1. Xóa tokens và session
        localDataSource.clearTokens();
        sessionManager.clear();
        
        // ✅ 2. Xóa fullName khỏi SharedPreferences
        SharedPreferences userPrefs = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
        userPrefs.edit().remove("full_name").apply();
        
        // ✅ 3. Xóa sync preferences (synced transaction IDs, etc.)
        SharedPreferences syncPrefs = context.getSharedPreferences("sync_prefs", Context.MODE_PRIVATE);
        syncPrefs.edit().clear().apply();
        
        // ✅ 4. Xóa tất cả dữ liệu trong local database (chạy trên background thread)
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                AppDatabase database = AppDatabase.getDatabase(context);
                
                // Xóa user-specific data
                database.tokenDao().clearTokens();
                database.walletDao().deleteAll();
                database.transactionDao().deleteAll();
                database.friendDao().deleteAll();
                database.pendingSyncDao().deleteAll();
                
                android.util.Log.d("AuthRepository", "All local data cleared successfully (categories preserved)");
            } catch (Exception e) {
                android.util.Log.e("AuthRepository", "Error clearing local database: " + e.getMessage(), e);
            }
        });
    }

    // ✅ Gọi API /auth/me để lấy user info và lưu fullName vào SharedPreferences
    private void fetchAndSaveUserInfo() {
        authApi.getCurrentUser().enqueue(new Callback<com.finmate.data.dto.UserInfoResponse>() {
            @Override
            public void onResponse(Call<com.finmate.data.dto.UserInfoResponse> call, Response<com.finmate.data.dto.UserInfoResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    com.finmate.data.dto.UserInfoResponse userInfo = response.body();
                    SharedPreferences prefs = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
                    if (userInfo.getFullName() != null && !userInfo.getFullName().isEmpty()) {
                        prefs.edit().putString("full_name", userInfo.getFullName()).apply();
                    }
                }
            }

            @Override
            public void onFailure(Call<com.finmate.data.dto.UserInfoResponse> call, Throwable t) {
                // Ignore errors
            }
        });
    }
    
    /**
     * ✅ Sync wallets từ backend về local database sau khi đăng nhập/đăng ký
     */
    private void syncWalletsFromBackend(Runnable onComplete) {
        if (networkChecker == null || !networkChecker.isNetworkAvailable()) {
            android.util.Log.d("AuthRepository", "No network available, skipping wallet sync");
            if (onComplete != null) onComplete.run();
            return;
        }

        WalletRemoteRepository walletRemoteRepository = walletRemoteRepositoryLazy.get();
        com.finmate.data.repository.WalletRepository walletRepository = walletRepositoryLazy.get();

        if (walletRemoteRepository == null || walletRepository == null) {
            android.util.Log.e("AuthRepository", "Wallet repositories not available, skipping wallet sync");
            if (onComplete != null) onComplete.run();
            return;
        }

        walletRemoteRepository.fetchMyWallets(new com.finmate.core.network.ApiCallback<java.util.List<com.finmate.data.dto.WalletResponse>>() {
            @Override
            public void onSuccess(java.util.List<com.finmate.data.dto.WalletResponse> walletResponses) {
                try {
                    if (walletResponses == null || walletResponses.isEmpty()) {
                        android.util.Log.d("AuthRepository", "No wallets found from backend");
                        return;
                    }

                    java.util.List<com.finmate.data.local.database.entity.WalletEntity> walletEntities = new java.util.ArrayList<>();
                    for (com.finmate.data.dto.WalletResponse w : walletResponses) {
                        if (w.isDeleted() || w.getId() == null || w.getId().isEmpty()) {
                            continue;
                        }

                        double currentBalance = w.getCurrentBalance() != 0.0 ? w.getCurrentBalance() : w.getInitialBalance();
                        double initialBalance = w.getInitialBalance();

                        String formattedBalance = String.format(
                                java.util.Locale.getDefault(),
                                "%,.0f %s",
                                currentBalance,
                                w.getCurrency() != null ? w.getCurrency() : ""
                        );

                        com.finmate.data.local.database.entity.WalletEntity entity = new com.finmate.data.local.database.entity.WalletEntity(
                                w.getId(),
                                w.getName(),
                                formattedBalance,
                                currentBalance,
                                initialBalance,
                                0
                        );
                        walletEntities.add(entity);
                    }

                    if (!walletEntities.isEmpty()) {
                        walletRepository.upsertAll(walletEntities);
                        android.util.Log.d("AuthRepository", "Successfully synced " + walletEntities.size() + " wallets from backend");
                    }
                } finally {
                    if (onComplete != null) onComplete.run();
                }
            }

            @Override
            public void onError(String message) {
                android.util.Log.e("AuthRepository", "Failed to sync wallets from backend: " + message);
                if (onComplete != null) onComplete.run();
            }
        });
    }

    /**
     * ✅ Sync tất cả giao dịch từ backend về local sau khi đăng nhập/đăng ký
     * (lấy trang đầu, không filter ví hoặc thời gian)
     */
    private void syncTransactionsFromBackend(Runnable onComplete) {
        if (networkChecker == null || !networkChecker.isNetworkAvailable()) {
            android.util.Log.d("AuthRepository", "No network available, skipping transaction sync");
            if (onComplete != null) onComplete.run();
            return;
        }

        TransactionRemoteRepository transactionRemoteRepository = transactionRemoteRepositoryLazy.get();
        TransactionRepository transactionRepository = transactionRepositoryLazy.get();
        com.finmate.data.repository.WalletRepository walletRepository = walletRepositoryLazy.get();

        if (transactionRemoteRepository == null || transactionRepository == null || walletRepository == null) {
            android.util.Log.e("AuthRepository", "Transaction repositories not available, skipping transaction sync");
            if (onComplete != null) onComplete.run();
            return;
        }

        transactionRemoteRepository.fetchTransactions(null, new com.finmate.core.network.ApiCallback<com.finmate.data.dto.TransactionPageResponse>() {
            @Override
            public void onSuccess(com.finmate.data.dto.TransactionPageResponse page) {
                try {
                    if (page == null || page.getContent() == null) {
                        return;
                    }

                    walletRepository.getAll(new com.finmate.data.repository.WalletRepository.Callback() {
                        @Override
                        public void onResult(java.util.List<com.finmate.data.local.database.entity.WalletEntity> wallets) {
                            java.util.List<com.finmate.data.local.database.entity.TransactionEntity> mapped = new java.util.ArrayList<>();
                            for (com.finmate.data.dto.TransactionResponse t : page.getContent()) {
                                if (t.getId() == null || t.getId().isEmpty()) {
                                    continue;
                                }

                                String walletNameForTransaction = null;
                                if (t.getWalletId() != null && wallets != null) {
                                    for (com.finmate.data.local.database.entity.WalletEntity w : wallets) {
                                        if (w.id.equals(t.getWalletId())) {
                                            walletNameForTransaction = w.name;
                                            break;
                                        }
                                    }
                                }

                                String categoryName = t.getCategoryName() != null ? t.getCategoryName() : "";

                                double amountValue = t.getAmount() != null ? t.getAmount().doubleValue() : 0.0;
                                String amountFormatted = String.format(
                                        "%,.0f",
                                        amountValue
                                );

                                String dateDisplay = "";
                                if (t.getOccurredAt() != null) {
                                    dateDisplay = t.getOccurredAt();
                                }

                                String title = (t.getNote() != null && !t.getNote().isEmpty())
                                        ? t.getNote()
                                        : (categoryName.isEmpty() ? "" : categoryName);

                                com.finmate.data.local.database.entity.TransactionEntity entity =
                                        new com.finmate.data.local.database.entity.TransactionEntity(
                                                t.getId(),
                                                title,
                                                categoryName,
                                                amountFormatted,
                                                walletNameForTransaction != null ? walletNameForTransaction : "",
                                                dateDisplay,
                                                t.getType(),
                                                amountValue
                                        );

                                mapped.add(entity);
                            }

                            // Upsert tất cả giao dịch nhận được
                            transactionRepository.upsertAll(mapped);

                            // Sau khi upsert, cập nhật lại balance cho các ví liên quan
                            if (wallets != null && !mapped.isEmpty()) {
                                AppDatabase db = AppDatabase.getDatabase(context);
                                java.util.Set<String> walletNamesToUpdate = new java.util.HashSet<>();
                                for (com.finmate.data.local.database.entity.TransactionEntity tx : mapped) {
                                    if (tx.wallet != null && !tx.wallet.isEmpty()) {
                                        walletNamesToUpdate.add(tx.wallet);
                                    }
                                }
                                for (String walletName : walletNamesToUpdate) {
                                    walletRepository.updateWalletBalance(walletName, db.transactionDao());
                                }
                            }
                        }
                    });
                } finally {
                    if (onComplete != null) onComplete.run();
                }
            }

            @Override
            public void onError(String message) {
                android.util.Log.e("AuthRepository", "Failed to sync transactions from backend: " + message);
                if (onComplete != null) onComplete.run();
            }
        });
    }
}
