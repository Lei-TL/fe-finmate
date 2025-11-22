package com.finmate;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class SettingsActivity extends AppCompatActivity {

    ImageView btnBack;
    LinearLayout btnLanguage, btnCategory, btnTheme, btnFriend, btnAccount, btnNotification;
    BottomNavigationView bottomNavigation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        // ÁNH XẠ
        btnBack = findViewById(R.id.btnBack);
        btnLanguage = findViewById(R.id.btnLanguage);
        btnCategory = findViewById(R.id.btnCategory);
        btnTheme = findViewById(R.id.btnTheme);
        btnFriend = findViewById(R.id.btnFriend);
        btnAccount = findViewById(R.id.btnAccount);
        btnNotification = findViewById(R.id.btnNotification);
        bottomNavigation = findViewById(R.id.bottomNavigation);

        // Chọn tab Cài đặt
        bottomNavigation.setSelectedItemId(R.id.nav_settings);

        // Nút quay lại
        btnBack.setOnClickListener(v -> finish());

        // Xử lý các mục cài đặt
        btnLanguage.setOnClickListener(v -> show("Ngôn ngữ"));
        btnCategory.setOnClickListener(v -> show("Quản lý thể loại"));
        btnTheme.setOnClickListener(v -> show("Giao diện hệ thống"));
        btnFriend.setOnClickListener(v -> startActivity(new Intent(this, FriendActivity.class)));
        btnAccount.setOnClickListener(v -> show("Tài khoản"));
        btnNotification.setOnClickListener(v -> show("Thông báo"));

        // ====== BOTTOM NAVIGATION ======
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
                return true; // đang ở đây
            }
            return false;
        });
    }

    private void show(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }
}
