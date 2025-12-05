package com.finmate.data.repository;

import android.content.Context;

import com.finmate.core.network.ApiCallback;
import com.finmate.data.dto.CategoryRequest;
import com.finmate.data.dto.CategoryResponse;
import com.finmate.data.local.database.AppDatabase;
import com.finmate.data.local.database.dao.CategoryDao;
import com.finmate.entities.CategoryEntity;

import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import javax.inject.Inject;
import javax.inject.Singleton;

import dagger.hilt.android.qualifiers.ApplicationContext;

@Singleton
public class CategoryRepository {

    private static final Executor EXECUTOR = Executors.newSingleThreadExecutor();

    private final CategoryDao dao;
    private final CategoryRemoteRepository remoteRepository;

    @Inject
    public CategoryRepository(@ApplicationContext Context ctx,
                              CategoryRemoteRepository remoteRepository) {
        this.dao = AppDatabase.getDatabase(ctx).categoryDao();
        this.remoteRepository = remoteRepository;
    }

    public void insert(CategoryEntity category) {
        EXECUTOR.execute(() -> dao.insert(category));
    }

    public void update(CategoryEntity category) {
        EXECUTOR.execute(() -> dao.update(category));
    }

    public void delete(CategoryEntity category) {
        EXECUTOR.execute(() -> dao.delete(category));
    }

    public void getByType(String type, Callback<List<CategoryEntity>> callback) {
        EXECUTOR.execute(() -> callback.onResult(dao.getByType(type)));
    }

    public void getAll(Callback<List<CategoryEntity>> callback) {
        EXECUTOR.execute(() -> callback.onResult(dao.getAll()));
    }

    /**
     * Ghi đè tất cả category của 1 type (INCOME/EXPENSE) bằng dữ liệu mới.
     */
    public void replaceByType(String type, List<CategoryEntity> newCategories) {
        EXECUTOR.execute(() -> {
            // Xoá category cũ theo type
            List<CategoryEntity> existing = dao.getByType(type);
            for (CategoryEntity c : existing) {
                dao.delete(c);
            }
            // Insert list mới
            for (CategoryEntity c : newCategories) {
                dao.insert(c);
            }
        });
    }

    /**
     * OFFLINE-FIRST:
     *  1) Load local từ Room theo type -> callback lần 1.
     *  2) Gọi BE -> nếu ok, save về Room + callback lần 2 với data mới.
     *  3) Nếu BE lỗi -> vẫn giữ data local, không crash.
     */
    public void fetchByTypeOfflineFirst(String type,
                                        Callback<List<CategoryEntity>> callback) {
        // 1) Local trước
        getByType(type, callback);

        // 2) Remote sau
        remoteRepository.fetchCategoriesByType(type, new ApiCallback<List<CategoryResponse>>() {
            @Override
            public void onSuccess(List<CategoryResponse> body) {
                if (body == null) return;

                // Map CategoryResponse -> CategoryEntity
                List<CategoryEntity> mapped = new java.util.ArrayList<>();
                for (CategoryResponse res : body) {
                    CategoryEntity entity = new CategoryEntity(
                            res.getName(),
                            type,      // hoặc res.getType() nếu FE/BE dùng cùng format
                            res.getIcon()
                    );
                    mapped.add(entity);
                }

                // Ghi đè vào local cache theo type
                replaceByType(type, mapped);

                // Callback với data mới
                callback.onResult(mapped);
            }

            @Override
            public void onError(String message) {
                // BE lỗi -> bỏ qua, cứ dùng local
                // optional: callback.onResult(...) / callback.onError(...)
            }
        });
    }

    public interface Callback<T> {
        void onResult(T data);
    }

    public interface OperationCallback {
        void onSuccess();
        void onError(String message);
    }

    public void createCategoryOnlineFirst(CategoryEntity localDraft,
                                          OperationCallback callback) {

        CategoryRequest request = new CategoryRequest(
                localDraft.getName(),
                localDraft.getType(),
                localDraft.getIcon()
        );

        remoteRepository.createCategory(request, new ApiCallback<CategoryResponse>() {
            @Override
            public void onSuccess(CategoryResponse res) {
                // Map CategoryResponse -> CategoryEntity chuẩn theo server
                CategoryEntity entity = new CategoryEntity(
                        res.getName(),
                        res.getType(),
                        res.getIcon()
                );
                // nếu CategoryEntity có id từ server:
                // entity.setId(res.getId());

                // Lưu vào Room
                EXECUTOR.execute(() -> dao.insert(entity));

                callback.onSuccess();
            }

            @Override
            public void onError(String message) {
                callback.onError(message);
            }
        });
    }

    public void updateCategoryOnlineFirst(CategoryEntity localEdit,
                                          OperationCallback callback) {

        // Giả định entity có id là id server
        String id = localEdit.getId();

        CategoryRequest request = new CategoryRequest(
                localEdit.getName(),
                localEdit.getType(),
                localEdit.getIcon()
        );

        remoteRepository.updateCategory(id, request, new ApiCallback<CategoryResponse>() {
            @Override
            public void onSuccess(CategoryResponse res) {
                // Map response -> entity cập nhật
                CategoryEntity updated = new CategoryEntity(
                        res.getName(),
                        res.getType(),
                        res.getIcon()
                );
                // giữ lại id server
                updated.setId(res.getId());

                EXECUTOR.execute(() -> dao.update(updated));

                callback.onSuccess();
            }

            @Override
            public void onError(String message) {
                callback.onError(message);
            }
        });
    }

    public void deleteCategoryOnlineFirst(CategoryEntity category,
                                          OperationCallback callback) {

        String id = category.getId();

        remoteRepository.deleteCategory(id, new ApiCallback<Void>() {
            @Override
            public void onSuccess(Void unused) {
                // BE đã xoá → xoá local
                EXECUTOR.execute(() -> dao.delete(category));
                callback.onSuccess();
            }

            @Override
            public void onError(String message) {
                callback.onError(message);
            }
        });
    }


}
