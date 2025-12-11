package com.finmate.data.local.database.entity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.finmate.core.offline.OfflineSyncEntity;
import com.finmate.core.offline.PendingAction;
import com.finmate.core.offline.SyncStatus;

@Entity(tableName = "wallets")
public class WalletEntity implements OfflineSyncEntity {

    @PrimaryKey(autoGenerate = true)
    public int id;

    @Nullable
    public String remoteId;

    public String name;
    public String currency;
    public double balanceCached;
    public int iconRes;

    @NonNull
    public String syncStatus;
    @NonNull
    public String pendingAction;
    public long updatedAt;

    public WalletEntity(String name, String currency, double balanceCached, int iconRes) {
        this.name = name;
        this.currency = currency;
        this.balanceCached = balanceCached;
        this.iconRes = iconRes;
        this.syncStatus = SyncStatus.PENDING_CREATE;
        this.pendingAction = PendingAction.CREATE;
        this.updatedAt = System.currentTimeMillis();
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getBalanceCached() {
        return balanceCached;
    }

    public void setBalanceCached(double balanceCached) {
        this.balanceCached = balanceCached;
    }

    public int getIconRes() {
        return iconRes;
    }

    public void setIconRes(int iconRes) {
        this.iconRes = iconRes;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
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
