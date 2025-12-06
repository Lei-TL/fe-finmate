package com.finmate.data.repository;

import android.content.Context;
import androidx.lifecycle.LiveData;

import com.finmate.core.network.ApiCallback;
import com.finmate.data.dto.CategoryRequest;
import com.finmate.data.dto.CategoryResponse;
import com.finmate.data.local.database.AppDatabase;
import com.finmate.data.local.database.dao.CategoryDao;
import com.finmate.entities.CategoryEntity;

import java.util.ArrayList;
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
    private final CategoryRemoteRepository remoteRepo;
    private final Context context;

    @Inject
    public CategoryRepository(@ApplicationContext Context ctx,
                              CategoryRemoteRepository remoteRepo) {
        this.context = ctx;
        this.dao = AppDatabase.getDatabase(ctx).categoryDao();
        this.remoteRepo = remoteRepo;
    }

    // ===== LOCAL CRUD =====

    public void insert(CategoryEntity e) {
        EXECUTOR.execute(() -> dao.insert(e));
    }

    public void update(CategoryEntity e) {
        EXECUTOR.execute(() -> dao.update(e));
    }

    public void delete(CategoryEntity e) {
        EXECUTOR.execute(() -> dao.delete(e));
    }

    // Sửa lại: Trả về LiveData để ViewModel có thể observe sự thay đổi dữ liệu một cách an toàn
    public LiveData<List<CategoryEntity>> getByType(String type) {
        return dao.getByType(type);
    }

    // Sửa lại: Dùng transaction để đảm bảo tính toàn vẹn khi thay thế dữ liệu.
    private void replaceByType(String type, List<CategoryEntity> list) {
        EXECUTOR.execute(() -> {
            AppDatabase.getDatabase(context).runInTransaction(() -> {
                dao.deleteByType(type);
                for (CategoryEntity entity : list) {
                    dao.insert(entity);
                }
            });
        });
    }

    // ===== REMOTE OPERATIONS =====

    // Sửa lại: Phương thức này chỉ trigger việc fetch dữ liệu từ server.
    // ViewModel sẽ nhận dữ liệu mới thông qua LiveData từ getByType.
    public void fetchRemoteCategoriesByType(String type) {
        remoteRepo.fetchCategoriesByType(type, new ApiCallback<List<CategoryResponse>>() {
            @Override
            public void onSuccess(List<CategoryResponse> body) {
                if (body == null) return;

                List<CategoryEntity> mapped = new ArrayList<>();
                for (CategoryResponse r : body) {
                    // Sửa lại: Chuyển đổi tên icon (String) từ server thành resource ID (int) để lưu trữ
                    int iconResId = context.getResources().getIdentifier(r.getIcon(), "drawable", context.getPackageName());
                    if (iconResId == 0) continue; // Bỏ qua nếu không tìm thấy icon

                    mapped.add(new CategoryEntity(
                            r.getName(),
                            r.getType(),
                            iconResId
                    ));
                }
                replaceByType(type, mapped);
            }

            @Override
            public void onError(String msg) {
                // Có thể log lỗi hoặc hiển thị thông báo cho người dùng nếu cần
            }
        });
    }

    public void createCategory(CategoryEntity localDraft, OperationCallback cb) {
        // Sửa lại: Chuyển resource ID (int) thành tên (String) để gửi lên server
        String iconName = context.getResources().getResourceEntryName(localDraft.getIconRes());

        CategoryRequest req = new CategoryRequest(
                localDraft.getName(),
                localDraft.getType(),
                iconName
        );

        remoteRepo.createCategory(req, new ApiCallback<CategoryResponse>() {
            @Override
            public void onSuccess(CategoryResponse res) {
                int iconResId = context.getResources().getIdentifier(res.getIcon(), "drawable", context.getPackageName());
                if (iconResId == 0) {
                    cb.onError("Icon not found: " + res.getIcon());
                    return;
                }
                CategoryEntity e = new CategoryEntity(
                        res.getName(),
                        res.getType(),
                        iconResId
                );
                insert(e);
                cb.onSuccess();
            }

            @Override
            public void onError(String msg) {
                cb.onError(msg);
            }
        });
    }

    public void updateCategory(CategoryEntity edit, OperationCallback cb) {
        String id = String.valueOf(edit.getId());
        String iconName = context.getResources().getResourceEntryName(edit.getIconRes());

        CategoryRequest req = new CategoryRequest(
                edit.getName(),
                edit.getType(),
                iconName
        );

        remoteRepo.updateCategory(id, req, new ApiCallback<CategoryResponse>() {
            @Override
            public void onSuccess(CategoryResponse res) {
                int iconResId = context.getResources().getIdentifier(res.getIcon(), "drawable", context.getPackageName());
                if (iconResId == 0) {
                    cb.onError("Icon not found: " + res.getIcon());
                    return;
                }
                CategoryEntity updated = new CategoryEntity(
                        res.getName(),
                        res.getType(),
                        iconResId
                );
                updated.setId(edit.getId());
                update(updated);
                cb.onSuccess();
            }

            @Override
            public void onError(String msg) {
                cb.onError(msg);
            }
        });
    }

    public void deleteCategory(CategoryEntity e, OperationCallback cb) {
        String id = String.valueOf(e.getId());

        remoteRepo.deleteCategory(id, new ApiCallback<Void>() {
            @Override
            public void onSuccess(Void unused) {
                delete(e);
                cb.onSuccess();
            }

            @Override
            public void onError(String msg) {
                cb.onError(msg);
            }
        });
    }

    // ===== CALLBACK INTERFACE =====
    public interface OperationCallback {
        void onSuccess();
        void onError(String msg);
    }
}
