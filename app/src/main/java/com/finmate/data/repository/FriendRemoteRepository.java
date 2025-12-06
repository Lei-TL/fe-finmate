package com.finmate.data.repository;

import com.finmate.core.network.ApiCallExecutor;
import com.finmate.core.network.ApiCallback;
import com.finmate.data.dto.FriendRequest;
import com.finmate.data.dto.FriendResponse;
import com.finmate.data.remote.api.FriendService;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import retrofit2.Call;

@Singleton
public class FriendRemoteRepository {

    private final FriendService friendApi;
    private final ApiCallExecutor apiCallExecutor;

    @Inject
    public FriendRemoteRepository(FriendService friendApi,
                                  ApiCallExecutor apiCallExecutor) {
        this.friendApi = friendApi;
        this.apiCallExecutor = apiCallExecutor;
    }

    public void getFriends(ApiCallback<List<FriendResponse>> callback) {
        Call<List<FriendResponse>> call = friendApi.getFriends();
        apiCallExecutor.execute(call, callback);
    }

    public void getIncoming(ApiCallback<List<FriendResponse>> callback) {
        Call<List<FriendResponse>> call = friendApi.getIncomingRequests();
        apiCallExecutor.execute(call, callback);
    }

    public void getOutgoing(ApiCallback<List<FriendResponse>> callback) {
        Call<List<FriendResponse>> call = friendApi.getOutgoingRequests();
        apiCallExecutor.execute(call, callback);
    }

    public void sendRequest(String email, ApiCallback<Void> callback) {
        Call<Void> call = friendApi.sendFriendRequest(new FriendRequest(email));
        apiCallExecutor.execute(call, callback);
    }

    public void accept(String friendshipId, ApiCallback<Void> callback) {
        Call<Void> call = friendApi.acceptRequest(friendshipId);
        apiCallExecutor.execute(call, callback);
    }

    public void reject(String friendshipId, ApiCallback<Void> callback) {
        Call<Void> call = friendApi.rejectRequest(friendshipId);
        apiCallExecutor.execute(call, callback);
    }

    public void remove(String friendshipId, ApiCallback<Void> callback) {
        Call<Void> call = friendApi.removeFriend(friendshipId);
        apiCallExecutor.execute(call, callback);
    }
}
