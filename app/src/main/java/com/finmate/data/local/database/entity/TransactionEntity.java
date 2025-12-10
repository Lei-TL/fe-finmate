package com.finmate.data.local.database.entity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

/**
 * Entity lưu dữ liệu thô (domain model), không format cho UI.
 * - amount: double (số tiền gốc, không format)
 * - occurredAt: String (ISO datetime từ BE, hoặc ISO format khi tạo local)
 */
@Entity(tableName = "transactions")
public class TransactionEntity {

    @PrimaryKey(autoGenerate = true)
    public int id;

    @Nullable
    public String remoteId;

    @NonNull
    public String name;
    @NonNull
    public String category;
    @NonNull
    public double amount;  // Changed from String to double - raw value for calculations
    @NonNull
    public String wallet;
    @NonNull
    public String occurredAt;  // Changed from "date" to "occurredAt" - ISO datetime string

    @NonNull
    public String syncStatus;

    @NonNull
    public String pendingAction;

    // Constructor for creating a new local (pending) transaction
    public TransactionEntity(@NonNull String name, @NonNull String category, double amount, @NonNull String wallet, @NonNull String occurredAt) {
        this.name = name;
        this.category = category;
        this.amount = amount;
        this.wallet = wallet;
        this.occurredAt = occurredAt;
        this.syncStatus = SyncStatus.PENDING_CREATE;
        this.pendingAction = PendingAction.CREATE;
    }

    // Constructor for creating a synced transaction from a server response
    @Ignore
    public TransactionEntity(@NonNull String remoteId, @NonNull String name, @NonNull String category, double amount, @NonNull String wallet, @NonNull String occurredAt) {
        this.remoteId = remoteId;
        this.name = name;
        this.category = category;
        this.amount = amount;
        this.wallet = wallet;
        this.occurredAt = occurredAt;
        this.syncStatus = SyncStatus.SYNCED;
        this.pendingAction = PendingAction.NONE;
    }
}
