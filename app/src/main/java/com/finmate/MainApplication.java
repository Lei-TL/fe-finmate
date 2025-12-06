package com.finmate;

import android.app.Application;
import android.content.SharedPreferences;

import com.finmate.core.ui.ThemeHelper;

import dagger.hilt.android.HiltAndroidApp;

@HiltAndroidApp
public class MainApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        SharedPreferences prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
        String theme = prefs.getString("theme", ThemeHelper.SYSTEM_DEFAULT);
        ThemeHelper.applyTheme(theme);
    }
}
