package com.finmate.UI.activities;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.finmate.R;
import com.finmate.UI.models.FriendUIModel;
import com.finmate.adapters.FriendAdapter;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.List;

public class FriendActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friend);

        // Back button
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        // RecyclerView setup
        RecyclerView rvFriends = findViewById(R.id.rvFriends);
        rvFriends.setLayoutManager(new LinearLayoutManager(this));

        // Sample data
        List<FriendUIModel> friendList = new ArrayList<>();
        friendList.add(new FriendUIModel(R.drawable.ic_friend, "Bạn còn thiếu một chút tiền từ Nguyễn An"));
        friendList.add(new FriendUIModel(R.drawable.ic_friend, "Nguyễn Văn B đã trả lại bạn 50,000 VND"));
        friendList.add(new FriendUIModel(R.drawable.ic_friend, "Bạn đã nhắc nhở Trần Thị C trả tiền"));
        friendList.add(new FriendUIModel(R.drawable.ic_friend, "Lê Văn D vừa gửi bạn một yêu cầu chia tiền"));
        friendList.add(new FriendUIModel(R.drawable.ic_friend, "Bạn và Phạm Thị E đã hòa tiền"));

        // Set adapter
        FriendAdapter adapter = new FriendAdapter(this, friendList);
        rvFriends.setAdapter(adapter);

        // Bottom Navigation
        BottomNavigationView bottomNav = findViewById(R.id.bottomNavigation);
        bottomNav.setSelectedItemId(R.id.nav_settings);

        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                startActivity(new Intent(this, HomeActivity.class));
            } else if (id == R.id.nav_wallet) {
                startActivity(new Intent(this, WalletActivity.class));
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
