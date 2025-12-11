package com.finmate.data.repository;

import androidx.annotation.Nullable;

import com.finmate.core.offline.PendingAction;
import com.finmate.core.offline.SyncStatus;
import com.finmate.data.local.database.dao.SharedTransactionDao;
import com.finmate.data.local.database.entity.SharedTransactionEntity;
import com.finmate.data.remote.api.ApiCallback;

import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * V1: chỉ cho phép tạo shared expense khi online.
 * Nếu offline: trả lỗi để UI show banner "Cần Internet".
 */
@Singleton
public class SharedExpenseRepository {

    private static final Executor EXECUTOR = Executors.newSingleThreadExecutor();

    private final SharedTransactionDao sharedDao;
    private final SharedExpenseRemoteRepository remoteRepository;

    @Inject
    public SharedExpenseRepository(SharedTransactionDao sharedDao,
                                   SharedExpenseRemoteRepository remoteRepository) {
        this.sharedDao = sharedDao;
        this.remoteRepository = remoteRepository;
    }

    public interface Callback<T> {
        void onResult(T data);
        void onError(String message);
    }

    public void loadSharedExpenses(Callback<List<SharedTransactionEntity>> callback) {
        EXECUTOR.execute(() -> callback.onResult(sharedDao.getAll()));

        remoteRepository.listShared(new ApiCallback<List<SharedTransactionEntity>>() {
            @Override
            public void onSuccess(List<SharedTransactionEntity> data) {
                EXECUTOR.execute(() -> {
                    sharedDao.deleteAll();
                    for (SharedTransactionEntity e : data) {
                        e.syncStatus = SyncStatus.SYNCED;
                        e.pendingAction = PendingAction.NONE;
                        e.updatedAt = System.currentTimeMillis();
                        sharedDao.insert(e);
                    }
                });
                callback.onResult(data);
            }

            @Override
            public void onError(String message, @Nullable Integer code) {
                // keep cached
            }
        });
    }

    public void createSharedExpenseOnline(SharedTransactionEntity entity, Callback<Void> callback) {
        remoteRepository.createShared(entity, new ApiCallback<SharedTransactionEntity>() {
            @Override
            public void onSuccess(SharedTransactionEntity data) {
                data.syncStatus = SyncStatus.SYNCED;
                data.pendingAction = PendingAction.NONE;
                data.updatedAt = System.currentTimeMillis();
                EXECUTOR.execute(() -> sharedDao.insert(data));
                callback.onResult(null);
            }

            @Override
            public void onError(String message, @Nullable Integer code) {
                callback.onError(message != null ? message : "Không thể tạo giao dịch chia sẻ");
            }
        });
    }
}

