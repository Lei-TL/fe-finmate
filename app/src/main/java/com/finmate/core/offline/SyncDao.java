package com.finmate.core.offline;

import java.util.List;

/**
 * Marker interface for DAOs that expose pending items for syncing.
 */
public interface SyncDao<T extends OfflineSyncEntity> {
    List<T> getPendingForSync();
}


