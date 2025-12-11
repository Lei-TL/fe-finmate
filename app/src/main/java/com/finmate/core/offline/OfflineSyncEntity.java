package com.finmate.core.offline;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * Base contract for entities that participate in offline-first syncing.
 * Every entity keeps the server id, current sync status, pending action
 * and a monotonically increasing updatedAt for conflict resolution.
 */
public interface OfflineSyncEntity {

    @Nullable
    String getRemoteId();

    void setRemoteId(@Nullable String remoteId);

    @NonNull
    String getSyncStatus();

    void setSyncStatus(@NonNull String syncStatus);

    @NonNull
    String getPendingAction();

    void setPendingAction(@NonNull String pendingAction);

    long getUpdatedAt();

    void setUpdatedAt(long updatedAt);
}


