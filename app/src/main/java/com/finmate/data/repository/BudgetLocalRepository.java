package com.finmate.data.repository;

import android.content.Context;

import com.finmate.core.offline.PendingAction;
import com.finmate.core.offline.SyncStatus;
import com.finmate.data.local.database.AppDatabase;
import com.finmate.data.local.database.dao.BudgetDao;
import com.finmate.data.local.database.entity.BudgetEntity;

import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import javax.inject.Inject;
import javax.inject.Singleton;

import dagger.hilt.android.qualifiers.ApplicationContext;

@Singleton
public class BudgetLocalRepository {

    private static final Executor EXECUTOR = Executors.newSingleThreadExecutor();

    private final BudgetDao dao;

    @Inject
    public BudgetLocalRepository(@ApplicationContext Context context) {
        dao = AppDatabase.getDatabase(context).budgetDao();
    }

    public void insert(BudgetEntity entity) {
        entity.setPendingAction(PendingAction.CREATE);
        entity.setSyncStatus(SyncStatus.PENDING_CREATE);
        entity.setUpdatedAt(System.currentTimeMillis());
        EXECUTOR.execute(() -> dao.insert(entity));
    }

    public void update(BudgetEntity entity) {
        entity.setPendingAction(PendingAction.UPDATE);
        entity.setSyncStatus(SyncStatus.PENDING_UPDATE);
        entity.setUpdatedAt(System.currentTimeMillis());
        EXECUTOR.execute(() -> dao.update(entity));
    }

    public void delete(BudgetEntity entity) {
        entity.setPendingAction(PendingAction.DELETE);
        entity.setSyncStatus(SyncStatus.PENDING_DELETE);
        entity.setUpdatedAt(System.currentTimeMillis());
        EXECUTOR.execute(() -> dao.update(entity));
    }

    public void deleteImmediate(BudgetEntity entity) {
        EXECUTOR.execute(() -> dao.delete(entity));
    }

    public void getAll(Callback callback) {
        EXECUTOR.execute(() -> callback.onResult(dao.getAll()));
    }

    public void getPendingForSync(Callback callback) {
        EXECUTOR.execute(() -> callback.onResult(dao.getPendingForSync()));
    }

    public interface Callback {
        void onResult(List<BudgetEntity> list);
    }
}
