package com.finmate.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.finmate.R;
import com.finmate.data.local.datastore.UserPreferencesLocalDataSource;
import com.finmate.ui.activities.AddTransactionActivity;
import com.finmate.ui.activities.StatisticActivity;
import com.finmate.ui.base.BaseActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class LanguageSettingActivity extends BaseActivity {

    @Inject
    UserPreferencesLocalDataSource userPreferencesLocalDataSource;

    ImageView btnBack;
    LinearLayout btnVietnamese, btnEnglish, btnSystem;
    ImageView ivVietnameseCheck, ivEnglishCheck, ivSystemCheck;
    BottomNavigationView bottomNavigation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Locale đã được apply tự động bởi BaseActivity.attachBaseContext()
        setContentView(R.layout.activity_language_settings);

        // ÁNH XẠ
        btnBack = findViewById(R.id.btnBack);
        btnVietnamese = findViewById(R.id.btnVietnamese);
        btnEnglish = findViewById(R.id.btnEnglish);
        btnSystem = findViewById(R.id.btnSystem);
        ivVietnameseCheck = btnVietnamese.findViewById(R.id.ivVietnameseCheck);
        ivEnglishCheck = btnEnglish.findViewById(R.id.ivEnglishCheck);
        ivSystemCheck = btnSystem.findViewById(R.id.ivSystemCheck);
        bottomNavigation = findViewById(R.id.bottomNavigation);
        
        // Load current language và hiển thị check
        loadCurrentLanguage();

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

        // ================= CHỌN THEO HỆ THỐNG =================
        btnSystem.setOnClickListener(v -> {
            saveLanguage("system");
            restartApp();
        });

        // ================= BOTTOM NAVIGATION =================
        setupBottomNavigation();
    }

    private void setupBottomNavigation() {
        bottomNavigation.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            Intent intent = null;

            if (id == R.id.nav_home) {
                intent = new Intent(this, com.finmate.ui.home.HomeActivity.class);
            } else if (id == R.id.nav_wallet) {
                intent = new Intent(this, com.finmate.ui.home.WalletActivity.class);
            } else if (id == R.id.nav_add) {
                intent = new Intent(this, AddTransactionActivity.class);
            } else if (id == R.id.nav_statistic) {
                intent = new Intent(this, StatisticActivity.class);
            } else if (id == R.id.nav_settings) {
                return true; // Đang ở Settings (LanguageSettingActivity là sub-screen của Settings), không cần navigate
            }

            if (intent != null) {
                startActivity(intent);
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                finish(); // Đóng màn hình hiện tại khi navigate
            }
            return true;
        });
    }

    // ==================================================
    // ⭐ LƯU NGÔN NGỮ VÀO DataStore (và SharedPreferences)
    // ==================================================
    private void saveLanguage(String lang) {
        userPreferencesLocalDataSource.saveLanguage(lang);
    }

    // ==================================================
    // ⭐ LOAD NGÔN NGỮ HIỆN TẠI VÀ HIỂN THỊ CHECK
    // ==================================================
    private void loadCurrentLanguage() {
        String currentLang = userPreferencesLocalDataSource.getLanguageSync();
        hideAllChecks();
        
        if ("vi".equals(currentLang)) {
            if (ivVietnameseCheck != null) ivVietnameseCheck.setVisibility(android.view.View.VISIBLE);
        } else if ("en".equals(currentLang)) {
            if (ivEnglishCheck != null) ivEnglishCheck.setVisibility(android.view.View.VISIBLE);
        } else if ("system".equals(currentLang) || currentLang == null || currentLang.isEmpty()) {
            if (ivSystemCheck != null) ivSystemCheck.setVisibility(android.view.View.VISIBLE);
        }
    }
    
    private void hideAllChecks() {
        if (ivVietnameseCheck != null) ivVietnameseCheck.setVisibility(android.view.View.GONE);
        if (ivEnglishCheck != null) ivEnglishCheck.setVisibility(android.view.View.GONE);
        if (ivSystemCheck != null) ivSystemCheck.setVisibility(android.view.View.GONE);
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
