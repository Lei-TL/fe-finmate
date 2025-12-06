package com.finmate.data.dto;

import com.google.gson.annotations.SerializedName;

public class FriendResponse {

    // id của bản ghi Friendship
    @SerializedName("id")
    private String id;

    @SerializedName("friendUserId")
    private String friendUserId;

    @SerializedName("friendName")
    private String friendName;

    @SerializedName("friendEmail")
    private String friendEmail;

    @SerializedName("status")
    private String status;   // "PENDING", "ACCEPTED", ...

    @SerializedName("incoming")
    private boolean incoming;

    @SerializedName("createdAt")
    private String createdAt;

    @SerializedName("updatedAt")
    private String updatedAt;

    public String getId() {
        return id;
    }

    public String getFriendUserId() {
        return friendUserId;
    }

    public String getFriendName() {
        return friendName;
    }

    public String getFriendEmail() {
        return friendEmail;
    }

    public String getStatus() {
        return status;
    }

    public boolean isIncoming() {
        return incoming;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }
}
