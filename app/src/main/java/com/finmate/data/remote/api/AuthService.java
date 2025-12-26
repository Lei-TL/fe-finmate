package com.finmate.data.remote.api;

import com.finmate.data.dto.LoginRequest;
import com.finmate.data.dto.RegisterRequest;
import com.finmate.data.dto.TokenResponse;
import com.finmate.data.dto.RefreshTokenRequest;
import com.finmate.data.dto.UpdateUserRequest;
import com.finmate.data.dto.UserInfoResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;

public interface AuthService {
    @POST("auth/register")
    Call<TokenResponse> register(@Body RegisterRequest request);

    @POST("auth/login")
    Call<TokenResponse> login(@Body LoginRequest request);

    @POST("auth/refresh")
    Call<TokenResponse> refresh(@Body RefreshTokenRequest request);

    @GET("auth/me")
    Call<UserInfoResponse> getCurrentUser();

    @PUT("auth/me")
    Call<UserInfoResponse> updateCurrentUser(@Body UpdateUserRequest request);
}
