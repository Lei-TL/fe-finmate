package com.finmate.models;

public class Friend {
    private int avatarResId;
    private String name;

    public Friend(int avatarResId, String name) {
        this.avatarResId = avatarResId;
        this.name = name;
    }

    public int getAvatarResId() {
        return avatarResId;
    }

    public String getName() {
        return name;
    }
}
