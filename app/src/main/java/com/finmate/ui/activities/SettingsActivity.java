package com.finmate.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.finmate.R;
import com.finmate.ui.base.BaseActivity;
import com.finmate.ui.dialogs.ThemeDialog;

public class SettingsActivity extends BaseActivity {

    private ImageView btnBack;
    private LinearLayout btnLanguage, btnCategory, btnTheme, btnFriend, btnAccount, btnNotification;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        initViews();
        setupListeners();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        btnLanguage = findViewById(R.id.btnLanguage);
        btnCategory = findViewById(R.id.btnCategory);
        btnTheme = findViewById(R.id.btnTheme);
        btnFriend = findViewById(R.id.btnFriend);
        btnAccount = findViewById(R.id.btnAccount);
        btnNotification = findViewById(R.id.btnNotification);
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
        btnTheme.setOnClickListener(v -> {
            ThemeDialog dialog = new ThemeDialog();
            dialog.show(getSupportFragmentManager(), "theme_dialog");
        });

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
    }
}
