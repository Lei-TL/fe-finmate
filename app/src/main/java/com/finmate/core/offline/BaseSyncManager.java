package com.finmate.core.offline;

import androidx.annotation.Nullable;

import com.finmate.core.network.SyncOnlineStatusManager;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Generic sequential sync runner that respects online/backoff status.
 * Subclasses implement performSync to push a single item.
 */
public abstract class BaseSyncManager<T extends OfflineSyncEntity> {

    private final SyncOnlineStatusManager onlineStatusManager;
    private final AtomicBoolean isSyncing = new AtomicBoolean(false);

    protected BaseSyncManager(SyncOnlineStatusManager onlineStatusManager) {
        this.onlineStatusManager = onlineStatusManager;
    }

    public void syncPending(List<T> pendingItems) {
        if (!onlineStatusManager.canSyncNow() || !isSyncing.compareAndSet(false, true)) {
            return;
        }
        if (pendingItems == null || pendingItems.isEmpty()) {
            onlineStatusManager.onSyncSuccess();
            isSyncing.set(false);
            return;
        }
        processSequentially(pendingItems, 0);
    }

    private void processSequentially(List<T> items, int index) {
        if (index >= items.size()) {
            onlineStatusManager.onSyncSuccess();
            isSyncing.set(false);
            return;
        }

        performSync(items.get(index), new SyncCallback() {
            @Override
            public void onSuccess() {
                processSequentially(items, index + 1);
            }

            @Override
            public void onSkip() {
                processSequentially(items, index + 1);
            }

            @Override
            public void onError(@Nullable Integer httpCode, @Nullable Throwable t) {
                onlineStatusManager.onSyncError(httpCode, t);
                isSyncing.set(false);
            }
        });
    }

    protected abstract void performSync(T item, SyncCallback callback);

    public interface SyncCallback {
        void onSuccess();

        void onSkip();

        void onError(@Nullable Integer httpCode, @Nullable Throwable t);
    }
}


