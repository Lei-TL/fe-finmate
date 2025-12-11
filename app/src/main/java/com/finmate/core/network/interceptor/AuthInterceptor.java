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

        // Không add token cho các API auth
        if (token != null && !request.url().encodedPath().startsWith("/auth")) {
            request = request.newBuilder()
                    .addHeader("Authorization", "Bearer " + token)
                    .build();
        }

        return chain.proceed(request);
    }
}
