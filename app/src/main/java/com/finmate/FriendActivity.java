package com.finmate;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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

        // Create sample data
        List<Friend> friendList = new ArrayList<>();
        friendList.add(new Friend(R.drawable.ic_friend, "Bạn còn thiếu một chút tiền từ Nguyễn An"));
        friendList.add(new Friend(R.drawable.ic_friend, "Nguyễn Văn B đã trả lại bạn 50,000 VND"));
        friendList.add(new Friend(R.drawable.ic_friend, "Bạn đã nhắc nhở Trần Thị C trả tiền"));
        friendList.add(new Friend(R.drawable.ic_friend, "Lê Văn D vừa gửi bạn một yêu cầu chia tiền"));
        friendList.add(new Friend(R.drawable.ic_friend, "Bạn và Phạm Thị E đã hòa tiền"));

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

// ===================================
//      DATA CLASS FOR FRIEND
// ===================================
class Friend {
    int avatarResId;
    String status;

    public Friend(int avatarResId, String status) {
        this.avatarResId = avatarResId;
        this.status = status;
    }
}

// ===================================
//      ADAPTER FOR RECYCLERVIEW
// ===================================
class FriendAdapter extends RecyclerView.Adapter<FriendAdapter.FriendViewHolder> {

    private Context context;
    private List<Friend> friendList;

    public FriendAdapter(Context context, List<Friend> friendList) {
        this.context = context;
        this.friendList = friendList;
    }

    @NonNull
    @Override
    public FriendViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_friend, parent, false);
        return new FriendViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FriendViewHolder holder, int position) {
        Friend friend = friendList.get(position);
        holder.ivAvatar.setImageResource(friend.avatarResId);
        holder.tvStatus.setText(friend.status);

        // Handle click on more options icon
        holder.ivMoreOptions.setOnClickListener(v -> {
            Toast.makeText(context, "Tùy chọn cho: " + friend.status, Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    public int getItemCount() {
        return friendList.size();
    }

    static class FriendViewHolder extends RecyclerView.ViewHolder {
        ImageView ivAvatar, ivMoreOptions;
        TextView tvStatus;

        public FriendViewHolder(@NonNull View itemView) {
            super(itemView);
            ivAvatar = itemView.findViewById(R.id.ivFriendAvatar);
            tvStatus = itemView.findViewById(R.id.tvFriendStatus);
            ivMoreOptions = itemView.findViewById(R.id.ivMoreOptions);
        }
    }
}
