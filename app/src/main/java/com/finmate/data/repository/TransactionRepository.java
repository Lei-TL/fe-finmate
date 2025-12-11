package com.finmate.data.repository;

import android.content.Context;

import com.finmate.data.local.database.AppDatabase;
import com.finmate.data.local.database.dao.TransactionDao;
import com.finmate.data.local.database.entity.TransactionEntity;

import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import javax.inject.Inject;
import javax.inject.Singleton;

import dagger.hilt.android.qualifiers.ApplicationContext;

@Singleton
public class TransactionRepository {

    private static final Executor EXECUTOR = Executors.newSingleThreadExecutor();

    private final TransactionDao dao;

    @Inject
    public TransactionRepository(@ApplicationContext Context context) {
        dao = AppDatabase.getDatabase(context).transactionDao();
    }

    public void insert(TransactionEntity entity) {
        EXECUTOR.execute(() -> dao.insert(entity));
    }

    public void getAll(OnResultCallback<List<TransactionEntity>> callback) {
        EXECUTOR.execute(() -> callback.onResult(dao.getAll()));
    }

    public void update(TransactionEntity entity) {
        EXECUTOR.execute(() -> dao.update(entity));
    }

    public void delete(TransactionEntity entity) {
        EXECUTOR.execute(() -> dao.delete(entity));
    }

    /**
     * Ghi đè toàn bộ transaction local = data mới từ server.
     * Anh có thể sau này đổi thành sync thông minh hơn.
     */
    public void replaceAll(List<TransactionEntity> transactions) {
        EXECUTOR.execute(() -> {
            List<TransactionEntity> existing = dao.getAll();
            for (TransactionEntity t : existing) {
                dao.delete(t);
            }
            for (TransactionEntity t : transactions) {
                dao.insert(t);
            }
        });
    }

    public interface OnResultCallback<T> {
        void onResult(T data);
    }
}
