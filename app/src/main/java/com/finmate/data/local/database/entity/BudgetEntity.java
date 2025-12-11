package com.finmate.data.local.database.entity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.finmate.core.offline.OfflineSyncEntity;
import com.finmate.core.offline.PendingAction;
import com.finmate.core.offline.SyncStatus;

/**
 * Local-first budget. Wallet/category are optional (by name) to match current schema.
 */
@Entity(tableName = "budgets")
public class BudgetEntity implements OfflineSyncEntity {

    @PrimaryKey(autoGenerate = true)
    public int id;

    @Nullable
    public String remoteId;

    @Nullable
    public String walletName;

    @Nullable
    public String categoryName;

    @NonNull
    public String period; // MONTH or WEEK

    public double amountLimit;

    @NonNull
    public String syncStatus;

    @NonNull
    public String pendingAction;

    public long updatedAt;

    public BudgetEntity(@Nullable String walletName,
                        @Nullable String categoryName,
                        @NonNull String period,
                        double amountLimit) {
        this.walletName = walletName;
        this.categoryName = categoryName;
        this.period = period;
        this.amountLimit = amountLimit;
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
