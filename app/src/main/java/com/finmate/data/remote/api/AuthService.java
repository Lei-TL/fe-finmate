package com.finmate.data.remote.api;

import com.finmate.data.dto.LoginRequest;
import com.finmate.data.dto.RefreshTokenRequest;
import com.finmate.data.dto.RegisterRequest;
import com.finmate.data.dto.TokenResponse;

import io.reactivex.rxjava3.core.Single;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface AuthService {

    @POST("auth/register")
    Single<TokenResponse> register(@Body RegisterRequest request);

    @POST("auth/login")
    Single<TokenResponse> login(@Body LoginRequest request);

    @POST("auth/refresh")
    Single<TokenResponse> refreshToken(@Body RefreshTokenRequest request);
}
