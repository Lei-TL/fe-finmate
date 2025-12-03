package com.finmate.repository;

import android.content.Context;

import com.finmate.database.AppDatabase;
import com.finmate.database.dao.WalletDao;
import com.finmate.entities.WalletEntity;

import java.util.concurrent.Executors;

public class WalletRepository {

    private final WalletDao dao;

    public WalletRepository(Context context) {
        dao = AppDatabase.getInstance(context).walletDao();
    }

    public void insert(WalletEntity wallet) {
        Executors.newSingleThreadExecutor().execute(() ->
                dao.insert(wallet)
        );
    }

    public void getAll(Callback callback) {
        Executors.newSingleThreadExecutor().execute(() ->
                callback.onResult(dao.getAll())
        );
    }

    public interface Callback {
        void onResult(java.util.List<WalletEntity> list);
    }
}
