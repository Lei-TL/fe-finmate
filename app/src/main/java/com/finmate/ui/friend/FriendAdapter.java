package com.finmate.ui.friend;

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

import java.util.ArrayList;
import java.util.List;


public class FriendAdapter extends RecyclerView.Adapter<FriendAdapter.FriendViewHolder> {

    private final List<FriendUIModel> friendList = new ArrayList<>();

    public FriendAdapter(List<FriendUIModel> initialData) {
        if (initialData != null) {
            friendList.addAll(initialData);
        }
    }

    @NonNull
    @Override
    public FriendViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        View view = LayoutInflater.from(context).inflate(R.layout.item_friend, parent, false);
        return new FriendViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FriendViewHolder holder, int position) {
        FriendUIModel friend = friendList.get(position);

        // Avatar tạm thời dùng icon chung
        holder.ivAvatar.setImageResource(R.drawable.taikhoan); // đổi icon nếu anh có

        holder.tvStatus.setText(friend.getStatus());

        holder.ivMoreOptions.setOnClickListener(v -> {
            if (actionListener != null) {
                // Ví dụ: mặc định là remove, hoặc show bottom sheet rồi gọi actionListener
                actionListener.onRemove(friend);
            } else {
                Toast.makeText(
                        holder.itemView.getContext(),
                        "Tùy chọn cho: " + friend.getStatus(),
                        Toast.LENGTH_SHORT
                ).show();
            }
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

    public void updateData(List<FriendUIModel> newData) {
        friendList.clear();
        if (newData != null) {
            friendList.addAll(newData);
        }
        notifyDataSetChanged();
    }

    public interface OnActionListener {
        void onRemove(FriendUIModel item);
        void onAccept(FriendUIModel item);
        void onReject(FriendUIModel item);
    }

    private OnActionListener actionListener;

    public void setOnActionListener(OnActionListener listener) {
        this.actionListener = listener;
    }
}
