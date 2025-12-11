package com.finmate.data.repository;

import androidx.annotation.Nullable;

import com.finmate.core.network.SyncOnlineStatusManager;
import com.finmate.core.offline.BaseSyncManager;
import com.finmate.core.offline.PendingAction;
import com.finmate.core.offline.SyncStatus;
import com.finmate.data.dto.CategoryRequest;
import com.finmate.data.dto.CategoryResponse;
import com.finmate.data.local.database.entity.CategoryEntity;
import com.finmate.data.remote.api.ApiCallback;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class CategorySyncManager extends BaseSyncManager<CategoryEntity> {

    private final CategoryRemoteRepository remoteRepository;
    private final CategoryLocalRepository localRepository;

    @Inject
    public CategorySyncManager(CategoryRemoteRepository remoteRepository,
                               CategoryLocalRepository localRepository,
                               SyncOnlineStatusManager onlineStatusManager) {
        super(onlineStatusManager);
        this.remoteRepository = remoteRepository;
        this.localRepository = localRepository;
    }

    public void syncPendingCategories() {
        localRepository.getPendingForSync(this::syncPending);
    }

    @Override
    protected void performSync(CategoryEntity item, SyncCallback callback) {
        switch (item.getPendingAction()) {
            case PendingAction.CREATE:
                syncCreate(item, callback);
                break;
            case PendingAction.UPDATE:
                syncUpdate(item, callback);
                break;
            case PendingAction.DELETE:
                syncDelete(item, callback);
                break;
            default:
                callback.onSkip();
        }
    }

    private void syncCreate(CategoryEntity e, SyncCallback callback) {
        CategoryRequest req = new CategoryRequest(e.getName(), e.getType(), e.getIcon());
        remoteRepository.createCategory(req, new ApiCallback<CategoryResponse>() {
            @Override
            public void onSuccess(CategoryResponse res) {
                CategoryEntity synced = map(res, e.getIconRes());
                localRepository.markAsSyncedAfterCreate(e, synced);
                callback.onSuccess();
            }

            @Override
            public void onError(String message, @Nullable Integer code) {
                handleError(e, code, callback);
            }
        });
    }

    private void syncUpdate(CategoryEntity e, SyncCallback callback) {
        if (e.getRemoteId() == null) {
            syncCreate(e, callback);
            return;
        }
        CategoryRequest req = new CategoryRequest(e.getName(), e.getType(), e.getIcon());
        remoteRepository.updateCategory(e.getRemoteId(), req, new ApiCallback<CategoryResponse>() {
            @Override
            public void onSuccess(CategoryResponse res) {
                e.setSyncStatus(SyncStatus.SYNCED);
                e.setPendingAction(PendingAction.NONE);
                e.setUpdatedAt(System.currentTimeMillis());
                localRepository.updateAsSynced(e);
                callback.onSuccess();
            }

            @Override
            public void onError(String message, @Nullable Integer code) {
                handleError(e, code, callback);
            }
        });
    }

    private void syncDelete(CategoryEntity e, SyncCallback callback) {
        if (e.getRemoteId() == null) {
            localRepository.deleteImmediate(e);
            callback.onSuccess();
            return;
        }
        remoteRepository.deleteCategory(e.getRemoteId(), new ApiCallback<Void>() {
            @Override
            public void onSuccess(Void unused) {
                localRepository.deleteImmediate(e);
                callback.onSuccess();
            }

            @Override
            public void onError(String message, @Nullable Integer code) {
                handleError(e, code, callback);
            }
        });
    }

    private void handleError(CategoryEntity e, @Nullable Integer code, SyncCallback callback) {
        if (code != null && code >= 400 && code < 500) {
            e.setSyncStatus(SyncStatus.FAILED);
            e.setPendingAction(PendingAction.NONE);
            localRepository.saveStatusOnly(e);
            callback.onSuccess();
        } else {
            callback.onError(code, null);
        }
    }

    private CategoryEntity map(CategoryResponse res, int iconResId) {
        CategoryEntity entity = new CategoryEntity(res.getName(), res.getType(), iconResId);
        entity.setIcon(res.getIcon());
        entity.setRemoteId(res.getId());
        entity.setSyncStatus(SyncStatus.SYNCED);
        entity.setPendingAction(PendingAction.NONE);
        entity.setUpdatedAt(System.currentTimeMillis());
        return entity;
    }
}

