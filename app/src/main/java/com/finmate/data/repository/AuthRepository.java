package com.finmate.data.repository;

import com.finmate.core.session.SessionManager;
import com.finmate.data.local.datastore.AuthLocalDataSource;
import com.finmate.data.remote.api.AuthService;
import com.finmate.data.dto.LoginRequest;
import com.finmate.data.dto.RefreshTokenRequest;
import com.finmate.data.dto.RegisterRequest;
import com.finmate.data.dto.TokenResponse;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;

@Singleton
public class AuthRepository {

    private final AuthService authService;
    private final AuthLocalDataSource localDataSource;
    private final SessionManager sessionManager;

    @Inject
    public AuthRepository(
            AuthService authService,
            AuthLocalDataSource localDataSource,
            SessionManager sessionManager) {
        this.authService = authService;
        this.localDataSource = localDataSource;
        this.sessionManager = sessionManager;
    }

    public Completable login(String username, String password) {
        return authService.login(new LoginRequest(username, password))
                .flatMapCompletable(this::handleTokenResponse);
    }

    public Completable register(String email, String password) {
        return authService.register(new RegisterRequest(email, password))
                .flatMapCompletable(this::handleTokenResponse);
    }

    public boolean refreshTokenBlocking() {
        String refreshToken = localDataSource.getRefreshTokenSingle().blockingGet();
        if (refreshToken == null || refreshToken.isEmpty()) {
            return false;
        }

        try {
            TokenResponse newTokens = authService.refreshToken(new RefreshTokenRequest(refreshToken)).blockingGet();
            handleTokenResponse(newTokens).blockingAwait(); // Wait for save to complete
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public Single<Boolean> checkLoginStatus() {
        return localDataSource.getAccessTokenSingle()
                .map(token -> {
                    if (token != null && !token.isEmpty()) {
                        sessionManager.saveAccessToken(token);
                        return true;
                    }
                    return false;
                });
    }

    public Completable logout() {
        sessionManager.clear();
        return localDataSource.clearTokens();
    }

    private Completable handleTokenResponse(TokenResponse tokens) {
        sessionManager.saveAccessToken(tokens.getAccessToken());
        return localDataSource.saveTokens(tokens.getAccessToken(), tokens.getRefreshToken());
    }
}
