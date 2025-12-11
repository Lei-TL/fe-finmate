package com.finmate.core.network;

import androidx.annotation.Nullable;

import java.util.concurrent.atomic.AtomicBoolean;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class SyncOnlineStatusManager {

    private static final long BASE_BLOCK_DURATION_MS = 30_000; // 30 seconds
    private static final int MAX_BACKOFF_LEVEL = 3;

    private final AtomicBoolean blocked = new AtomicBoolean(false);
    private volatile long blockUntilTimeMillis = 0L;
    private volatile int backoffLevel = 0;

    @Inject
    public SyncOnlineStatusManager() {
    }

    public boolean canSyncNow() {
        if (!blocked.get()) {
            return true;
        }

        if (System.currentTimeMillis() >= blockUntilTimeMillis) {
            blocked.set(false);
            backoffLevel = 0; // Reset backoff when block expires
            return true;
        }

        return false;
    }

    public void onSyncSuccess() {
        blocked.set(false);
        blockUntilTimeMillis = 0L;
        backoffLevel = 0;
    }

    public void onSyncError(@Nullable Integer httpCode, @Nullable Throwable t) {
        boolean shouldBlock =
                t instanceof java.io.IOException ||
                (httpCode != null && httpCode >= 500 && httpCode <= 599);

        if (!shouldBlock) {
            return; // Not a network/server error, don't block global sync
        }

        if (backoffLevel < MAX_BACKOFF_LEVEL) {
            backoffLevel++;
        }

        long blockDuration = BASE_BLOCK_DURATION_MS * backoffLevel;
        blocked.set(true);
        blockUntilTimeMillis = System.currentTimeMillis() + blockDuration;
    }
}
