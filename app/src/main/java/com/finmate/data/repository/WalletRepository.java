package com.finmate.data.repository;

import android.content.Context;

import com.finmate.data.local.database.AppDatabase;
import com.finmate.data.local.database.dao.WalletDao;
import com.finmate.data.local.database.entity.WalletEntity;

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

    /**
     * Ghi đè toàn bộ wallet local = data mới từ server.
     * ⚠️ CẢNH BÁO: Method này sẽ XÓA TẤT CẢ wallets cũ, chỉ giữ lại danh sách mới.
     * Nếu có wallets tạo offline (chưa sync), sẽ bị mất.
     * Nên dùng upsertAll() thay vì method này.
     */
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

    /**
     * Upsert wallets: Insert nếu mới, Update nếu đã tồn tại (dựa trên id/UUID).
     * ✅ An toàn hơn replaceAll: Không xóa wallets cũ, chỉ cập nhật/thêm mới.
     * ✅ Giữ nguyên các wallets tạo local (nếu có id khác với danh sách từ server).
     * 
     * @param wallets Danh sách wallets từ backend (phải có id là UUID)
     */
    public void upsertAll(List<WalletEntity> wallets) {
        Executors.newSingleThreadExecutor().execute(() -> {
            for (WalletEntity newWallet : wallets) {
                // Chỉ upsert những wallets có id (UUID từ backend)
                if (newWallet.id == null || newWallet.id.isEmpty()) {
                    continue; // Bỏ qua wallets không có id
                }
                
                // Tìm wallet đã tồn tại theo id
                WalletEntity existing = dao.getById(newWallet.id);
                
                if (existing != null) {
                    // Đã tồn tại → Update
                    dao.update(newWallet);
                } else {
                    // Chưa tồn tại → Insert mới
                    dao.insert(newWallet);
                }
            }
        });
    }

    /**
     * ✅ Tính lại và update wallet balance dựa trên tất cả transactions của wallet
     * @param walletName Tên của wallet cần update balance
     * @param transactionDao DAO để lấy transactions trực tiếp
     */
    public void updateWalletBalance(String walletName, com.finmate.data.local.database.dao.TransactionDao transactionDao) {
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                // Tìm wallet theo tên (vì có thể có nhiều wallets với tên khác nhau)
                List<WalletEntity> allWallets = dao.getAll();
                WalletEntity wallet = null;
                for (WalletEntity w : allWallets) {
                    if (w.name != null && w.name.equals(walletName)) {
                        wallet = w;
                        break;
                    }
                }
                
                if (wallet == null) {
                    return; // Wallet không tồn tại
                }
                
                // Lấy tất cả transactions của wallet này (không giới hạn)
                List<com.finmate.data.local.database.entity.TransactionEntity> transactions = transactionDao.getByWalletName(walletName);
                
                // Tính lại balance từ initialBalance + tất cả transactions
                double newBalance = wallet.initialBalance;
                
                if (transactions != null) {
                    for (com.finmate.data.local.database.entity.TransactionEntity tx : transactions) {
                        if (tx.type != null && tx.amountDouble != 0) {
                            if ("INCOME".equals(tx.type)) {
                                newBalance += tx.amountDouble;
                            } else if ("EXPENSE".equals(tx.type)) {
                                newBalance -= tx.amountDouble;
                            }
                        }
                    }
                }
                
                // Update wallet balance
                wallet.currentBalance = newBalance;
                dao.update(wallet);
            } catch (Exception e) {
                android.util.Log.e("WalletRepository", "Error updating wallet balance: " + e.getMessage(), e);
            }
        });
    }

    public interface Callback {
        void onResult(List<WalletEntity> list);
    }
}
