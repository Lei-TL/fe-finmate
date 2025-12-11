package com.finmate.core.di;

import com.finmate.core.network.interceptor.AuthInterceptor;
import com.finmate.core.session.SessionManager;
import com.finmate.data.remote.api.AuthService;
import com.finmate.data.remote.api.CategoryService;
import com.finmate.data.remote.api.FriendService;
import com.finmate.data.remote.api.TransactionService;
import com.finmate.data.remote.api.WalletService;
import com.finmate.data.repository.AuthRepository;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.components.SingletonComponent;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava3.RxJava3CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

@Module
@InstallIn(SingletonComponent.class)
public class NetworkModule {

    private static final String BASE_URL = "http://10.0.2.2:8080/";

    // Tạo OkHttpClient không có AuthInterceptor cho AuthService (tránh dependency cycle)
    @Provides
    @Singleton
    @javax.inject.Named("authOkHttpClient")
    public OkHttpClient provideAuthOkHttpClient() {
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);

        return new OkHttpClient.Builder()
                .addInterceptor(logging)
                .build();
    }

    // Retrofit riêng cho AuthService (không có AuthInterceptor)
    @Provides
    @Singleton
    @javax.inject.Named("authRetrofit")
    public Retrofit provideAuthRetrofit(@javax.inject.Named("authOkHttpClient") OkHttpClient okHttpClient) {
        return new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(okHttpClient)
                .addCallAdapterFactory(RxJava3CallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .build();
    }

    // Retrofit chính cho các service khác (có AuthInterceptor)
    @Provides
    @Singleton
    public Retrofit provideRetrofit(OkHttpClient okHttpClient) {
        return new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(okHttpClient)
                .addCallAdapterFactory(RxJava3CallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .build();
    }

    // AuthService dùng Retrofit riêng (không có AuthInterceptor)
    @Provides
    @Singleton
    public AuthService provideAuthService(@javax.inject.Named("authRetrofit") Retrofit retrofit) {
        return retrofit.create(AuthService.class);
    }

    @Provides
    @Singleton
    public TransactionService provideTransactionService(Retrofit retrofit) {
        return retrofit.create(TransactionService.class);
    }

    @Provides
    @Singleton
    public FriendService provideFriendService(Retrofit retrofit) {
        return retrofit.create(FriendService.class);
    }

    @Provides
    @Singleton
    public CategoryService provideCategoryService(Retrofit retrofit) {
        return retrofit.create(CategoryService.class);
    }

    @Provides
    @Singleton
    public WalletService provideWalletService(Retrofit retrofit) {
        return retrofit.create(WalletService.class);
    }

    @Provides
    @Singleton
    public OkHttpClient provideOkHttpClient(AuthInterceptor authInterceptor) {
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);

        return new OkHttpClient.Builder()
                .addInterceptor(authInterceptor)
                .addInterceptor(logging)
                .build();
    }

    @Provides
    @Singleton
    public AuthInterceptor provideAuthInterceptor(SessionManager sessionManager, AuthRepository authRepository) {
        return new AuthInterceptor(sessionManager, authRepository);
    }
}
