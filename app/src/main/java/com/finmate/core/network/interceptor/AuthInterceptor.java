package com.finmate.core.network.interceptor;

import androidx.annotation.NonNull;

import com.finmate.core.session.SessionManager;

import java.io.IOException;

import javax.inject.Inject;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

public class AuthInterceptor implements Interceptor {

    private final SessionManager sessionManager;

    @Inject
    public AuthInterceptor(SessionManager sessionManager) {
        this.sessionManager = sessionManager;
    }

    @NonNull
    @Override
    public Response intercept(@NonNull Chain chain) throws IOException {

        String token = sessionManager.getAccessToken();
        Request request = chain.request();
        String path = request.url().encodedPath();

        // ✅ Không add token cho các API auth (login, register, refresh)
        // ✅ Nhưng /auth/me cần token
        boolean isAuthEndpoint = path.startsWith("/auth");
        boolean isAuthMe = path.equals("/auth/me");
        
        if (token != null && !token.isEmpty() && (!isAuthEndpoint || isAuthMe)) {
            request = request.newBuilder()
                    .addHeader("Authorization", "Bearer " + token)
                    .build();
        }

        return chain.proceed(request);
    }
}
