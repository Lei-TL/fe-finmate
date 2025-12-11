package com.finmate.data.repository;

import android.content.Context;
import androidx.lifecycle.LiveData;

import androidx.annotation.Nullable;
import com.finmate.data.remote.api.ApiCallback;
import com.finmate.core.offline.PendingAction;
import com.finmate.core.offline.SyncStatus;
import com.finmate.data.dto.CategoryRequest;
import com.finmate.data.dto.CategoryResponse;
import com.finmate.data.local.database.AppDatabase;
import com.finmate.data.local.database.dao.CategoryDao;
import com.finmate.data.local.database.entity.CategoryEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import javax.inject.Inject;
import javax.inject.Singleton;

import dagger.hilt.android.qualifiers.ApplicationContext;

@Singleton
public class CategoryLocalRepository {

    private static final Executor EXECUTOR = Executors.newSingleThreadExecutor();

    private final CategoryDao dao;
    private final CategoryRemoteRepository remoteRepo;
    private final Context context;

    @Inject
    public CategoryLocalRepository(@ApplicationContext Context ctx,
                              CategoryRemoteRepository remoteRepo) {
        this.context = ctx;
        this.dao = AppDatabase.getDatabase(ctx).categoryDao();
        this.remoteRepo = remoteRepo;
    }

    // ===== LOCAL CRUD =====

    public void insert(CategoryEntity e) {
        e.setPendingAction(PendingAction.CREATE);
        e.setSyncStatus(SyncStatus.PENDING_CREATE);
        e.setUpdatedAt(System.currentTimeMillis());
        EXECUTOR.execute(() -> dao.insert(e));
    }

    public void update(CategoryEntity e) {
        e.setPendingAction(PendingAction.UPDATE);
        e.setSyncStatus(SyncStatus.PENDING_UPDATE);
        e.setUpdatedAt(System.currentTimeMillis());
        EXECUTOR.execute(() -> dao.update(e));
    }

    public void delete(CategoryEntity e) {
        e.setPendingAction(PendingAction.DELETE);
        e.setSyncStatus(SyncStatus.PENDING_DELETE);
        e.setUpdatedAt(System.currentTimeMillis());
        EXECUTOR.execute(() -> dao.update(e));
    }

    public void updateAsSynced(CategoryEntity e) {
        e.setPendingAction(PendingAction.NONE);
        e.setSyncStatus(SyncStatus.SYNCED);
        e.setUpdatedAt(System.currentTimeMillis());
        EXECUTOR.execute(() -> dao.update(e));
    }

    public void saveStatusOnly(CategoryEntity e) {
        e.setUpdatedAt(System.currentTimeMillis());
        EXECUTOR.execute(() -> dao.update(e));
    }

    public void getPendingForSync(OnResultCallback<List<CategoryEntity>> callback) {
        EXECUTOR.execute(() -> callback.onResult(dao.getPendingForSync()));
    }

    public void getPendingCategories(OnResultCallback<List<CategoryEntity>> callback) {
        getPendingForSync(callback);
    }

    public void markAsSyncedAfterCreate(CategoryEntity pending, CategoryEntity synced) {
        EXECUTOR.execute(() -> {
            dao.delete(pending);
            dao.insert(synced);
        });
    }

    public void deleteImmediate(CategoryEntity e) {
        EXECUTOR.execute(() -> dao.delete(e));
    }

    // Sửa lại: Trả về LiveData để ViewModel có thể observe sự thay đổi dữ liệu một cách an toàn
    public LiveData<List<CategoryEntity>> getByType(String type) {
        return dao.getByType(type);
    }

    // Synchronous method for lookups
    public void getByTypeSync(String type, OnResultCallback<List<CategoryEntity>> callback) {
        EXECUTOR.execute(() -> callback.onResult(dao.getByTypeSync(type)));
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

                    CategoryEntity entity = new CategoryEntity(
                            r.getName(),
                            r.getType(),
                            iconResId
                    );
                    entity.setIcon(r.getIcon());
                    entity.setRemoteId(r.getId());
                    entity.setSyncStatus(SyncStatus.SYNCED);
                    entity.setPendingAction(PendingAction.NONE);
                    entity.setUpdatedAt(System.currentTimeMillis());
                    mapped.add(entity);
                }
                replaceByType(type, mapped);
            }

            @Override
            public void onError(String msg, @Nullable Integer code) {
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
                e.setIcon(res.getIcon());
                e.setRemoteId(res.getId());
                e.setSyncStatus(SyncStatus.SYNCED);
                e.setPendingAction(PendingAction.NONE);
                e.setUpdatedAt(System.currentTimeMillis());
                insert(e);
                cb.onSuccess();
            }

            @Override
            public void onError(String msg, @Nullable Integer code) {
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
                updated.setIcon(res.getIcon());
                updated.setRemoteId(res.getId());
                updated.setSyncStatus(SyncStatus.SYNCED);
                updated.setPendingAction(PendingAction.NONE);
                updated.setUpdatedAt(System.currentTimeMillis());
                update(updated);
                cb.onSuccess();
            }

            @Override
            public void onError(String msg, @Nullable Integer code) {
                cb.onError(msg);
            }
        });
    }

    public void deleteCategory(CategoryEntity e, OperationCallback cb) {
        String id = String.valueOf(e.getId());

        remoteRepo.deleteCategory(id, new ApiCallback<Void>() {
            @Override
            public void onSuccess(Void unused) {
                dao.delete(e);
                cb.onSuccess();
            }

            @Override
            public void onError(String msg, @Nullable Integer code) {
                cb.onError(msg);
            }
        });
    }

    // ===== CALLBACK INTERFACE =====
    public interface OperationCallback {
        void onSuccess();
        void onError(String msg);
    }

    public interface OnResultCallback<T> {
        void onResult(T data);
    }
}
