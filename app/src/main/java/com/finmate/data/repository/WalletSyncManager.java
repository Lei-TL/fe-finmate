package com.finmate.data.repository;

import androidx.annotation.Nullable;

import com.finmate.core.network.SyncOnlineStatusManager;
import com.finmate.core.offline.BaseSyncManager;
import com.finmate.core.offline.PendingAction;
import com.finmate.core.offline.SyncStatus;
import com.finmate.data.dto.WalletRequest;
import com.finmate.data.dto.WalletResponse;
import com.finmate.data.local.database.entity.WalletEntity;
import com.finmate.data.remote.api.ApiCallback;

import java.math.BigDecimal;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class WalletSyncManager extends BaseSyncManager<WalletEntity> {

    private final WalletRemoteRepository remoteRepository;
    private final WalletLocalRepository localRepository;

    @Inject
    public WalletSyncManager(WalletRemoteRepository remoteRepository,
                             WalletLocalRepository localRepository,
                             SyncOnlineStatusManager onlineStatusManager) {
        super(onlineStatusManager);
        this.remoteRepository = remoteRepository;
        this.localRepository = localRepository;
    }

    public void syncPendingWallets() {
        localRepository.getPendingForSync(this::syncPending);
    }

    @Override
    protected void performSync(WalletEntity wallet, SyncCallback callback) {
        switch (wallet.getPendingAction()) {
            case PendingAction.CREATE:
                syncCreate(wallet, callback);
                break;
            case PendingAction.UPDATE:
                syncUpdate(wallet, callback);
                break;
            case PendingAction.DELETE:
                syncDelete(wallet, callback);
                break;
            default:
                callback.onSkip();
        }
    }

    private void syncCreate(WalletEntity wallet, SyncCallback callback) {
        WalletRequest request = new WalletRequest(
                wallet.getName(),
                "CASH", // default type, can be made configurable
                wallet.getCurrency() != null ? wallet.getCurrency() : "VND",
                BigDecimal.valueOf(wallet.getBalanceCached()),
                false, // archived
                null // color
        );
        remoteRepository.createWallet(request, new ApiCallback<WalletResponse>() {
            @Override
            public void onSuccess(WalletResponse data) {
                WalletEntity synced = mapResponse(data);
                localRepository.markAsSyncedAfterCreate(wallet, synced);
                callback.onSuccess();
            }

            @Override
            public void onError(String message, @Nullable Integer code) {
                handleError(wallet, code, callback);
            }
        });
    }

    private void syncUpdate(WalletEntity wallet, SyncCallback callback) {
        if (wallet.getRemoteId() == null) {
            // fallback treat as create
            syncCreate(wallet, callback);
            return;
        }
        WalletRequest request = new WalletRequest(
                wallet.getName(),
                "CASH", // default type, can be made configurable
                wallet.getCurrency() != null ? wallet.getCurrency() : "VND",
                BigDecimal.valueOf(wallet.getBalanceCached()),
                false, // archived
                null // color
        );
        remoteRepository.updateWallet(wallet.getRemoteId(), request, new ApiCallback<WalletResponse>() {
            @Override
            public void onSuccess(WalletResponse data) {
                wallet.setSyncStatus(SyncStatus.SYNCED);
                wallet.setPendingAction(PendingAction.NONE);
                wallet.setUpdatedAt(System.currentTimeMillis());
                localRepository.updateAsSynced(wallet);
                callback.onSuccess();
            }

            @Override
            public void onError(String message, @Nullable Integer code) {
                handleError(wallet, code, callback);
            }
        });
    }

    private void syncDelete(WalletEntity wallet, SyncCallback callback) {
        if (wallet.getRemoteId() == null) {
            localRepository.deleteImmediate(wallet);
            callback.onSuccess();
            return;
        }
        remoteRepository.deleteWallet(wallet.getRemoteId(), new ApiCallback<Void>() {
            @Override
            public void onSuccess(Void unused) {
                localRepository.deleteImmediate(wallet);
                callback.onSuccess();
            }

            @Override
            public void onError(String message, @Nullable Integer code) {
                handleError(wallet, code, callback);
            }
        });
    }

    private void handleError(WalletEntity wallet, @Nullable Integer code, SyncCallback callback) {
        if (code != null && code >= 400 && code < 500) {
            wallet.setSyncStatus(SyncStatus.FAILED);
            wallet.setPendingAction(PendingAction.NONE);
            localRepository.saveStatusOnly(wallet);
            callback.onSuccess();
        } else {
            callback.onError(code, null);
        }
    }

    private WalletEntity mapResponse(WalletResponse res) {
        // Use initialBalance from backend, or fallback to currentBalance helper
        double balance = res.getInitialBalance() != null ? 
                res.getInitialBalance().doubleValue() : res.getCurrentBalance();
        WalletEntity entity = new WalletEntity(
                res.getName(),
                res.getCurrency() != null ? res.getCurrency() : "VND",
                balance,
                0
        );
        entity.setRemoteId(res.getId());
        entity.setSyncStatus(SyncStatus.SYNCED);
        entity.setPendingAction(PendingAction.NONE);
        entity.setUpdatedAt(System.currentTimeMillis());
        return entity;
    }
}

