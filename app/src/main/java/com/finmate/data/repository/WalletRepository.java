package com.finmate.data.repository;

import android.content.Context;

import com.finmate.data.local.database.AppDatabase;
import com.finmate.data.local.database.dao.WalletDao;
import com.finmate.entities.WalletEntity;

import java.util.List;
import java.util.concurrent.Executors;

import javax.inject.Inject;
import javax.inject.Singleton;

import dagger.hilt.android.qualifiers.ApplicationContext;

@Singleton
public class WalletRepository {

    private final WalletDao dao;

    @Inject
    public WalletRepository(@ApplicationContext Context context) {
        dao = AppDatabase.getDatabase(context).walletDao();
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

    public void replaceAll(List<WalletEntity> wallets) {
        Executors.newSingleThreadExecutor().execute(() -> {
            // Cách đơn giản: xóa hết rồi insert lại
            List<WalletEntity> existing = dao.getAll();
            for (WalletEntity w : existing) {
                dao.delete(w);
            }
            for (WalletEntity w : wallets) {
                dao.insert(w);
            }
        });
    }

    public interface Callback {
        void onResult(List<WalletEntity> list);
    }
}
