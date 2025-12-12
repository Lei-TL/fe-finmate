package com.finmate.data.local.database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.finmate.data.local.database.dao.CategoryDao;
import com.finmate.data.local.database.dao.FriendDao;
import com.finmate.data.local.database.dao.PendingSyncDao;
import com.finmate.data.local.database.dao.TokenDao;
import com.finmate.data.local.database.dao.TransactionDao;
import com.finmate.data.local.database.dao.WalletDao;
import com.finmate.data.local.database.entity.CategoryEntity;
import com.finmate.data.local.database.entity.FriendEntity;
import com.finmate.data.local.database.entity.PendingSyncEntity;
import com.finmate.data.local.datastore.entity.TokenEntity;
import com.finmate.data.local.database.entity.TransactionEntity;
import com.finmate.data.local.database.entity.WalletEntity;

@Database(entities = {
        TokenEntity.class,
        CategoryEntity.class,
        WalletEntity.class,
        TransactionEntity.class,
        FriendEntity.class,
        PendingSyncEntity.class
}, version = 7, exportSchema = false) // ✅ Tăng version vì thêm FriendEntity và PendingSyncEntity
public abstract class AppDatabase extends RoomDatabase {

    public abstract TokenDao tokenDao();
    public abstract CategoryDao categoryDao();
    public abstract WalletDao walletDao();
    public abstract TransactionDao transactionDao();
    public abstract FriendDao friendDao();
    public abstract PendingSyncDao pendingSyncDao();

    private static volatile AppDatabase INSTANCE;

    public static AppDatabase getDatabase(Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                                    AppDatabase.class, "finmate_database")
                            .fallbackToDestructiveMigration()
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}
