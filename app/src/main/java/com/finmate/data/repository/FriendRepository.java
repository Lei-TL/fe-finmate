package com.finmate.data.repository;

import android.content.Context;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.finmate.core.network.ApiCallback;
import com.finmate.core.network.NetworkChecker;
import com.finmate.data.dto.FriendResponse;
import com.finmate.data.local.database.AppDatabase;
import com.finmate.data.local.database.dao.FriendDao;
import com.finmate.data.local.database.entity.FriendEntity;
import com.finmate.data.sync.SyncManager;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import javax.inject.Inject;
import javax.inject.Singleton;

import dagger.hilt.android.qualifiers.ApplicationContext;

/**
 * FriendRepository với offline-first pattern:
 * - Đọc từ local DB (LiveData)
 * - Ghi vào local DB trước
 * - Sync lên server nếu có mạng
 * - Lưu vào pending sync nếu không có mạng
 */
@Singleton
public class FriendRepository {

    private static final Executor EXECUTOR = Executors.newSingleThreadExecutor();

    private final FriendDao dao;
    private final FriendRemoteRepository remoteRepo;
    private final NetworkChecker networkChecker;
    private final SyncManager syncManager;

    @Inject
    public FriendRepository(@ApplicationContext Context context,
                           FriendRemoteRepository remoteRepo,
                           NetworkChecker networkChecker,
                           SyncManager syncManager) {
        this.dao = AppDatabase.getDatabase(context).friendDao();
        this.remoteRepo = remoteRepo;
        this.networkChecker = networkChecker;
        this.syncManager = syncManager;
    }

    // ===== LOCAL CRUD =====

    /**
     * Lấy tất cả friends từ local DB
     */
    public LiveData<List<FriendEntity>> getAllFriends() {
        MutableLiveData<List<FriendEntity>> liveData = new MutableLiveData<>();
        EXECUTOR.execute(() -> {
            List<FriendEntity> friends = dao.getAll();
            liveData.postValue(friends != null ? friends : new ArrayList<>());
        });
        return liveData;
    }

    /**
     * Lấy friends theo status từ local DB
     */
    public LiveData<List<FriendEntity>> getFriendsByStatus(String status) {
        MutableLiveData<List<FriendEntity>> liveData = new MutableLiveData<>();
        EXECUTOR.execute(() -> {
            List<FriendEntity> friends = dao.getByStatus(status);
            liveData.postValue(friends != null ? friends : new ArrayList<>());
        });
        return liveData;
    }

    /**
     * Lấy incoming/outgoing requests từ local DB
     */
    public LiveData<List<FriendEntity>> getRequests(boolean incoming) {
        MutableLiveData<List<FriendEntity>> liveData = new MutableLiveData<>();
        EXECUTOR.execute(() -> {
            List<FriendEntity> friends = dao.getByIncoming(incoming);
            liveData.postValue(friends != null ? friends : new ArrayList<>());
        });
        return liveData;
    }

    // ===== REMOTE OPERATIONS =====

    /**
     * Fetch friends từ remote và sync về local
     * Offline-first: Chỉ fetch nếu có mạng
     */
    public void fetchRemoteFriends() {
        if (!networkChecker.isNetworkAvailable()) {
            return;
        }

        remoteRepo.getFriends(new ApiCallback<List<FriendResponse>>() {
            @Override
            public void onSuccess(List<FriendResponse> body) {
                if (body == null) return;

                List<FriendEntity> mapped = new ArrayList<>();
                for (FriendResponse r : body) {
                    FriendEntity entity = new FriendEntity(
                            r.getId(),
                            r.getFriendUserId(),
                            r.getFriendName(),
                            r.getFriendEmail(),
                            r.getStatus(),
                            r.isIncoming()
                    );
                    mapped.add(entity);
                }

                // Replace local với data từ server
                EXECUTOR.execute(() -> {
                    dao.deleteAll();
                    dao.insertAll(mapped);
                });
            }

            @Override
            public void onError(String msg) {
                // Lỗi → chỉ dùng data local
            }
        });
    }

    /**
     * Send friend request
     * Offline-first: Lưu local trước, sau đó sync nếu có mạng
     */
    public void sendRequest(String email, OperationCallback cb) {
        // Tạo friend entity với status PENDING
        FriendEntity friend = new FriendEntity(
                "pending_" + System.currentTimeMillis(), // Temporary ID
                "",
                "",
                email,
                "PENDING",
                false // outgoing
        );

        // 1) Lưu vào local DB trước (offline-first)
        EXECUTOR.execute(() -> {
            dao.insert(friend);

            // 2) Sync lên backend nếu có mạng
            if (networkChecker.isNetworkAvailable()) {
                remoteRepo.sendRequest(email, new ApiCallback<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        if (cb != null) cb.onSuccess();
                    }

                    @Override
                    public void onError(String msg) {
                        // Lỗi sync → thêm vào pending sync
                        syncManager.addPendingSync("FRIEND", "CREATE", 0, friend);
                        if (cb != null) cb.onSuccess(); // Vẫn success vì đã lưu local
                    }
                });
            } else {
                // Không có mạng → thêm vào pending sync
                syncManager.addPendingSync("FRIEND", "CREATE", 0, friend);
                if (cb != null) cb.onSuccess();
            }
        });
    }

    /**
     * Accept friend request
     * Offline-first: Update local trước, sau đó sync nếu có mạng
     */
    public void acceptRequest(String friendshipId, OperationCallback cb) {
        EXECUTOR.execute(() -> {
            // Update local status
            List<FriendEntity> friends = dao.getAll();
            for (FriendEntity f : friends) {
                if (f.friendshipId.equals(friendshipId)) {
                    f.status = "ACCEPTED";
                    dao.insert(f); // Replace với status mới
                    break;
                }
            }

            // Sync lên backend nếu có mạng
            if (networkChecker.isNetworkAvailable()) {
                remoteRepo.accept(friendshipId, new ApiCallback<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        if (cb != null) cb.onSuccess();
                    }

                    @Override
                    public void onError(String msg) {
                        // Lỗi sync → thêm vào pending sync
                        FriendEntity friend = new FriendEntity(friendshipId, "", "", "", "ACCEPTED", false);
                        syncManager.addPendingSync("FRIEND", "UPDATE", 0, friend);
                        if (cb != null) cb.onSuccess();
                    }
                });
            } else {
                // Không có mạng → thêm vào pending sync
                FriendEntity friend = new FriendEntity(friendshipId, "", "", "", "ACCEPTED", false);
                syncManager.addPendingSync("FRIEND", "UPDATE", 0, friend);
                if (cb != null) cb.onSuccess();
            }
        });
    }

    /**
     * Reject friend request
     * Offline-first: Delete local trước, sau đó sync nếu có mạng
     */
    public void rejectRequest(String friendshipId, OperationCallback cb) {
        EXECUTOR.execute(() -> {
            // Delete từ local
            List<FriendEntity> friends = dao.getAll();
            for (FriendEntity f : friends) {
                if (f.friendshipId.equals(friendshipId)) {
                    dao.delete(f);
                    break;
                }
            }

            // Sync lên backend nếu có mạng
            if (networkChecker.isNetworkAvailable()) {
                remoteRepo.reject(friendshipId, new ApiCallback<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        if (cb != null) cb.onSuccess();
                    }

                    @Override
                    public void onError(String msg) {
                        // Lỗi sync → thêm vào pending sync
                        FriendEntity friend = new FriendEntity(friendshipId, "", "", "", "REJECTED", false);
                        syncManager.addPendingSync("FRIEND", "DELETE", 0, friend);
                        if (cb != null) cb.onSuccess();
                    }
                });
            } else {
                // Không có mạng → thêm vào pending sync
                FriendEntity friend = new FriendEntity(friendshipId, "", "", "", "REJECTED", false);
                syncManager.addPendingSync("FRIEND", "DELETE", 0, friend);
                if (cb != null) cb.onSuccess();
            }
        });
    }

    /**
     * Remove friend
     * Offline-first: Delete local trước, sau đó sync nếu có mạng
     */
    public void removeFriend(String friendshipId, OperationCallback cb) {
        EXECUTOR.execute(() -> {
            // Delete từ local
            List<FriendEntity> friends = dao.getAll();
            for (FriendEntity f : friends) {
                if (f.friendshipId.equals(friendshipId)) {
                    dao.delete(f);
                    break;
                }
            }

            // Sync lên backend nếu có mạng
            if (networkChecker.isNetworkAvailable()) {
                remoteRepo.remove(friendshipId, new ApiCallback<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        if (cb != null) cb.onSuccess();
                    }

                    @Override
                    public void onError(String msg) {
                        // Lỗi sync → thêm vào pending sync
                        FriendEntity friend = new FriendEntity(friendshipId, "", "", "", "", false);
                        syncManager.addPendingSync("FRIEND", "DELETE", 0, friend);
                        if (cb != null) cb.onSuccess();
                    }
                });
            } else {
                // Không có mạng → thêm vào pending sync
                FriendEntity friend = new FriendEntity(friendshipId, "", "", "", "", false);
                syncManager.addPendingSync("FRIEND", "DELETE", 0, friend);
                if (cb != null) cb.onSuccess();
            }
        });
    }

    // ===== CALLBACK INTERFACE =====
    public interface OperationCallback {
        void onSuccess();
        void onError(String msg);
    }
}

