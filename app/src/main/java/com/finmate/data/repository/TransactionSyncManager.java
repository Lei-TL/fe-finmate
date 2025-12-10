package com.finmate.data.repository;

import androidx.annotation.Nullable;

import com.finmate.core.network.SyncOnlineStatusManager;
import com.finmate.data.local.database.entity.PendingAction;
import com.finmate.data.local.database.entity.SyncStatus;
import com.finmate.data.local.database.entity.TransactionEntity;
import com.finmate.data.remote.api.ApiCallback;
import com.finmate.data.dto.TransactionResponse;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class TransactionSyncManager {

    private final TransactionRemoteRepository remoteRepository;
    private final TransactionLocalRepository localRepository;
    private final SyncOnlineStatusManager onlineStatusManager;

    private final AtomicBoolean isSyncing = new AtomicBoolean(false);

    @Inject
    public TransactionSyncManager(
            TransactionRemoteRepository remoteRepository,
            TransactionLocalRepository localRepository,
            SyncOnlineStatusManager onlineStatusManager) {
        this.remoteRepository = remoteRepository;
        this.localRepository = localRepository;
        this.onlineStatusManager = onlineStatusManager;
    }

    public void syncPendingTransactions() {
        if (!onlineStatusManager.canSyncNow() || !isSyncing.compareAndSet(false, true)) {
            return;
        }

        localRepository.getPendingTransactions(pendingList -> {
            if (pendingList == null || pendingList.isEmpty()) {
                onlineStatusManager.onSyncSuccess();
                isSyncing.set(false);
                return;
            }
            processPendingListSequentially(pendingList, 0);
        });
    }

    private void processPendingListSequentially(List<TransactionEntity> list, int index) {
        if (index >= list.size()) {
            onlineStatusManager.onSyncSuccess();
            isSyncing.set(false);
            return;
        }

        TransactionEntity tx = list.get(index);
        switch (tx.pendingAction) {
            case PendingAction.CREATE:
                syncCreate(tx, list, index);
                break;
            case PendingAction.UPDATE:
                syncUpdate(tx, list, index);
                break;
            case PendingAction.DELETE:
                syncDelete(tx, list, index);
                break;
            default:
                processPendingListSequentially(list, index + 1);
                break;
        }
    }

    private void syncCreate(TransactionEntity tx, List<TransactionEntity> list, int index) {
        remoteRepository.createFromLocal(tx, new ApiCallback<TransactionResponse>() {
            @Override
            public void onSuccess(TransactionResponse data) {
                TransactionEntity synced = mapResponseToEntity(data);
                localRepository.markAsSyncedAfterCreate(tx, synced);
                processPendingListSequentially(list, index + 1);
            }

            @Override
            public void onError(String message, @Nullable Integer code) {
                handleSyncError(code, null);
            }
        });
    }

    private void syncUpdate(TransactionEntity tx, List<TransactionEntity> list, int index) {
        remoteRepository.updateFromLocal(tx, new ApiCallback<TransactionResponse>() {
            @Override
            public void onSuccess(TransactionResponse data) {
                tx.syncStatus = SyncStatus.SYNCED;
                tx.pendingAction = PendingAction.NONE;
                localRepository.update(tx);
                processPendingListSequentially(list, index + 1);
            }

            @Override
            public void onError(String message, @Nullable Integer code) {
                handleSyncError(code, null);
            }
        });
    }

    private void syncDelete(TransactionEntity tx, List<TransactionEntity> list, int index) {
        remoteRepository.deleteFromLocal(tx, new ApiCallback<Void>() {
            @Override
            public void onSuccess(Void ignored) {
                localRepository.delete(tx);
                processPendingListSequentially(list, index + 1);
            }

            @Override
            public void onError(String message, @Nullable Integer code) {
                handleSyncError(code, null);
            }
        });
    }

    private void handleSyncError(@Nullable Integer httpCode, @Nullable Throwable t) {
        onlineStatusManager.onSyncError(httpCode, t);
        isSyncing.set(false);
    }

    private TransactionEntity mapResponseToEntity(TransactionResponse t) {
        String title = (t.getNote() != null && !t.getNote().isEmpty()) ? t.getNote() : t.getCategoryName();
        // Giữ raw data: amount là double, occurredAt là ISO string từ BE
        TransactionEntity entity = new TransactionEntity(title, t.getCategoryName(), t.getAmount(), t.getWalletName(), t.getOccurredAt());
        entity.remoteId = t.getId();
        entity.syncStatus = SyncStatus.SYNCED;
        entity.pendingAction = PendingAction.NONE;
        return entity;
    }
}
