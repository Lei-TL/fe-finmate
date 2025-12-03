package com.finmate.UI.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import com.finmate.R;
import com.finmate.adapters.ThemeHelper;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class SettingsActivity extends BaseActivity {

    ImageView btnBack;
    LinearLayout btnLanguage, btnCategory, btnTheme, btnFriend, btnAccount, btnNotification;
    BottomNavigationView bottomNavigation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        // ÁNH XẠ
        initViews();

        // XỬ LÝ SỰ KIỆN
        setupListeners();

        // Chọn tab Cài đặt trên Bottom Nav
        bottomNavigation.setSelectedItemId(R.id.nav_settings);
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        btnLanguage = findViewById(R.id.btnLanguage);
        btnCategory = findViewById(R.id.btnCategory);
        btnTheme = findViewById(R.id.btnTheme);
        btnFriend = findViewById(R.id.btnFriend);
        btnAccount = findViewById(R.id.btnAccount);
        btnNotification = findViewById(R.id.btnNotification);
        bottomNavigation = findViewById(R.id.bottomNavigation);
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());

        btnAccount.setOnClickListener(v -> startActivity(new Intent(this, AccountActivity.class)));
        btnCategory.setOnClickListener(v -> startActivity(new Intent(this, CategoryIncomeActivity.class)));
        btnFriend.setOnClickListener(v -> startActivity(new Intent(this, FriendActivity.class)));
        btnLanguage.setOnClickListener(v -> startActivity(new Intent(this, LanguageSettingActivity.class)));
        btnTheme.setOnClickListener(v -> showThemeDialog());

        btnNotification.setOnClickListener(v -> showFeatureInDevelopmentToast());

        // Xử lý Bottom Navigation
        bottomNavigation.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_home) {
                startActivity(new Intent(this, HomeActivity.class));
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                return true;
            } else if (itemId == R.id.nav_wallet) {
                startActivity(new Intent(this, WalletActivity.class));
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                return true;
            } else if (itemId == R.id.nav_add) {
                startActivity(new Intent(this, AddTransactionActivity.class));
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                return true;
            } else if (itemId == R.id.nav_statistic) {
                startActivity(new Intent(this, StatisticActivity.class));
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                return true;
            } else if (itemId == R.id.nav_settings) {
                return true;
            }
            return false;
        });
    }

    private void showThemeDialog() {
        final String[] themeCodes = {ThemeHelper.LIGHT_MODE, ThemeHelper.DARK_MODE, ThemeHelper.SYSTEM_DEFAULT};
        SharedPreferences prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
        String currentTheme = prefs.getString("theme", ThemeHelper.SYSTEM_DEFAULT);
        int checkedItem = 0;
        if (currentTheme.equals(ThemeHelper.DARK_MODE)) {
            checkedItem = 1;
        } else if (currentTheme.equals(ThemeHelper.SYSTEM_DEFAULT)) {
            checkedItem = 2;
        }

        new AlertDialog.Builder(this)
                .setTitle(R.string.choose_theme)
                .setSingleChoiceItems(R.array.theme_options, checkedItem, (dialog, which) -> {
                    String selectedTheme = themeCodes[which];
                    prefs.edit().putString("theme", selectedTheme).apply();
                    ThemeHelper.applyTheme(selectedTheme);
                    dialog.dismiss();
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void showFeatureInDevelopmentToast() {
        Toast.makeText(this, R.string.feature_in_development, Toast.LENGTH_SHORT).show();
    }
}
