package com.finmate.util;

import android.content.Context;
import android.content.SharedPreferences;
import dagger.hilt.android.qualifiers.ApplicationContext;
import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class TokenManager {
    private SharedPreferences prefs;
    private SharedPreferences.Editor editor;

    @Inject
    public TokenManager(@ApplicationContext Context context) {
        prefs = context.getSharedPreferences("FinMatePrefs", Context.MODE_PRIVATE);
        editor = prefs.edit();
    }

    public void saveToken(String accessToken, String refreshToken) {
        editor.putString("ACCESS_TOKEN", accessToken);
        editor.putString("REFRESH_TOKEN", refreshToken);
        editor.apply();
    }

    public boolean isLoggedIn() {
        return prefs.contains("ACCESS_TOKEN");
    }

    public String getToken() {
        return prefs.getString("ACCESS_TOKEN", null);
    }

    public void logout() {
        editor.clear().apply();
    }
}
