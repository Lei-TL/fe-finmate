package com.finmate.data.repository;

import android.content.Context;

import com.finmate.data.local.database.AppDatabase;
import com.finmate.data.local.database.dao.WalletDao;
import com.finmate.data.local.database.entity.WalletEntity;
import com.finmate.core.offline.PendingAction;
import com.finmate.core.offline.SyncStatus;

import java.util.List;
import java.util.concurrent.Executors;

import javax.inject.Inject;
import javax.inject.Singleton;

import dagger.hilt.android.qualifiers.ApplicationContext;

@Singleton
public class WalletLocalRepository {

    private final WalletDao dao;

    @Inject
    public WalletLocalRepository(@ApplicationContext Context context) {
        dao = AppDatabase.getDatabase(context).walletDao();
    }

    public void insert(WalletEntity wallet) {
        wallet.setPendingAction(PendingAction.CREATE);
        wallet.setSyncStatus(SyncStatus.PENDING_CREATE);
        wallet.setUpdatedAt(System.currentTimeMillis());
        Executors.newSingleThreadExecutor().execute(() -> dao.insert(wallet));
    }

    public void getAll(Callback callback) {
        Executors.newSingleThreadExecutor().execute(() ->
                callback.onResult(dao.getAll())
        );
    }

    public void getPendingWallets(Callback callback) {
        getPendingForSync(callback);
    }

    public void replaceAll(List<WalletEntity> wallets) {
        Executors.newSingleThreadExecutor().execute(() -> {
            // Cách đơn giản: xóa hết rồi insert lại
            List<WalletEntity> existing = dao.getAll();
            for (WalletEntity w : existing) {
                dao.delete(w);
            }
            for (WalletEntity w : wallets) {
                if (w.getSyncStatus() == null || w.getSyncStatus().isEmpty()) {
                    w.setSyncStatus(SyncStatus.SYNCED);
                }
                w.setPendingAction(PendingAction.NONE);
                w.setUpdatedAt(System.currentTimeMillis());
                dao.insert(w);
            }
        });
    }

    public void update(WalletEntity wallet) {
        wallet.setPendingAction(PendingAction.UPDATE);
        wallet.setSyncStatus(SyncStatus.PENDING_UPDATE);
        wallet.setUpdatedAt(System.currentTimeMillis());
        Executors.newSingleThreadExecutor().execute(() -> dao.update(wallet));
    }

    public void updateAsSynced(WalletEntity wallet) {
        wallet.setPendingAction(PendingAction.NONE);
        wallet.setSyncStatus(SyncStatus.SYNCED);
        wallet.setUpdatedAt(System.currentTimeMillis());
        Executors.newSingleThreadExecutor().execute(() -> dao.update(wallet));
    }

    public void saveStatusOnly(WalletEntity wallet) {
        wallet.setUpdatedAt(System.currentTimeMillis());
        Executors.newSingleThreadExecutor().execute(() -> dao.update(wallet));
    }

    public void flagDelete(WalletEntity wallet) {
        wallet.setPendingAction(PendingAction.DELETE);
        wallet.setSyncStatus(SyncStatus.PENDING_DELETE);
        wallet.setUpdatedAt(System.currentTimeMillis());
        Executors.newSingleThreadExecutor().execute(() -> dao.update(wallet));
    }

    public void deleteImmediate(WalletEntity wallet) {
        Executors.newSingleThreadExecutor().execute(() -> dao.delete(wallet));
    }

    public void getPendingForSync(Callback callback) {
        Executors.newSingleThreadExecutor().execute(() ->
                callback.onResult(dao.getPendingForSync())
        );
    }

    public void markAsSyncedAfterCreate(WalletEntity pending, WalletEntity synced) {
        Executors.newSingleThreadExecutor().execute(() -> {
            dao.delete(pending);
            dao.insert(synced);
        });
    }

    public interface Callback {
        void onResult(List<WalletEntity> list);
    }
}
