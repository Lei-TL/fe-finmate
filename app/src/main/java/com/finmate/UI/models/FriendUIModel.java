package com.finmate.UI.models;

public class FriendUIModel {

    private int avatarResId;
    private String status;

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
    