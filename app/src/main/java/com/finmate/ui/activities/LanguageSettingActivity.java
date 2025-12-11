package com.finmate.ui.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;

import com.finmate.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.Locale;

public class LanguageSettingActivity extends AppCompatActivity {

    ImageView btnBack;
    LinearLayout btnVietnamese, btnEnglish;
    BottomNavigationView bottomNavigation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // ⭐ Load ngôn ngữ trước khi setContentView
        loadLanguage();

        setContentView(R.layout.activity_language_settings);

        // ÁNH XẠ
        btnBack = findViewById(R.id.btnBack);
        btnVietnamese = findViewById(R.id.btnVietnamese);
        btnEnglish = findViewById(R.id.btnEnglish);
        bottomNavigation = findViewById(R.id.bottomNavigation);

        // QUAY LẠI
        btnBack.setOnClickListener(v -> finish());

        // ================= CHỌN TIẾNG VIỆT =================
        btnVietnamese.setOnClickListener(v -> {
            saveLanguage("vi");
            restartApp();
        });

        // ================= CHỌN ENGLISH =================
        btnEnglish.setOnClickListener(v -> {
            saveLanguage("en");
            restartApp();
        });


    }

    // ==================================================
    // ⭐ LƯU NGÔN NGỮ VÀO SharedPreferences
    // ==================================================
    private void saveLanguage(String lang) {
        SharedPreferences.Editor editor =
                getSharedPreferences("settings", MODE_PRIVATE).edit();

        editor.putString("language", lang);
        editor.apply();
    }

    // ==================================================
    // ⭐ ÁP DỤNG NGÔN NGỮ KHI MỞ ACTIVITY
    // ==================================================
    private void loadLanguage() {
        SharedPreferences prefs = getSharedPreferences("settings", MODE_PRIVATE);
        String lang = prefs.getString("language", "vi");

        Locale newLocale = new Locale(lang);
        Locale.setDefault(newLocale);

        android.content.res.Configuration config =
                new android.content.res.Configuration();

        config.setLocale(newLocale);

        getResources().updateConfiguration(
                config,
                getResources().getDisplayMetrics()
        );
    }

    // ==================================================
    // ⭐ RESTART APP SAU KHI ĐỔI NGÔN NGỮ
    // ==================================================
    private void restartApp() {
        Intent intent = new Intent(this, SettingsActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP |
                Intent.FLAG_ACTIVITY_NEW_TASK |
                Intent.FLAG_ACTIVITY_CLEAR_TASK);

        startActivity(intent);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }
}
