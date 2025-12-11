package com.finmate.data.dto;

public class RegisterRequest {
    private String email;
    private String password;
    private String fullName;
    private String avatarUrl;

    public RegisterRequest(String email, String password, String fullName, String avatarUrl) {
        this.email = email;
        this.password = password;
        this.fullName = fullName;
        this.avatarUrl = avatarUrl;
    }

    public String getEmail() { return email; }
    public String getPassword() { return password; }
    public String getFullName() { return fullName; }
    public String getAvatarUrl() { return avatarUrl; }
}
