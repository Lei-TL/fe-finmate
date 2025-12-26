package com.finmate.data.local.database.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * Entity để lưu các thay đổi cần sync lên server.
 * SyncManager sẽ đọc bảng này và retry sync khi có mạng.
 */
@Entity(tableName = "pending_sync")
public class PendingSyncEntity {

    @PrimaryKey(autoGenerate = true)
    public long id;

    /**
     * Loại entity: "TRANSACTION", "WALLET", "CATEGORY", "FRIEND"
     */
    public String entityType;

    /**
     * Loại operation: "CREATE", "UPDATE", "DELETE"
     */
    public String operationType;

    /**
     * ID của entity trong local DB
     */
    public int localEntityId;

    /**
     * JSON data của entity để sync
     */
    public String entityData;

    /**
     * Số lần retry đã thử
     */
    public int retryCount = 0;

    /**
     * Timestamp khi tạo pending sync
     */
    public long createdAt;

    public PendingSyncEntity(String entityType, String operationType, int localEntityId, String entityData) {
        this.entityType = entityType;
        this.operationType = operationType;
        this.localEntityId = localEntityId;
        this.entityData = entityData;
        this.createdAt = System.currentTimeMillis();
    }
}

