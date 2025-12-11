package com.finmate.data.repository;

import androidx.annotation.Nullable;

import com.finmate.core.network.SyncOnlineStatusManager;
import com.finmate.core.offline.BaseSyncManager;
import com.finmate.core.offline.PendingAction;
import com.finmate.core.offline.SyncStatus;
import com.finmate.data.local.database.entity.TransactionEntity;
import com.finmate.data.local.database.entity.WalletEntity;
import com.finmate.data.local.database.entity.CategoryEntity;
import com.finmate.data.remote.api.ApiCallback;
import com.finmate.data.dto.TransactionResponse;

import java.math.BigDecimal;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class TransactionSyncManager extends BaseSyncManager<TransactionEntity> {

    private final TransactionRemoteRepository remoteRepository;
    private final TransactionLocalRepository localRepository;
    private final WalletLocalRepository walletLocalRepository;
    private final CategoryLocalRepository categoryLocalRepository;

    @Inject
    public TransactionSyncManager(
            TransactionRemoteRepository remoteRepository,
            TransactionLocalRepository localRepository,
            WalletLocalRepository walletLocalRepository,
            CategoryLocalRepository categoryLocalRepository,
            SyncOnlineStatusManager onlineStatusManager) {
        super(onlineStatusManager);
        this.remoteRepository = remoteRepository;
        this.localRepository = localRepository;
        this.walletLocalRepository = walletLocalRepository;
        this.categoryLocalRepository = categoryLocalRepository;
    }

    public void syncPendingTransactions() {
        localRepository.getPendingForSync(this::syncPending);
    }

    @Override
    protected void performSync(TransactionEntity tx, SyncCallback callback) {
        switch (tx.pendingAction) {
            case PendingAction.CREATE:
                syncCreate(tx, callback);
                break;
            case PendingAction.UPDATE:
                syncUpdate(tx, callback);
                break;
            case PendingAction.DELETE:
                syncDelete(tx, callback);
                break;
            default:
                callback.onSkip();
        }
    }

    private void syncCreate(TransactionEntity tx, SyncCallback callback) {
        remoteRepository.createFromLocal(tx, new ApiCallback<TransactionResponse>() {
            @Override
            public void onSuccess(TransactionResponse data) {
                mapResponseToEntityAsync(data, new OnEntityMappedCallback() {
                    @Override
                    public void onMapped(TransactionEntity synced) {
                        localRepository.markAsSyncedAfterCreate(tx, synced);
                        callback.onSuccess();
                    }
                });
            }

            @Override
            public void onError(String message, @Nullable Integer code) {
                if (code != null && code >= 400 && code < 500) {
                    tx.syncStatus = SyncStatus.FAILED;
                    tx.pendingAction = PendingAction.NONE;
                    localRepository.update(tx);
                    callback.onSuccess();
                } else {
                    callback.onError(code, null);
                }
            }
        });
    }

    private void syncUpdate(TransactionEntity tx, SyncCallback callback) {
        remoteRepository.updateFromLocal(tx, new ApiCallback<TransactionResponse>() {
            @Override
            public void onSuccess(TransactionResponse data) {
                // Update the existing entity with new data from server
                // Look up names from IDs and update
                mapResponseToEntityAsync(data, new OnEntityMappedCallback() {
                    @Override
                    public void onMapped(TransactionEntity synced) {
                        // Update existing entity with synced data
                        tx.remoteId = synced.remoteId;
                        tx.name = synced.name;
                        tx.categoryName = synced.categoryName;
                        tx.amount = synced.amount;
                        tx.walletName = synced.walletName;
                        tx.occurredAt = synced.occurredAt;
                        tx.type = synced.type;
                        tx.syncStatus = SyncStatus.SYNCED;
                        tx.pendingAction = PendingAction.NONE;
                        tx.updatedAt = System.currentTimeMillis();
                        localRepository.update(tx);
                        callback.onSuccess();
                    }
                });
            }

            @Override
            public void onError(String message, @Nullable Integer code) {
                if (code != null && code >= 400 && code < 500) {
                    tx.syncStatus = SyncStatus.FAILED;
                    tx.pendingAction = PendingAction.NONE;
                    localRepository.update(tx);
                    callback.onSuccess();
                } else {
                    callback.onError(code, null);
                }
            }
        });
    }

    private void syncDelete(TransactionEntity tx, SyncCallback callback) {
        remoteRepository.deleteFromLocal(tx, new ApiCallback<Void>() {
            @Override
            public void onSuccess(Void ignored) {
                localRepository.delete(tx);
                callback.onSuccess();
            }

            @Override
            public void onError(String message, @Nullable Integer code) {
                if (code != null && code >= 400 && code < 500) {
                    tx.syncStatus = SyncStatus.FAILED;
                    tx.pendingAction = PendingAction.NONE;
                    localRepository.delete(tx);
                    callback.onSuccess();
                } else {
                    callback.onError(code, null);
                }
            }
        });
    }

    private void mapResponseToEntityAsync(TransactionResponse t, OnEntityMappedCallback callback) {
        // Look up wallet name
        walletLocalRepository.getAll(new WalletLocalRepository.Callback() {
            @Override
            public void onResult(List<WalletEntity> wallets) {
                final String[] walletName = {""};
                for (WalletEntity w : wallets) {
                    if (w.getRemoteId() != null && w.getRemoteId().equals(t.getWalletId())) {
                        walletName[0] = w.getName();
                        break;
                    }
                }
                
                // Look up category name
                if (t.getCategoryId() != null && t.getType() != null) {
                    categoryLocalRepository.getByTypeSync(t.getType(), 
                        new CategoryLocalRepository.OnResultCallback<List<CategoryEntity>>() {
                            @Override
                            public void onResult(List<CategoryEntity> categories) {
                                final String[] categoryName = {""};
                                if (categories != null) {
                                    for (CategoryEntity c : categories) {
                                        if (c.getRemoteId() != null && c.getRemoteId().equals(t.getCategoryId())) {
                                            categoryName[0] = c.getName();
                                            break;
                                        }
                                    }
                                }
                                
                                // Create entity with mapped names
                                createEntityFromResponse(t, walletName[0], categoryName[0], callback);
                            }
                        });
                } else {
                    // No category, create entity without category name
                    createEntityFromResponse(t, walletName[0], "Unknown", callback);
                }
            }
        });
    }
    
    private void createEntityFromResponse(TransactionResponse t, String walletName, String categoryName, OnEntityMappedCallback callback) {
        String title = (t.getNote() != null && !t.getNote().isEmpty()) ? t.getNote() : categoryName;
        double amount = t.getAmount() != null ? t.getAmount().doubleValue() : 0.0;
        
        TransactionEntity entity = new TransactionEntity(
                t.getId(),
                title,
                categoryName.isEmpty() ? "Unknown" : categoryName,
                amount,
                walletName.isEmpty() ? "Unknown" : walletName,
                t.getOccurredAt() != null ? t.getOccurredAt() : "",
                t.getType() != null ? t.getType() : "EXPENSE"
        );
        entity.setSyncStatus(SyncStatus.SYNCED);
        entity.setPendingAction(PendingAction.NONE);
        entity.setUpdatedAt(System.currentTimeMillis());
        callback.onMapped(entity);
    }
    
    private interface OnEntityMappedCallback {
        void onMapped(TransactionEntity entity);
    }
}
