package com.finmate.data.repository;

import android.content.Context;

import com.finmate.core.offline.PendingAction;
import com.finmate.core.offline.SyncStatus;
import com.finmate.data.local.database.AppDatabase;
import com.finmate.data.local.database.dao.FriendDao;
import com.finmate.data.local.database.entity.FriendEntity;

import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import javax.inject.Inject;
import javax.inject.Singleton;

import dagger.hilt.android.qualifiers.ApplicationContext;

@Singleton
public class FriendLocalRepository {

    private static final Executor EXECUTOR = Executors.newSingleThreadExecutor();
    private final FriendDao dao;

    @Inject
    public FriendLocalRepository(@ApplicationContext Context context) {
        dao = AppDatabase.getDatabase(context).friendDao();
    }

    public void replaceAll(List<FriendEntity> friends) {
        EXECUTOR.execute(() -> {
            dao.deleteAll();
            for (FriendEntity e : friends) {
                dao.insert(e);
            }
        });
    }

    public void getAll(OnResultCallback<List<FriendEntity>> callback) {
        EXECUTOR.execute(() -> callback.onResult(dao.getAll()));
    }

    public void insertPending(FriendEntity entity) {
        entity.pendingAction = PendingAction.CREATE;
        entity.syncStatus = SyncStatus.PENDING_CREATE;
        entity.updatedAt = System.currentTimeMillis();
        EXECUTOR.execute(() -> dao.insert(entity));
    }

    public void markSynced(FriendEntity entity) {
        entity.pendingAction = PendingAction.NONE;
        entity.syncStatus = SyncStatus.SYNCED;
        entity.updatedAt = System.currentTimeMillis();
        EXECUTOR.execute(() -> dao.update(entity));
    }

    public void getPendingForSync(OnResultCallback<List<FriendEntity>> callback) {
        EXECUTOR.execute(() -> callback.onResult(dao.getPendingForSync()));
    }

    public interface OnResultCallback<T> {
        void onResult(T data);
    }
}

