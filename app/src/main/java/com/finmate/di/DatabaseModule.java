package com.finmate.di;

import android.content.Context;

import com.finmate.database.AppDatabase;
import com.finmate.dto.TokenDao;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.android.qualifiers.ApplicationContext;
import dagger.hilt.components.SingletonComponent;

@Module
@InstallIn(SingletonComponent.class)
public class DatabaseModule {

    @Provides
    @Singleton
    public AppDatabase provideAppDatabase(@ApplicationContext Context context) {
        return AppDatabase.getDatabase(context);
    }

    @Provides
    @Singleton
    public TokenDao provideTokenDao(AppDatabase appDatabase) {
        return appDatabase.tokenDao();
    }
}
