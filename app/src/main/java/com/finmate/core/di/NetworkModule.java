package com.finmate.core.di;

import android.content.Context;

import com.finmate.core.network.interceptor.AuthInterceptor;
import com.finmate.core.session.SessionManager;
import com.finmate.data.remote.api.AuthService;
import com.finmate.data.remote.api.CategoryService;
import com.finmate.data.remote.api.FriendService;
import com.finmate.data.remote.api.TransactionService;
import com.finmate.data.remote.api.WalletService;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.android.qualifiers.ApplicationContext;
import dagger.hilt.components.SingletonComponent;
import androidx.work.WorkManager;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava3.RxJava3CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

@Module
@InstallIn(SingletonComponent.class)
public class NetworkModule {

    private static final String BASE_URL = "http://10.0.2.2:8080/";

    @Provides
    @Singleton
    public AuthService provideAuthService(Retrofit retrofit) {
        return retrofit.create(AuthService.class);
    }

    @Provides
    @Singleton
    public WalletService provideWalletService(Retrofit retrofit) {
        return retrofit.create(WalletService.class);
    }

    @Provides
    @Singleton
    public TransactionService provideTransactionService(Retrofit retrofit) {
        return retrofit.create(TransactionService.class);
    }

    @Provides
    @Singleton
    public CategoryService provideCategoryService(Retrofit retrofit) {
        return retrofit.create(CategoryService.class);
    }

    @Provides
    @Singleton
    public FriendService provideFriendService(Retrofit retrofit) {
        return retrofit.create(FriendService.class);
    }

    @Provides
    @Singleton
    public Retrofit provideRetrofit(OkHttpClient okHttpClient) {
        return new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava3CallAdapterFactory.create())
                .build();
    }

    @Provides
    @Singleton
    public OkHttpClient provideOkHttpClient(AuthInterceptor authInterceptor) {
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);

        return new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS) // Tăng timeout kết nối lên 30s
                .readTimeout(30, TimeUnit.SECONDS)    // Tăng timeout đọc lên 30s
                .writeTimeout(30, TimeUnit.SECONDS)   // Tăng timeout ghi lên 30s
                .addInterceptor(authInterceptor)
                .addInterceptor(logging)
                .retryOnConnectionFailure(true)        // Tự động retry khi mất kết nối
                .build();
    }

    // Explicitly providing AuthInterceptor to resolve potential Hilt issues
    @Provides
    @Singleton
    public AuthInterceptor provideAuthInterceptor(SessionManager sessionManager) {
        return new AuthInterceptor(sessionManager);
    }

    // Provide WorkManager for sync operations
    @Provides
    @Singleton
    public WorkManager provideWorkManager(@ApplicationContext Context context) {
        return WorkManager.getInstance(context);
    }
}
