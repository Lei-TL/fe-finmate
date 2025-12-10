package com.finmate.data.local.database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.finmate.data.local.database.dao.CategoryDao;
import com.finmate.data.local.database.dao.TokenDao;
import com.finmate.data.local.database.dao.TransactionDao;
import com.finmate.data.local.database.dao.WalletDao;
import com.finmate.data.local.database.entity.CategoryEntity;
import com.finmate.data.local.database.entity.TransactionEntity;
import com.finmate.data.local.database.entity.WalletEntity;
import com.finmate.data.local.datastore.entity.TokenEntity;

@Database(entities = {TokenEntity.class, CategoryEntity.class, WalletEntity.class, TransactionEntity.class}, version = 4, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {
    public abstract TokenDao tokenDao();
    public abstract CategoryDao categoryDao();
    public abstract WalletDao walletDao();
    public abstract TransactionDao transactionDao();

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
