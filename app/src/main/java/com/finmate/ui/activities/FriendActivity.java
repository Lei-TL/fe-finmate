package com.finmate.ui.activities;

import android.content.Intent;
import android.os.Bundle;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.finmate.R;
import com.finmate.ui.friend.FriendUIModel;
import com.finmate.ui.friend.FriendAdapter;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.finmate.ui.base.BaseActivity;

import java.util.ArrayList;
import java.util.List;

public class FriendActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friend);

        // Nút Back
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        // RecyclerView
        RecyclerView rvFriends = findViewById(R.id.rvFriends);
        rvFriends.setLayoutManager(new LinearLayoutManager(this));

        // Sample data - TODO: Load from database/API
        List<FriendUIModel> friendList = new ArrayList<>();
        // FriendUIModel constructor: (friendshipId, friendUserId, name, email, status, incoming)
        // Tạm thời để trống vì constructor không khớp với sample data cũ

        FriendAdapter adapter = new FriendAdapter(friendList);
        rvFriends.setAdapter(adapter);

        // Bottom Navigation
        BottomNavigationView bottomNav = findViewById(R.id.bottomNavigation);
        bottomNav.setSelectedItemId(R.id.nav_settings);

        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                startActivity(new Intent(this, HomeActivity.class));
            } else if (id == R.id.nav_wallet) {
                startActivity(new Intent(this, com.finmate.ui.home.WalletActivity.class));
            } else if (id == R.id.nav_add) {
                startActivity(new Intent(this, AddTransactionActivity.class));
            } else if (id == R.id.nav_statistic) {
                startActivity(new Intent(this, StatisticActivity.class));
            } else if (id == R.id.nav_settings) {
                startActivity(new Intent(this, SettingsActivity.class));
            }
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            return true;
        });
    }
}
