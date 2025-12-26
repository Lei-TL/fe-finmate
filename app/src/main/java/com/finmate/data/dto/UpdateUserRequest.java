package com.finmate.data.dto;

public class UpdateUserRequest {
    private String fullName;
    private String avatarUrl;
    private String birthday; // Format: yyyy-MM-dd hoặc null
    private String note; // Mô tả bản thân

    public UpdateUserRequest() {
    }

    public UpdateUserRequest(String fullName, String avatarUrl, String birthday, String note) {
        this.fullName = fullName;
        this.avatarUrl = avatarUrl;
        this.birthday = birthday;
        this.note = note;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    public String getBirthday() {
        return birthday;
    }

    public void setBirthday(String birthday) {
        this.birthday = birthday;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }
}



