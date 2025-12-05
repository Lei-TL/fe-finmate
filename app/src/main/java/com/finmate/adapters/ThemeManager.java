package com.finmate.adapters;
import android.content.Context;
import android.content.SharedPreferences;

import androidx.appcompat.app.AppCompatDelegate;

public class ThemeManager {

    private static final String PREFS_NAME = "ThemePrefs";
    private static final String THEME_KEY = "ThemeMode";
    private final SharedPreferences sharedPreferences;

    public ThemeManager(Context context) {
        sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public void saveTheme(int themeMode) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(THEME_KEY, themeMode);
        editor.apply();
    }

    public int getTheme() {
        // Mặc định là chế độ Hệ thống
        return sharedPreferences.getInt(THEME_KEY, AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
    }
}
