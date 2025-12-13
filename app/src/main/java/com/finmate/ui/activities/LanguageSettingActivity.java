package com.finmate.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.finmate.R;
import com.finmate.data.local.datastore.UserPreferencesLocalDataSource;
import com.finmate.ui.auth.LoginActivity;
import com.finmate.ui.base.BaseActivity;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class LanguageSettingActivity extends BaseActivity {

    @Inject
    UserPreferencesLocalDataSource userPreferencesLocalDataSource;

    ImageView btnBack;
    LinearLayout btnVietnamese, btnEnglish, btnSystem;
    ImageView ivVietnameseCheck, ivEnglishCheck, ivSystemCheck;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_language_settings);

        // ÁNH XẠ
        btnBack = findViewById(R.id.btnBack);
        btnVietnamese = findViewById(R.id.btnVietnamese);
        btnEnglish = findViewById(R.id.btnEnglish);
        btnSystem = findViewById(R.id.btnSystem);
        ivVietnameseCheck = btnVietnamese.findViewById(R.id.ivVietnameseCheck);
        ivEnglishCheck = btnEnglish.findViewById(R.id.ivEnglishCheck);
        ivSystemCheck = btnSystem.findViewById(R.id.ivSystemCheck);

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
    }

    private void saveLanguage(String lang) {
        userPreferencesLocalDataSource.saveLanguage(lang);
    }

    private void loadCurrentLanguage() {
        String currentLang = userPreferencesLocalDataSource.getLanguageSync();
        hideAllChecks();

        if ("vi".equals(currentLang)) {
            if (ivVietnameseCheck != null) ivVietnameseCheck.setVisibility(android.view.View.VISIBLE);
        } else if ("en".equals(currentLang)) {
            if (ivEnglishCheck != null) ivEnglishCheck.setVisibility(android.view.View.VISIBLE);
        } else {
            if (ivSystemCheck != null) ivSystemCheck.setVisibility(android.view.View.VISIBLE);
        }
    }

    private void hideAllChecks() {
        if (ivVietnameseCheck != null) ivVietnameseCheck.setVisibility(android.view.View.GONE);
        if (ivEnglishCheck != null) ivEnglishCheck.setVisibility(android.view.View.GONE);
        if (ivSystemCheck != null) ivSystemCheck.setVisibility(android.view.View.GONE);
    }

    private void restartApp() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        finish();
    }
}
