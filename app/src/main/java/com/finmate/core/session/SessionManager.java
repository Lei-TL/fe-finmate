package com.finmate.core.session;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class SessionManager {

    // chỉ dùng trong RAM cho AuthInterceptor & UI
    private volatile String accessToken;

    @Inject
    public SessionManager() {}

    public synchronized void saveAccessToken(@Nullable String token) {
        this.accessToken = token;
    }

    @Nullable
    public String getAccessToken() {
        return accessToken;
    }

    public void clear() {
        saveAccessToken(null);
    }

    public boolean isLoggedIn() {
        return accessToken != null && !accessToken.isEmpty();
    }
}

