package com.finmate.database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.finmate.database.dao.TokenDao;
import com.finmate.database.dao.TransactionDao;
import com.finmate.database.dao.CategoryDao;
import com.finmate.database.dao.WalletDao;

import com.finmate.entities.TokenEntity;
import com.finmate.entities.TransactionEntity;
import com.finmate.entities.CategoryEntity;
import com.finmate.entities.WalletEntity;

@Database(
        entities = {
                TokenEntity.class,
                TransactionEntity.class,
                CategoryEntity.class,
                WalletEntity.class              // 👈 THIẾU CÁI NÀY
        },
        version = 5,                            // 👈 TĂNG VERSION
        exportSchema = false
)
public abstract class AppDatabase extends RoomDatabase {

    public abstract TokenDao tokenDao();
    public abstract TransactionDao transactionDao();
    public abstract CategoryDao categoryDao();
    public abstract WalletDao walletDao();      // 👈 DAO ví

    private static volatile AppDatabase INSTANCE;

    public static AppDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                                    context.getApplicationContext(),
                                    AppDatabase.class,
                                    "finmate.db"
                            )
                            .fallbackToDestructiveMigration()
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}
