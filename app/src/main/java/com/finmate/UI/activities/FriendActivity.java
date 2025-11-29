package com.finmate.UI.activities;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.finmate.R;
import com.finmate.UI.models.FriendUIModel; // Import UI Model mới
import com.finmate.adapters.FriendAdapter;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.List;

public class FriendActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friend);

        // BACK
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        // SETUP RECYCLER VIEW
        RecyclerView rvFriends = findViewById(R.id.rvFriends);
        rvFriends.setLayoutManager(new LinearLayoutManager(this));

        // Create sample data with the new UI Model
        List<FriendUIModel> friendList = new ArrayList<>();
        friendList.add(new FriendUIModel(R.drawable.ic_friend, "Bạn còn thiếu một chút tiền từ Nguyễn An"));
        friendList.add(new FriendUIModel(R.drawable.ic_friend, "Nguyễn Văn B đã trả lại bạn 50,000 VND"));
        friendList.add(new FriendUIModel(R.drawable.ic_friend, "Bạn đã nhắc nhở Trần Thị C trả tiền"));
        friendList.add(new FriendUIModel(R.drawable.ic_friend, "Lê Văn D vừa gửi bạn một yêu cầu chia tiền"));
        friendList.add(new FriendUIModel(R.drawable.ic_friend, "Bạn và Phạm Thị E đã hòa tiền"));

        // Set adapter
        FriendAdapter adapter = new FriendAdapter(this, friendList);
        rvFriends.setAdapter(adapter);

        // BOTTOM NAVIGATION
        BottomNavigationView bottomNav = findViewById(R.id.bottomNavigation);
        bottomNav.setSelectedItemId(R.id.nav_settings); // Đặt mục Cài đặt được chọn

        bottomNav.setOnItemSelectedListener(item -> {
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
                // Nhấn vào Cài đặt khi đang ở màn hình con của nó -> quay về Cài đặt
                startActivity(new Intent(this, SettingsActivity.class));
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                return true;
            }
            return false;
        });
    }
}
