package com.finmate.data.local.database.entity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.finmate.core.offline.OfflineSyncEntity;
import com.finmate.core.offline.PendingAction;
import com.finmate.core.offline.SyncStatus;

@Entity(tableName = "shared_transactions")
public class SharedTransactionEntity implements OfflineSyncEntity {

    @PrimaryKey(autoGenerate = true)
    public int id;

    @Nullable
    public String remoteId;

    @Nullable
    public String groupId;

    @Nullable
    public String groupName;

    public double totalAmount;

    @Nullable
    public String payerUserId;

    public boolean splitEqually;

    public int memberCount;

    @Nullable
    public String transactionRemoteId; // link to normal transaction if available

    @NonNull
    public String createdAtIso;

    @NonNull
    public String syncStatus;

    @NonNull
    public String pendingAction;

    public long updatedAt;

    public SharedTransactionEntity(@NonNull String createdAtIso, double totalAmount) {
        this.createdAtIso = createdAtIso;
        this.totalAmount = totalAmount;
        this.syncStatus = SyncStatus.SYNCED; // only created online for v1
        this.pendingAction = PendingAction.NONE;
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

