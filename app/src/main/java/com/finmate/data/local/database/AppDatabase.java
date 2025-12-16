package com.finmate.data.local.database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteDatabase;

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

import java.util.concurrent.Executors;

@Database(entities = {
        TokenEntity.class,
        CategoryEntity.class,
        WalletEntity.class,
        TransactionEntity.class,
        FriendEntity.class,
        PendingSyncEntity.class
}, version = 8, exportSchema = false) // ✅ Tăng version vì thêm index cho TransactionEntity
public abstract class AppDatabase extends RoomDatabase {

    public abstract TokenDao tokenDao();
    public abstract CategoryDao categoryDao();
    public abstract WalletDao walletDao();
    public abstract TransactionDao transactionDao();
    public abstract FriendDao friendDao();
    public abstract PendingSyncDao pendingSyncDao();

    private static volatile AppDatabase INSTANCE;
    private static final Object SEED_LOCK = new Object(); // ✅ Lock để tránh race condition khi seed categories
    private static volatile boolean isSeeding = false; // ✅ Flag để đảm bảo chỉ seed 1 lần tại một thời điểm

    public static AppDatabase getDatabase(Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    // ✅ Tạo database builder
                    RoomDatabase.Builder<AppDatabase> builder = Room.databaseBuilder(
                            context.getApplicationContext(),
                            AppDatabase.class, 
                            "finmate_database")
                            .fallbackToDestructiveMigration();
                    
                    // ✅ Tạo callback với reference đến builder
                    RoomDatabase.Callback callback = new RoomDatabase.Callback() {
                        @Override
                        public void onCreate(SupportSQLiteDatabase db) {
                            super.onCreate(db);
                            android.util.Log.d("AppDatabase", "onCreate called - database created");
                            // ✅ Seed categories sau khi INSTANCE được assign
                            Executors.newSingleThreadExecutor().execute(() -> {
                                seedCategoriesWhenReady();
                            });
                        }
                        
                        @Override
                        public void onOpen(SupportSQLiteDatabase db) {
                            super.onOpen(db);
                            android.util.Log.d("AppDatabase", "onOpen called - database opened");
                            // ✅ Luôn check và seed khi mở database (đảm bảo categories có sẵn)
                            Executors.newSingleThreadExecutor().execute(() -> {
                                seedCategoriesWhenReady();
                            });
                        }
                    };
                    
                    // ✅ Add callback và build
                    INSTANCE = builder.addCallback(callback).build();
                    
                    // ✅ Đảm bảo seed categories ngay sau khi build (fallback)
                    Executors.newSingleThreadExecutor().execute(() -> {
                        seedCategoriesWhenReady();
                    });
                }
            }
        }
        return INSTANCE;
    }
    
    /**
     * ✅ Helper method để seed categories khi INSTANCE đã sẵn sàng
     */
    private static void seedCategoriesWhenReady() {
        int maxRetries = 10;
        int retryCount = 0;
        
        while (retryCount < maxRetries) {
            try {
                if (INSTANCE != null) {
                    checkAndSeedCategories(INSTANCE.categoryDao());
                    return; // ✅ Thành công, thoát
                }
                // ✅ Đợi 100ms trước khi retry
                Thread.sleep(100);
                retryCount++;
            } catch (Exception e) {
                android.util.Log.e("AppDatabase", "Error in seedCategoriesWhenReady (retry " + retryCount + "): " + e.getMessage(), e);
                retryCount++;
            }
        }
        
        android.util.Log.w("AppDatabase", "Failed to seed categories after " + maxRetries + " retries");
    }
    
    /**
     * ✅ Seed default categories khi database được tạo lần đầu
     */
    private static void seedDefaultCategories(CategoryDao categoryDao) {
        android.util.Log.d("AppDatabase", "Seeding default categories on database creation...");
        seedCategories(categoryDao);
    }
    
    /**
     * ✅ Check và seed default categories nếu table rỗng
     * ✅ Có synchronization để tránh race condition
     */
    private static void checkAndSeedCategories(CategoryDao categoryDao) {
        // ✅ Synchronize để tránh nhiều thread cùng seed
        synchronized (SEED_LOCK) {
            // ✅ Check lại flag để tránh seed trùng
            if (isSeeding) {
                android.util.Log.d("AppDatabase", "Categories seeding is already in progress, skipping...");
                return;
            }
            
            // Check xem có categories nào không
            int count = categoryDao.getCount();
            if (count == 0) {
                android.util.Log.d("AppDatabase", "Categories table is empty, seeding default categories...");
                isSeeding = true;
                try {
                    seedCategories(categoryDao);
                } finally {
                    isSeeding = false;
                }
            } else {
                android.util.Log.d("AppDatabase", "Categories already exist (" + count + " items), skipping seed");
            }
        }
    }
    
    /**
     * ✅ Seed default categories vào database
     */
    private static void seedCategories(CategoryDao categoryDao) {
        java.util.List<CategoryEntity> defaultCategories = new java.util.ArrayList<>();
        
        // ✅ INCOME categories
        defaultCategories.add(new CategoryEntity("Lương", "INCOME", "ic_salary"));
        defaultCategories.add(new CategoryEntity("Thưởng", "INCOME", "ic_bonus"));
        defaultCategories.add(new CategoryEntity("Đầu tư", "INCOME", "ic_invest"));
        defaultCategories.add(new CategoryEntity("Kinh doanh", "INCOME", "ic_business"));
        defaultCategories.add(new CategoryEntity("Cho thuê", "INCOME", "ic_rent"));
        defaultCategories.add(new CategoryEntity("Lãi tiết kiệm", "INCOME", "ic_interest"));
        defaultCategories.add(new CategoryEntity("Quà tặng nhận", "INCOME", "ic_gift_received"));
        defaultCategories.add(new CategoryEntity("Bán hàng", "INCOME", "ic_sell"));
        defaultCategories.add(new CategoryEntity("Hoàn tiền", "INCOME", "ic_refund"));
        defaultCategories.add(new CategoryEntity("Thu nhập khác", "INCOME", "ic_other_income"));
        
        // ✅ EXPENSE categories
        defaultCategories.add(new CategoryEntity("Ăn uống", "EXPENSE", "ic_food"));
        defaultCategories.add(new CategoryEntity("Mua sắm", "EXPENSE", "ic_shopping"));
        defaultCategories.add(new CategoryEntity("Hóa đơn điện", "EXPENSE", "ic_electricbill"));
        defaultCategories.add(new CategoryEntity("Hóa đơn nước", "EXPENSE", "ic_waterbill"));
        defaultCategories.add(new CategoryEntity("Xăng xe", "EXPENSE", "ic_car"));
        defaultCategories.add(new CategoryEntity("Giải trí", "EXPENSE", "ic_entertain"));
        defaultCategories.add(new CategoryEntity("Sức khỏe", "EXPENSE", "ic_health"));
        defaultCategories.add(new CategoryEntity("Giáo dục", "EXPENSE", "ic_education"));
        defaultCategories.add(new CategoryEntity("Du lịch", "EXPENSE", "ic_travel"));
        defaultCategories.add(new CategoryEntity("Quà tặng", "EXPENSE", "ic_gift"));
        defaultCategories.add(new CategoryEntity("Thời trang", "EXPENSE", "ic_fashion"));
        defaultCategories.add(new CategoryEntity("Hóa đơn", "EXPENSE", "ic_bill"));
        defaultCategories.add(new CategoryEntity("Internet", "EXPENSE", "ic_internet"));
        defaultCategories.add(new CategoryEntity("Điện thoại", "EXPENSE", "ic_phone"));
        defaultCategories.add(new CategoryEntity("Bảo hiểm", "EXPENSE", "ic_insurance"));
        defaultCategories.add(new CategoryEntity("Nhà ở", "EXPENSE", "ic_home"));
        defaultCategories.add(new CategoryEntity("Thú cưng", "EXPENSE", "ic_pet"));
        defaultCategories.add(new CategoryEntity("Thể thao", "EXPENSE", "ic_sport"));
        defaultCategories.add(new CategoryEntity("Đọc sách", "EXPENSE", "ic_read"));
        defaultCategories.add(new CategoryEntity("Khác", "EXPENSE", "ic_default_category"));
        
        // ✅ Insert vào database (chỉ insert nếu chưa tồn tại)
        // ✅ Check lại count trước khi seed để tránh trùng
        int currentCount = categoryDao.getCount();
        if (currentCount > 0) {
            android.util.Log.d("AppDatabase", "Categories already exist (" + currentCount + " items), skipping seed to avoid duplicates");
            return;
        }
        
        for (CategoryEntity category : defaultCategories) {
            // Check xem đã tồn tại chưa (double-check để tránh race condition)
            CategoryEntity existing = categoryDao.getByName(category.getName());
            if (existing == null) {
                try {
                    categoryDao.insert(category);
                    android.util.Log.d("AppDatabase", "Inserted default category: " + category.getName());
                } catch (Exception e) {
                    // ✅ Nếu insert fail (có thể do duplicate constraint), log và skip
                    android.util.Log.w("AppDatabase", "Failed to insert category " + category.getName() + ": " + e.getMessage());
                }
            } else {
                android.util.Log.d("AppDatabase", "Category already exists: " + category.getName());
            }
        }
        
        android.util.Log.d("AppDatabase", "Default categories seeding completed");
    }
}
