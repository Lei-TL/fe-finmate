package com.finmate.data.local.database.entity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import com.finmate.core.offline.OfflineSyncEntity;
import com.finmate.core.offline.PendingAction;
import com.finmate.core.offline.SyncStatus;

/**
 * Entity lưu dữ liệu thô (domain model), không format cho UI.
 * - amount: double (số tiền gốc, không format)
 * - occurredAt: String (ISO datetime từ BE, hoặc ISO format khi tạo local)
 */
@Entity(tableName = "transactions")
public class TransactionEntity implements OfflineSyncEntity {

    @PrimaryKey(autoGenerate = true)
    public int id;

    @Nullable
    public String remoteId;

    @NonNull
    public String name;
    @NonNull
    public String categoryName;
    @NonNull
    public double amount;  // Raw value for calculations
    @NonNull
    public String walletName;
    @NonNull
    public String occurredAt;  // ISO datetime string

    @NonNull
    public String type; // INCOME / EXPENSE / TRANSFER

    @NonNull
    public String syncStatus;

    @NonNull
    public String pendingAction;

    public long updatedAt;

    // Constructor for creating a new local (pending) transaction
    public TransactionEntity(@NonNull String name, @NonNull String categoryName, double amount, @NonNull String walletName, @NonNull String occurredAt) {
        this.name = name;
        this.categoryName = categoryName;
        this.amount = amount;
        this.walletName = walletName;
        this.occurredAt = occurredAt;
        this.type = "EXPENSE";
        this.syncStatus = SyncStatus.PENDING_CREATE;
        this.pendingAction = PendingAction.CREATE;
        this.updatedAt = System.currentTimeMillis();
    }

    // Constructor for creating a synced transaction from a server response
    @Ignore
    public TransactionEntity(@NonNull String remoteId, @NonNull String name, @NonNull String categoryName, double amount, @NonNull String walletName, @NonNull String occurredAt, @NonNull String type) {
        this.remoteId = remoteId;
        this.name = name;
        this.categoryName = categoryName;
        this.amount = amount;
        this.walletName = walletName;
        this.occurredAt = occurredAt;
        this.type = type;
        this.syncStatus = SyncStatus.SYNCED;
        this.pendingAction = PendingAction.NONE;
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

    @NonNull
    public String getName() {
        return name;
    }

    public void setName(@NonNull String name) {
        this.name = name;
    }

    @NonNull
    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(@NonNull String categoryName) {
        this.categoryName = categoryName;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    @NonNull
    public String getWalletName() {
        return walletName;
    }

    public void setWalletName(@NonNull String walletName) {
        this.walletName = walletName;
    }

    @NonNull
    public String getType() {
        return type;
    }

    public void setType(@NonNull String type) {
        this.type = type;
    }

    @NonNull
    public String getOccurredAt() {
        return occurredAt;
    }

    public void setOccurredAt(@NonNull String occurredAt) {
        this.occurredAt = occurredAt;
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
