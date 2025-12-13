package com.finmate.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import com.finmate.R;
import com.finmate.data.repository.AuthRepository;
import com.finmate.data.repository.HomeRepository;
import com.finmate.ui.auth.LoginActivity;
import com.finmate.ui.base.BaseActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class SettingsActivity extends BaseActivity {

    @Inject
    AuthRepository authRepository;
    
    @Inject
    HomeRepository homeRepository;

    private ImageView btnBack;
    private LinearLayout btnLanguage, btnCategory, btnTheme, btnFriend, btnAccount, btnNotification, btnSync, btnLogout;
    private BottomNavigationView bottomNavigation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        initViews();
        setupListeners();
        setupBottomNavigation();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        btnLanguage = findViewById(R.id.btnLanguage);
        btnCategory = findViewById(R.id.btnCategory);
        btnTheme = findViewById(R.id.btnTheme);
        btnFriend = findViewById(R.id.btnFriend);
        btnAccount = findViewById(R.id.btnAccount);
        btnNotification = findViewById(R.id.btnNotification);
        btnSync = findViewById(R.id.btnSync);
        btnLogout = findViewById(R.id.btnLogout);
        bottomNavigation = findViewById(R.id.bottomNavigation);
    }

    private void setupListeners() {

        btnBack.setOnClickListener(v -> finish());

        // Ngôn ngữ
        btnLanguage.setOnClickListener(v ->
                startActivity(new Intent(this, LanguageSettingActivity.class))
        );

        // Thể loại
        btnCategory.setOnClickListener(v ->
                startActivity(new Intent(this, CategoryIncomeActivity.class))
        );

        // Giao diện (Theme)
        btnTheme.setOnClickListener(v ->
                startActivity(new Intent(this, ThemeSettingActivity.class))
        );

        // Bạn bè
        btnFriend.setOnClickListener(v ->
                startActivity(new Intent(this, FriendActivity.class))
        );

        // Tài khoản
        btnAccount.setOnClickListener(v ->
                startActivity(new Intent(this, AccountActivity.class))
        );

        // Thông báo
        btnNotification.setOnClickListener(v ->
                startActivity(new Intent(this, NotificationSettingsActivity.class))
        );

        // Đồng bộ ngay
        if (btnSync != null) {
            btnSync.setOnClickListener(v -> performSync());
        }

        // Đăng xuất
        btnLogout.setOnClickListener(v -> showLogoutConfirmation());
    }

    // ===================== BOTTOM NAVIGATION =====================
    private void setupBottomNavigation() {
        if (bottomNavigation == null) return;
        
        bottomNavigation.setSelectedItemId(R.id.nav_settings);

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
                return true; // Đang ở Settings, không cần navigate
            }

            if (intent != null) {
                startActivity(intent);
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                finish(); // Đóng SettingsActivity khi navigate
            }
            return true;
        });
    }

    private void showLogoutConfirmation() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.logout)
                .setMessage(R.string.logout_confirmation)
                .setPositiveButton(android.R.string.yes, (dialog, which) -> performLogout())
                .setNegativeButton(android.R.string.no, null)
                .show();
    }

    private void performSync() {
        Toast.makeText(this, R.string.syncing, Toast.LENGTH_SHORT).show();
        
        // Sync wallets
        homeRepository.fetchWallets(new HomeRepository.DataCallback<java.util.List<com.finmate.data.local.database.entity.WalletEntity>>() {
            @Override
            public void onDataLoaded(java.util.List<com.finmate.data.local.database.entity.WalletEntity> data) {
                Toast.makeText(SettingsActivity.this, R.string.sync_success, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(String message) {
                Toast.makeText(SettingsActivity.this, R.string.sync_failed + ": " + message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void performLogout() {
        authRepository.logout();
        Toast.makeText(this, R.string.logout, Toast.LENGTH_SHORT).show();
        
        // Navigate to LoginActivity and clear task stack
        Intent intent = new Intent(this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
