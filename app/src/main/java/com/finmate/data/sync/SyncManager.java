package com.finmate.data.sync;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.hilt.work.HiltWorker;
import androidx.work.Constraints;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.finmate.core.network.NetworkChecker;
import com.finmate.data.local.database.AppDatabase;
import com.finmate.data.local.database.dao.PendingSyncDao;
import com.finmate.data.local.database.entity.PendingSyncEntity;
import com.google.gson.Gson;

import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;
import javax.inject.Singleton;

import dagger.hilt.android.qualifiers.ApplicationContext;

/**
 * SyncManager: Quản lý việc sync dữ liệu từ local lên server với retry mechanism.
 * Sử dụng WorkManager để retry tự động khi có mạng.
 */
@Singleton
public class SyncManager {

    private final Context context;
    private final PendingSyncDao pendingSyncDao;
    private final NetworkChecker networkChecker;
    private final WorkManager workManager;
    private final Gson gson;

    @Inject
    public SyncManager(@ApplicationContext Context context,
                       NetworkChecker networkChecker,
                       WorkManager workManager) {
        this.context = context;
        this.pendingSyncDao = AppDatabase.getDatabase(context).pendingSyncDao();
        this.networkChecker = networkChecker;
        this.workManager = workManager;
        this.gson = new Gson();
    }

    /**
     * Lưu pending sync khi thực hiện operation offline
     */
    public void addPendingSync(String entityType, String operationType, int localEntityId, Object entityData) {
        String jsonData = gson.toJson(entityData);
        PendingSyncEntity pendingSync = new PendingSyncEntity(
                entityType,
                operationType,
                localEntityId,
                jsonData
        );
        pendingSyncDao.insert(pendingSync);
        
        // Schedule sync work nếu có mạng
        if (networkChecker.isNetworkAvailable()) {
            scheduleSyncWork();
        }
    }

    /**
     * Xóa pending sync sau khi sync thành công
     */
    public void removePendingSync(PendingSyncEntity pendingSync) {
        pendingSyncDao.delete(pendingSync);
    }

    /**
     * Lấy tất cả pending sync
     */
    public List<PendingSyncEntity> getAllPendingSync() {
        return pendingSyncDao.getAll();
    }

    /**
     * Lấy pending sync theo entity type
     */
    public List<PendingSyncEntity> getPendingSyncByType(String entityType) {
        return pendingSyncDao.getByEntityType(entityType);
    }

    /**
     * Schedule sync work với WorkManager
     */
    public void scheduleSyncWork() {
        Constraints constraints = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build();

        OneTimeWorkRequest syncWork = new OneTimeWorkRequest.Builder(SyncWorker.class)
                .setConstraints(constraints)
                .setInitialDelay(1, TimeUnit.SECONDS)
                .build();

        workManager.enqueue(syncWork);
    }

    /**
     * Worker để thực hiện sync trong background
     * Note: Worker implementation sẽ được tách ra file riêng nếu cần
     */
}

