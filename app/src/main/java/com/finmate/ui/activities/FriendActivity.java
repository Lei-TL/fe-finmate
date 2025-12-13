package com.finmate.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

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

        // Sample data
        List<FriendUIModel> friendList = new ArrayList<>();
        friendList.add(new FriendUIModel("1", "user1", "Nguyễn An", "nguyenan@example.com", "ACCEPTED", false));
        friendList.add(new FriendUIModel("2", "user2", "Nguyễn Văn B", "nguyenvanb@example.com", "ACCEPTED", false));
        friendList.add(new FriendUIModel("3", "user3", "Trần Thị C", "tranthic@example.com", "ACCEPTED", false));
        friendList.add(new FriendUIModel("4", "user4", "Lê Văn D", "levand@example.com", "PENDING", true));
        friendList.add(new FriendUIModel("5", "user5", "Phạm Thị E", "phamthie@example.com", "ACCEPTED", false));

        FriendAdapter adapter = new FriendAdapter(friendList);
        rvFriends.setAdapter(adapter);

        // Setup empty state
        View layoutEmptyState = findViewById(R.id.layoutEmptyState);
        if (layoutEmptyState != null) {
            TextView tvEmptyTitle = layoutEmptyState.findViewById(R.id.tvEmptyTitle);
            TextView tvEmptyHint = layoutEmptyState.findViewById(R.id.tvEmptyHint);
            if (tvEmptyTitle != null) {
                tvEmptyTitle.setText(R.string.no_friends);
            }
            if (tvEmptyHint != null) {
                tvEmptyHint.setText(R.string.no_friends_hint);
            }

            // Show/hide empty state based on data
            if (friendList.isEmpty()) {
                layoutEmptyState.setVisibility(View.VISIBLE);
                rvFriends.setVisibility(View.GONE);
            } else {
                layoutEmptyState.setVisibility(View.GONE);
                rvFriends.setVisibility(View.VISIBLE);
            }
        }

        // Connect New Friend Button
        android.widget.Button btnConnectNewFriend = findViewById(R.id.btnConnectNewFriend);
        if (btnConnectNewFriend != null) {
            btnConnectNewFriend.setOnClickListener(v -> showConnectFriendDialog());
        }

        // Bottom Navigation
        BottomNavigationView bottomNav = findViewById(R.id.bottomNavigation);
        bottomNav.setSelectedItemId(R.id.nav_settings);

        bottomNav.setOnItemSelectedListener(item -> {
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
                return true; // Đang ở Settings (FriendActivity là sub-screen của Settings), không cần navigate
            }

            if (intent != null) {
                startActivity(intent);
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            }
            return true;
        });
    }
    
    private void showConnectFriendDialog() {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder.setTitle(R.string.connect_new_friend);
        
        // Create input field
        final android.widget.EditText input = new android.widget.EditText(this);
        input.setHint(R.string.email_or_friend_name);
        input.setInputType(android.text.InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        builder.setView(input);
        
        builder.setPositiveButton(R.string.connect, (dialog, which) -> {
            String emailOrName = input.getText().toString().trim();
            if (emailOrName.isEmpty()) {
                android.widget.Toast.makeText(this, R.string.fill_all_info, android.widget.Toast.LENGTH_SHORT).show();
                return;
            }
            // TODO: Call API to send friend request
            android.widget.Toast.makeText(this, "Đã gửi lời mời kết bạn đến " + emailOrName, android.widget.Toast.LENGTH_SHORT).show();
        });
        
        builder.setNegativeButton(R.string.cancel, null);
        builder.show();
    }
}
