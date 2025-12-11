package com.finmate.data.local.database.entity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.finmate.core.offline.OfflineSyncEntity;
import com.finmate.core.offline.PendingAction;
import com.finmate.core.offline.SyncStatus;

@Entity(tableName = "friends")
public class FriendEntity implements OfflineSyncEntity {

    @PrimaryKey(autoGenerate = true)
    public int id;

    @Nullable
    public String remoteId; // friendship id from server

    @Nullable
    public String friendUserId;

    @Nullable
    public String name;

    @Nullable
    public String email;

    @Nullable
    public String avatar;

    @NonNull
    public String status; // PENDING / ACCEPTED / REJECTED

    @NonNull
    public String syncStatus;

    @NonNull
    public String pendingAction;

    public long updatedAt;

    public FriendEntity(@NonNull String status) {
        this.status = status;
        this.syncStatus = SyncStatus.PENDING_CREATE;
        this.pendingAction = PendingAction.CREATE;
        this.updatedAt = System.currentTimeMillis();
    }

    @Nullable
    @Override
    public String getRemoteId() {
        return remoteId;
    }

    @Override
    public void setRemoteId(@Nullable String remoteId) {
        this.remoteId = remoteId;
    }

    @NonNull
    @Override
    public String getSyncStatus() {
        return syncStatus;
    }

    @Override
    public void setSyncStatus(@NonNull String syncStatus) {
        this.syncStatus = syncStatus;
    }

    @NonNull
    @Override
    public String getPendingAction() {
        return pendingAction;
    }

    @Override
    public void setPendingAction(@NonNull String pendingAction) {
        this.pendingAction = pendingAction;
    }

    @Override
    public long getUpdatedAt() {
        return updatedAt;
    }

    @Override
    public void setUpdatedAt(long updatedAt) {
        this.updatedAt = updatedAt;
    }
}

