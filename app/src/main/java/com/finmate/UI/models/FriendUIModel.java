package com.finmate.UI.models;

/**
 * UI Model cho một người bạn (Friend)
 * Lớp này chỉ dùng cho Presentation Layer (UI).
 * Không phụ thuộc Retrofit, không phụ thuộc Database.
 */
public class FriendUIModel {

    private final int avatarResId;   // R.drawable.xxx
    private final String status;     // Trạng thái hiển thị

    public FriendUIModel(int avatarResId, String status) {
        this.avatarResId = avatarResId;
        this.status = status;
    }

    public int getAvatarResId() {
        return avatarResId;
    }

    public String getStatus() {
        return status;
    }
}
