package com.finmate.data.local.database.entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "friends")
public class FriendEntity {

    @PrimaryKey
    @NonNull
    public String friendshipId;

    public String friendUserId;
    public String name;
    public String email;
    public String status;   // PENDING / ACCEPTED / REJECTED / BLOCKED
    public boolean incoming;

    public FriendEntity(@NonNull String friendshipId, String friendUserId, String name, 
                       String email, String status, boolean incoming) {
        this.friendshipId = friendshipId;
        this.friendUserId = friendUserId;
        this.name = name;
        this.email = email;
        this.status = status;
        this.incoming = incoming;
    }
}

