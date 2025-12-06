package com.finmate.data.dto;

import com.google.gson.annotations.SerializedName;

public class FriendRequest {

    @SerializedName("targetEmail")
    private String targetEmail;

    public FriendRequest(String targetEmail) {
        this.targetEmail = targetEmail;
    }

    public String getTargetEmail() {
        return targetEmail;
    }
}
