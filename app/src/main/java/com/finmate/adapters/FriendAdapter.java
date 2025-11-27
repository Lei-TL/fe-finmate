package com.finmate.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.finmate.R;
import com.finmate.models.Friend;

import java.util.List;

// ===================================
//      ADAPTER FOR RECYCLERVIEW
// ===================================
// Thêm "public" để lớp này có thể được truy cập từ các package khác
public class FriendAdapter extends RecyclerView.Adapter<FriendAdapter.FriendViewHolder> {

    private Context context;
    private List<Friend> friendList;

    public FriendAdapter(Context context, List<Friend> friendList) {
        this.context = context;
        this.friendList = friendList;
    }

    @NonNull
    @Override
    public FriendAdapter.FriendViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_friend, parent, false);
        return new FriendAdapter.FriendViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FriendAdapter.FriendViewHolder holder, int position) {
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
