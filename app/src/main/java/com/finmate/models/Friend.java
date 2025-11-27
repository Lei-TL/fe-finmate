package com.finmate.models;

// ===================================
//      DATA CLASS FOR FRIEND
// ===================================
// Thêm "public" để lớp này có thể được truy cập từ các package khác
public class Friend {
    // Các trường cũng cần "public" để Adapter có thể truy cập
    public int avatarResId;
    public String status;

    public Friend(int avatarResId, String status) {
        this.avatarResId = avatarResId;
        this.status = status;
    }
}
