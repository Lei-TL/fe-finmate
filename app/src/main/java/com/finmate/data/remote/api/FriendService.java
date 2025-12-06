package com.finmate.data.remote.api;

import com.finmate.data.dto.FriendRequest;
import com.finmate.data.dto.FriendResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface FriendService {

    @POST("friends/requests")
    Call<Void> sendFriendRequest(@Body FriendRequest request);

    @GET("friends")
    Call<List<FriendResponse>> getFriends();

    @GET("friends/requests/incoming")
    Call<List<FriendResponse>> getIncomingRequests();

    @GET("friends/requests/outgoing")
    Call<List<FriendResponse>> getOutgoingRequests();

    @POST("friends/requests/{id}/accept")
    Call<Void> acceptRequest(@Path("id") String friendshipId);

    @POST("friends/requests/{id}/reject")
    Call<Void> rejectRequest(@Path("id") String friendshipId);

    @DELETE("friends/{id}")
    Call<Void> removeFriend(@Path("id") String friendshipId);
}
