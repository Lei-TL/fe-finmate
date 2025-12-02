package com.finmate.UI.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.finmate.R;
import com.finmate.UI.dialogs.ThemeDialog;
import com.finmate.UI.activities.NotificationSettingsActivity;


public class SettingsActivity extends AppCompatActivity {

    ImageView btnBack;
    LinearLayout btnLanguage, btnCategory, btnTheme, btnFriend, btnAccount, btnNotification;

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

        // Nút quay lại
        btnBack.setOnClickListener(v -> finish());

        // ===================== XỬ LÝ CÁC NÚT ========================

        // 1. Ngôn ngữ
        btnLanguage.setOnClickListener(v ->
                startActivity(new Intent(this, LanguageSettingActivity.class))
        );

        // 2. Quản lý thể loại
        btnCategory.setOnClickListener(v ->
                startActivity(new Intent(this, CategoryIncomeActivity.class))
        );

        // 3. Giao diện hệ thống (mở BottomSheet / Dialog)
        btnTheme.setOnClickListener(v -> {
            ThemeDialog dialog = new ThemeDialog();
            dialog.show(getSupportFragmentManager(), "theme_dialog");
        });

        // 4. Bạn bè
        btnFriend.setOnClickListener(v ->
                startActivity(new Intent(this, FriendActivity.class))
        );

        // 5. Tài khoản
        btnAccount.setOnClickListener(v ->
                startActivity(new Intent(this, AccountActivity.class))
        );

        // 6. Thông báo
        btnNotification.setOnClickListener(v ->
                startActivity(new Intent(this, NotificationSettingsActivity.class))
        );
    }
}
