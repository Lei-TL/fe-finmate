package com.finmate.core.network.interceptor;

import androidx.annotation.NonNull;

import com.finmate.core.session.SessionManager;
import com.finmate.data.repository.AuthRepository;

import java.io.IOException;

import javax.inject.Inject;
import javax.inject.Singleton;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

@Singleton
public class AuthInterceptor implements Interceptor {

    private final SessionManager sessionManager;
    private final AuthRepository authRepository;

    private volatile boolean isRefreshing = false;

    @Inject
    public AuthInterceptor(SessionManager sessionManager, AuthRepository authRepository) {
        this.sessionManager = sessionManager;
        this.authRepository = authRepository;
    }

    @NonNull
    @Override
    public Response intercept(@NonNull Chain chain) throws IOException {
        Request originalRequest = chain.request();

        String path = originalRequest.url().encodedPath();
        boolean isAuthEndpoint = path.startsWith("/auth");

        // 1. Gắn access token nếu cần
        String token = sessionManager.getAccessToken();
        Request request = originalRequest;

        if (!isAuthEndpoint && token != null && !token.isEmpty()) {
            request = originalRequest.newBuilder()
                    .addHeader("Authorization", "Bearer " + token)
                    .build();
        }

        // 2. Gửi request lần 1
        Response response = chain.proceed(request);

        // Nếu không phải 401 hoặc là endpoint /auth thì trả về luôn, không refresh
        if (response.code() != 401 || isAuthEndpoint) {
            return response;
        }

        // 3. Gặp 401 -> thử refresh token
        response.close(); // đóng response cũ để tránh leak

        synchronized (this) {
            if (!isRefreshing) {
                isRefreshing = true;
                try {
                    boolean refreshed = authRepository.refreshTokenBlocking();
                    if (!refreshed) {
                        // refresh fail -> coi như hết phiên đăng nhập
                        throw new IOException("Refresh token failed or missing");
                    }
                } finally {
                    isRefreshing = false;
                }
            }
            // Nếu thread này vào sau while refresh đang chạy,
            // đến đây là refresh đã xong (ok hoặc fail).
        }

        // 4. Lấy lại token mới từ SessionManager
        String newToken = sessionManager.getAccessToken();
        if (newToken == null || newToken.isEmpty()) {
            throw new IOException("No access token after refresh");
        }

        // 5. build lại request với token mới
        Request newRequest = originalRequest.newBuilder()
                // xóa header cũ (nếu có)
                .removeHeader("Authorization")
                .addHeader("Authorization", "Bearer " + newToken)
                .build();

        // 6. Gửi lại request
        return chain.proceed(newRequest);
    }
}
