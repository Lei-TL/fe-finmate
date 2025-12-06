package com.finmate.core.ui;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.appcompat.app.AppCompatDelegate;

public class ThemeHelper {

    public static final String LIGHT_MODE = "light";
    public static final String DARK_MODE = "dark";
    public static final String SYSTEM_DEFAULT = "system";

    private static final String PREFS_NAME = "user_prefs";
    private static final String KEY_THEME = "theme";

    // Áp dụng theme theo theme đang lưu trong SharedPreferences
    public static void applyCurrentTheme(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String savedTheme = prefs.getString(KEY_THEME, SYSTEM_DEFAULT);

        applyTheme(savedTheme);
    }

    // Áp dụng theme theo giá trị truyền vào
    public static void applyTheme(String theme) {
        switch (theme) {
            case LIGHT_MODE:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                break;
            case DARK_MODE:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                break;
            default:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
                break;
        }
    }
}
