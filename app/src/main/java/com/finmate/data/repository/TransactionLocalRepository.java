package com.finmate.data.repository;

import android.content.Context;

import com.finmate.data.local.database.AppDatabase;
import com.finmate.data.local.database.dao.TransactionDao;
import com.finmate.data.local.database.entity.SyncStatus;
import com.finmate.data.local.database.entity.TransactionEntity;

import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import javax.inject.Inject;
import javax.inject.Singleton;

import dagger.hilt.android.qualifiers.ApplicationContext;

@Singleton
public class TransactionLocalRepository {

    private static final Executor EXECUTOR = Executors.newSingleThreadExecutor();

    private final TransactionDao dao;

    @Inject
    public TransactionLocalRepository(@ApplicationContext Context context) {
        dao = AppDatabase.getDatabase(context).transactionDao();
    }

    public void insert(TransactionEntity entity) {
        EXECUTOR.execute(() -> dao.insert(entity));
    }

    public void getAll(OnResultCallback<List<TransactionEntity>> callback) {
        EXECUTOR.execute(() -> callback.onResult(dao.getAll()));
    }

    public void getPendingTransactions(OnResultCallback<List<TransactionEntity>> callback) {
        EXECUTOR.execute(() -> callback.onResult(dao.getPendingTransactions()));
    }

    public void update(TransactionEntity entity) {
        EXECUTOR.execute(() -> dao.update(entity));
    }

    public void delete(TransactionEntity entity) {
        EXECUTOR.execute(() -> dao.delete(entity));
    }

    public void replaceAllSynced(List<TransactionEntity> transactions) {
        EXECUTOR.execute(() -> {
            dao.deleteByStatus(SyncStatus.SYNCED);
            for (TransactionEntity t : transactions) {
                dao.insert(t);
            }
        });
    }

    public void markAsSyncedAfterCreate(TransactionEntity oldPending, TransactionEntity newSynced) {
        EXECUTOR.execute(() -> {
            dao.delete(oldPending);
            dao.insert(newSynced);
        });
    }
    
    public interface OnResultCallback<T> {
        void onResult(T data);
    }
}
