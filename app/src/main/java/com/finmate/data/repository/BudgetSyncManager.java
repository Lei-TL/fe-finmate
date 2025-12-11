package com.finmate.data.repository;

import androidx.annotation.Nullable;

import com.finmate.core.network.SyncOnlineStatusManager;
import com.finmate.core.offline.BaseSyncManager;
import com.finmate.core.offline.PendingAction;
import com.finmate.core.offline.SyncStatus;
import com.finmate.data.local.database.entity.BudgetEntity;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class BudgetSyncManager extends BaseSyncManager<BudgetEntity> {

    private final BudgetLocalRepository localRepository;

    @Inject
    public BudgetSyncManager(
            BudgetLocalRepository localRepository,
            SyncOnlineStatusManager onlineStatusManager) {
        super(onlineStatusManager);
        this.localRepository = localRepository;
    }

    public void syncPendingBudgets() {
        localRepository.getPendingForSync(new BudgetLocalRepository.Callback() {
            @Override
            public void onResult(java.util.List<BudgetEntity> list) {
                syncPending(list);
            }
        });
    }

    @Override
    protected void performSync(BudgetEntity item, SyncCallback callback) {
        // TODO: integrate remote budget API
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
