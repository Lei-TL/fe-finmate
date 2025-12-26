package com.finmate.data.local.database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.OnConflictStrategy;

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

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Executors;

@Database(entities = {
        TokenEntity.class,
        CategoryEntity.class,
        WalletEntity.class,
        TransactionEntity.class,
        FriendEntity.class,
        PendingSyncEntity.class
}, version = 7, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {

    public abstract TokenDao tokenDao();
    public abstract CategoryDao categoryDao();
    public abstract WalletDao walletDao();
    public abstract TransactionDao transactionDao();
    public abstract FriendDao friendDao();
    public abstract PendingSyncDao pendingSyncDao();

    private static volatile AppDatabase INSTANCE;
    private static final Object SEED_LOCK = new Object();
    private static volatile boolean isSeeding = false;

    public static AppDatabase getDatabase(Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                                    AppDatabase.class, "finmate_database")
                            .fallbackToDestructiveMigration()
                            .addCallback(new RoomDatabase.Callback() {
                                @Override
                                public void onCreate(androidx.sqlite.db.SupportSQLiteDatabase db) {
                                    super.onCreate(db);
                                    // ✅ Seed categories khi database được tạo lần đầu (fallback khi offline)
                                    Executors.newSingleThreadExecutor().execute(() -> {
                                        seedCategories(INSTANCE);
                                    });
                                }

                                @Override
                                public void onOpen(androidx.sqlite.db.SupportSQLiteDatabase db) {
                                    super.onOpen(db);
                                    // ✅ Check và seed categories khi database được mở (chỉ khi offline và chưa có)
                                    // ✅ Ưu tiên sync từ backend khi có mạng
                                    Executors.newSingleThreadExecutor().execute(() -> {
                                        checkAndSeedCategoriesIfNeeded(INSTANCE);
                                    });
                                }
                            })
                            .build();
                }
            }
        }
        return INSTANCE;
    }

    /**
     * ✅ Check và seed categories nếu chưa có (chỉ khi offline)
     * Được gọi khi database được mở
     * ✅ Ưu tiên sync từ backend khi có mạng, chỉ seed local khi offline và chưa có categories
     * ✅ Frontend sẽ sync từ backend khi có mạng, seed local chỉ là fallback
     */
    private static void checkAndSeedCategoriesIfNeeded(AppDatabase database) {
        if (database == null) return;
        
        synchronized (SEED_LOCK) {
            if (isSeeding) {
                return; // Đang seed, không seed lại
            }
            isSeeding = true;
        }
        
        try {
            CategoryDao categoryDao = database.categoryDao();
            int count = categoryDao.getCount();
            
            // ✅ Nếu chưa có categories, seed ngay (fallback khi offline)
            // ✅ Khi có mạng, frontend sẽ sync từ backend và override local data
            if (count == 0) {
                android.util.Log.d("AppDatabase", "No categories found (count=0), seeding default categories as fallback...");
                android.util.Log.d("AppDatabase", "Note: Frontend will sync from backend when online, this is just a fallback");
                seedCategories(database);
            } else {
                android.util.Log.d("AppDatabase", "Categories already exist: " + count + " items");
            }
        } catch (Exception e) {
            android.util.Log.e("AppDatabase", "Error checking categories: " + e.getMessage(), e);
            // ✅ Nếu có lỗi khi check, vẫn thử seed để đảm bảo có categories (fallback)
            try {
                seedCategories(database);
            } catch (Exception seedError) {
                android.util.Log.e("AppDatabase", "Error seeding categories: " + seedError.getMessage(), seedError);
            }
        } finally {
            synchronized (SEED_LOCK) {
                isSeeding = false;
            }
        }
    }

    /**
     * ✅ Seed các categories mặc định (system-wide)
     */
    private static void seedCategories(AppDatabase database) {
        if (database == null) return;
        
        try {
            CategoryDao categoryDao = database.categoryDao();
            
            // ✅ Kiểm tra số lượng categories hiện có
            int count = categoryDao.getCount();
            if (count > 0) {
                android.util.Log.d("AppDatabase", "Categories already exist (count=" + count + "), skipping seed");
                return;
            }
            
            // ✅ Danh sách categories mặc định (INCOME) - dựa trên sample_data_fixed.sql
            List<CategoryEntity> incomeCategories = Arrays.asList(
                    new CategoryEntity("Lương", "INCOME", "ic_salary"),
                    new CategoryEntity("Thưởng", "INCOME", "ic_bonus"),
                    new CategoryEntity("Đầu tư", "INCOME", "ic_invest"),
                    new CategoryEntity("Kinh doanh", "INCOME", "ic_business"),
                    new CategoryEntity("Cho thuê", "INCOME", "ic_rent"),
                    new CategoryEntity("Lãi tiết kiệm", "INCOME", "ic_interest"),
                    new CategoryEntity("Quà tặng nhận", "INCOME", "ic_gift_received"),
                    new CategoryEntity("Bán hàng", "INCOME", "ic_sell"),
                    new CategoryEntity("Hoàn tiền", "INCOME", "ic_refund"),
                    new CategoryEntity("Thu nhập khác", "INCOME", "ic_other_income")
            );
            
            // ✅ Danh sách categories mặc định (EXPENSE) - dựa trên sample_data_fixed.sql
            List<CategoryEntity> expenseCategories = Arrays.asList(
                    new CategoryEntity("Ăn uống", "EXPENSE", "ic_food"),
                    new CategoryEntity("Mua sắm", "EXPENSE", "ic_shopping"),
                    new CategoryEntity("Hóa đơn điện", "EXPENSE", "ic_electricbill"),
                    new CategoryEntity("Hóa đơn nước", "EXPENSE", "ic_waterbill"),
                    new CategoryEntity("Xăng xe", "EXPENSE", "ic_car"),
                    new CategoryEntity("Giải trí", "EXPENSE", "ic_entertain"),
                    new CategoryEntity("Sức khỏe", "EXPENSE", "ic_health"),
                    new CategoryEntity("Giáo dục", "EXPENSE", "ic_education"),
                    new CategoryEntity("Du lịch", "EXPENSE", "ic_travel"),
                    new CategoryEntity("Quà tặng", "EXPENSE", "ic_gift"),
                    new CategoryEntity("Thời trang", "EXPENSE", "ic_fashion"),
                    new CategoryEntity("Hóa đơn", "EXPENSE", "ic_bill"),
                    new CategoryEntity("Internet", "EXPENSE", "ic_internet"),
                    new CategoryEntity("Chi tiêu khác", "EXPENSE", "ic_other_expense")
            );
            
            // ✅ Insert categories
            int inserted = 0;
            for (CategoryEntity category : incomeCategories) {
                try {
                    categoryDao.insert(category);
                    inserted++;
                    android.util.Log.d("AppDatabase", "Seeded category: " + category.getName() + " (" + category.getType() + ")");
                } catch (Exception e) {
                    android.util.Log.e("AppDatabase", "Error inserting category " + category.getName() + ": " + e.getMessage());
                }
            }
            
            for (CategoryEntity category : expenseCategories) {
                try {
                    categoryDao.insert(category);
                    inserted++;
                    android.util.Log.d("AppDatabase", "Seeded category: " + category.getName() + " (" + category.getType() + ")");
                } catch (Exception e) {
                    android.util.Log.e("AppDatabase", "Error inserting category " + category.getName() + ": " + e.getMessage());
                }
            }
            
            android.util.Log.d("AppDatabase", "✅ Categories seeded successfully: " + inserted + " categories inserted");
        } catch (Exception e) {
            android.util.Log.e("AppDatabase", "Error seeding categories: " + e.getMessage(), e);
        }
    }
}
