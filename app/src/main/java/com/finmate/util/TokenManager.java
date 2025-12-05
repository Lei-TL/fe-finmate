package com.finmate.util;

import android.content.Context;

import com.finmate.data.local.database.AppDatabase;
import com.finmate.entities.TokenEntity;

public class TokenManager {
    private AppDatabase db;

    public TokenManager(Context context) {
        db = AppDatabase.getDatabase(context);
    }

    public void saveToken(String accessToken, String refreshToken) {
        TokenEntity token = new TokenEntity(accessToken, refreshToken);
        // Clear old tokens before inserting new one to ensure only one valid session exists
        db.tokenDao().clearTokens();
        db.tokenDao().insertToken(token);
    }

    public String getAccessToken() {
        TokenEntity token = db.tokenDao().getToken();
        return token != null ? token.accessToken : null;
    }
    
    public String getRefreshToken() {
        TokenEntity token = db.tokenDao().getToken();
        return token != null ? token.refreshToken : null;
    }

    public void clearToken() {
        db.tokenDao().clearTokens();
    }
    
    public boolean isLoggedIn() {
        return getAccessToken() != null;
    }
}
