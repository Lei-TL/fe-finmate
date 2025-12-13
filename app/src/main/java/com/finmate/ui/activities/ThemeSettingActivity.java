package com.finmate.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.finmate.R;
import com.finmate.core.ui.ThemeHelper;
import com.finmate.ui.auth.LoginActivity;
import com.finmate.ui.base.BaseActivity;

public class ThemeSettingActivity extends BaseActivity {

    ImageView btnBack;
    LinearLayout btnLight, btnDark, btnSystem;
    ImageView ivLightCheck, ivDarkCheck, ivSystemCheck;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_theme_settings);

        // Ánh xạ
        btnBack = findViewById(R.id.btnBack);
        btnLight = findViewById(R.id.btnLight);
        btnDark = findViewById(R.id.btnDark);
        btnSystem = findViewById(R.id.btnSystem);
        ivLightCheck = findViewById(R.id.ivLightCheck);
        ivDarkCheck = findViewById(R.id.ivDarkCheck);
        ivSystemCheck = findViewById(R.id.ivSystemCheck);

        loadCurrentTheme();

        btnBack.setOnClickListener(v -> finish());

        btnLight.setOnClickListener(v -> {
            ThemeHelper.saveTheme(this, ThemeHelper.THEME_LIGHT);
            restartApp();
        });

        btnDark.setOnClickListener(v -> {
            ThemeHelper.saveTheme(this, ThemeHelper.THEME_DARK);
            restartApp();
        });

        btnSystem.setOnClickListener(v -> {
            ThemeHelper.saveTheme(this, ThemeHelper.THEME_SYSTEM);
            restartApp();
        });
    }

    private void loadCurrentTheme() {
        String currentTheme = ThemeHelper.getCurrentTheme(this);
        hideAllChecks();

        if (ThemeHelper.THEME_LIGHT.equals(currentTheme)) {
            ivLightCheck.setVisibility(View.VISIBLE);
        } else if (ThemeHelper.THEME_DARK.equals(currentTheme)) {
            ivDarkCheck.setVisibility(View.VISIBLE);
        } else {
            ivSystemCheck.setVisibility(View.VISIBLE);
        }
    }

    private void hideAllChecks() {
        ivLightCheck.setVisibility(View.GONE);
        ivDarkCheck.setVisibility(View.GONE);
        ivSystemCheck.setVisibility(View.GONE);
    }

    private void restartApp() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        finish();
    }
}
