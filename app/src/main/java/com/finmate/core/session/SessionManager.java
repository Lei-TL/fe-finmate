package com.finmate.core.session;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class SessionManager {

    private String accessToken;

    @Inject
    public SessionManager() {}

    public void saveAccessToken(String token) {
        this.accessToken = token;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void clear() {
        this.accessToken = null;
    }

    public boolean isLoggedIn() {
        return accessToken != null && !accessToken.isEmpty();
    }
}
