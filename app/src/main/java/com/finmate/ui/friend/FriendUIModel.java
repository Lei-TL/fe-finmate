package com.finmate.ui.friend;

import com.finmate.data.dto.FriendResponse;

public class FriendUIModel {

    private String friendshipId;
    private String friendUserId;
    private String name;
    private String email;
    private String status;   // PENDING / ACCEPTED / REJECTED / BLOCKED
    private boolean incoming;

    public FriendUIModel(String friendshipId,
                         String friendUserId,
                         String name,
                         String email,
                         String status,
                         boolean incoming) {
        this.friendshipId = friendshipId;
        this.friendUserId = friendUserId;
        this.name = name;
        this.email = email;
        this.status = status;
        this.incoming = incoming;
    }

    public String getFriendshipId() {
        return friendshipId;
    }

    public String getFriendUserId() {
        return friendUserId;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getStatus() {
        return status;
    }

    public boolean isIncoming() {
        return incoming;
    }

    public static FriendUIModel fromDto(FriendResponse dto) {
        return new FriendUIModel(
                dto.getId(),
                dto.getFriendUserId(),
                dto.getFriendName(),
                dto.getFriendEmail(),
                dto.getStatus(),
                dto.isIncoming()
        );
    }


}
