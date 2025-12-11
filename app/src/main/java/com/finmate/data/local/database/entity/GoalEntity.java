package com.finmate.data.local.database.entity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.finmate.core.offline.OfflineSyncEntity;
import com.finmate.core.offline.PendingAction;
import com.finmate.core.offline.SyncStatus;

/**
 * Saving goal tracked locally.
 */
@Entity(tableName = "goals")
public class GoalEntity implements OfflineSyncEntity {

    @PrimaryKey(autoGenerate = true)
    public int id;

    @Nullable
    public String remoteId;

    @NonNull
    public String name;

    @Nullable
    public String description;

    public double targetAmount;

    @NonNull
    public String targetDate; // ISO date

    @Nullable
    public String linkedWalletName;

    @NonNull
    public String syncStatus;

    @NonNull
    public String pendingAction;

    public long updatedAt;

    public GoalEntity(@NonNull String name,
                      @Nullable String description,
                      double targetAmount,
                      @NonNull String targetDate,
                      @Nullable String linkedWalletName) {
        this.name = name;
        this.description = description;
        this.targetAmount = targetAmount;
        this.targetDate = targetDate;
        this.linkedWalletName = linkedWalletName;
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
