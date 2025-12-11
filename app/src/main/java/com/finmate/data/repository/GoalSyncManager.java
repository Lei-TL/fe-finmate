package com.finmate.data.repository;

import androidx.annotation.Nullable;

import com.finmate.core.network.SyncOnlineStatusManager;
import com.finmate.core.offline.BaseSyncManager;
import com.finmate.core.offline.PendingAction;
import com.finmate.core.offline.SyncStatus;
import com.finmate.data.local.database.entity.GoalEntity;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class GoalSyncManager extends BaseSyncManager<GoalEntity> {

    private final GoalLocalRepository localRepository;

    @Inject
    public GoalSyncManager(
            GoalLocalRepository localRepository,
            SyncOnlineStatusManager onlineStatusManager) {
        super(onlineStatusManager);
        this.localRepository = localRepository;
    }

    public void syncPendingGoals() {
        localRepository.getPendingForSync(new GoalLocalRepository.Callback() {
            @Override
            public void onResult(java.util.List<GoalEntity> list) {
                syncPending(list);
            }
        });
    }

    @Override
    protected void performSync(GoalEntity item, SyncCallback callback) {
        // TODO: integrate remote goal API
        if (item.getPendingAction().equals(PendingAction.DELETE)) {
            localRepository.deleteImmediate(item);
            callback.onSuccess();
            return;
        }
        item.setSyncStatus(SyncStatus.SYNCED);
        item.setPendingAction(PendingAction.NONE);
        localRepository.update(item);
        callback.onSuccess();
    }
}
