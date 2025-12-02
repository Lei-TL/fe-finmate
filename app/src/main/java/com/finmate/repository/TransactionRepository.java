package com.finmate.repository;

import android.content.Context;

import com.finmate.database.AppDatabase;
import com.finmate.database.dao.TransactionDao;
import com.finmate.entities.TransactionEntity;

import java.util.List;

public class TransactionRepository {

    private final TransactionDao dao;

    public TransactionRepository(Context context) {
        dao = AppDatabase.getInstance(context).transactionDao();
    }

    public void insert(TransactionEntity entity) {
        new Thread(() -> dao.insert(entity)).start();
    }

    public void getAll(OnResultCallback<List<TransactionEntity>> callback) {
        new Thread(() -> callback.onResult(dao.getAll())).start();
    }

    public void update(TransactionEntity entity) {
        new Thread(() -> dao.update(entity)).start();
    }

    public void delete(TransactionEntity entity) {
        new Thread(() -> dao.delete(entity)).start();
    }

    public interface OnResultCallback<T> {
        void onResult(T data);
    }
}
