package com.finmate.data.repository;

import androidx.annotation.Nullable;

import com.finmate.core.offline.PendingAction;
import com.finmate.core.offline.SyncStatus;
import com.finmate.data.dto.FriendResponse;
import com.finmate.data.local.database.entity.FriendEntity;
import com.finmate.data.remote.api.ApiCallback;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class FriendRepository {

    private final FriendRemoteRepository remoteRepository;
    private final FriendLocalRepository localRepository;

    @Inject
    public FriendRepository(FriendRemoteRepository remoteRepository,
                            FriendLocalRepository localRepository) {
        this.remoteRepository = remoteRepository;
        this.localRepository = localRepository;
    }

    public interface Callback<T> {
        void onData(T data);
    }

    public void loadFriends(Callback<List<FriendEntity>> callback) {
        // immediately serve cached
        localRepository.getAll(callback::onData);

        remoteRepository.getFriends(new ApiCallback<List<FriendResponse>>() {
            @Override
            public void onSuccess(List<FriendResponse> data) {
                List<FriendEntity> mapped = mapResponses(data, "ACCEPTED");
                localRepository.replaceAll(mapped);
                callback.onData(mapped);
            }

            @Override
            public void onError(String message, @Nullable Integer code) {
                // keep cached
            }
        });
    }

    public void loadIncoming(Callback<List<FriendEntity>> callback) {
        remoteRepository.getIncoming(new ApiCallback<List<FriendResponse>>() {
            @Override
            public void onSuccess(List<FriendResponse> data) {
                callback.onData(mapResponses(data, "PENDING"));
            }

            @Override
            public void onError(String message, @Nullable Integer code) {
                callback.onData(new ArrayList<>());
            }
        });
    }

    public void sendRequest(String email, ApiCallback<Void> callback) {
        remoteRepository.sendRequest(email, callback);
    }

    public void accept(String friendshipId, ApiCallback<Void> callback) {
        remoteRepository.accept(friendshipId, callback);
    }

    private List<FriendEntity> mapResponses(List<FriendResponse> responses, String defaultStatus) {
        List<FriendEntity> list = new ArrayList<>();
        if (responses == null) return list;
        for (FriendResponse r : responses) {
            FriendEntity e = new FriendEntity(defaultStatus);
            e.remoteId = r.getId();
            e.friendUserId = r.getFriendUserId();
            e.name = r.getFriendName();
            e.email = r.getFriendEmail();
            e.avatar = null; // FriendResponse doesn't have avatar field
            e.status = r.getStatus() != null ? r.getStatus() : defaultStatus;
            e.pendingAction = PendingAction.NONE;
            e.syncStatus = SyncStatus.SYNCED;
            e.updatedAt = System.currentTimeMillis();
            list.add(e);
        }
        return list;
    }
}

