package com.finmate.UI.models;

public class UserUIModel {

    private final String name;
    private final String email;
    private final String avatarUrl; // hoặc int avatarRes nếu dùng local

    public UserUIModel(String name, String email, String avatarUrl) {
        this.name = name;
        this.email = email;
        this.avatarUrl = avatarUrl;
    }

    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getAvatarUrl() { return avatarUrl; }
}
