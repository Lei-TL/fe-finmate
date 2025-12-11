package com.finmate.core.di;

import android.content.Context;

import androidx.datastore.preferences.core.Preferences;
import androidx.datastore.preferences.rxjava3.RxPreferenceDataStoreBuilder;
import androidx.datastore.rxjava3.RxDataStore;

import javax.inject.Singleton;
import javax.inject.Named;

import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.android.qualifiers.ApplicationContext;
import dagger.hilt.components.SingletonComponent;

@Module
@InstallIn(SingletonComponent.class)
public class DataStoreModule {

    @Provides
    @Singleton
    @Named("authDataStore")
    public RxDataStore<Preferences> provideAuthDataStore(
            @ApplicationContext Context context
    ) {
        return new RxPreferenceDataStoreBuilder(
                context,
                "auth_prefs"   // file lưu token
        ).build();
    }

    @Provides
    @Singleton
    @Named("settingsDataStore")
    public RxDataStore<Preferences> provideSettingsDataStore(
            @ApplicationContext Context context
    ) {
        return new RxPreferenceDataStoreBuilder(
                context,
                "user_settings"   // file lưu ngôn ngữ/theme/khác
        ).build();
    }
}
