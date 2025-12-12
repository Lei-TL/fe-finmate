package com.finmate.data.local.datastore;

import android.content.Context;

import androidx.datastore.preferences.core.MutablePreferences;
import androidx.datastore.preferences.core.Preferences;
import androidx.datastore.preferences.core.PreferencesKeys;
import androidx.datastore.rxjava3.RxDataStore;

import com.finmate.core.ui.LocaleHelper;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import dagger.hilt.android.qualifiers.ApplicationContext;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Single;

@Singleton
public class UserPreferencesLocalDataSource {

    private final RxDataStore<Preferences> dataStore;
    private final Context context;
    
    private static final Preferences.Key<String> LANGUAGE_KEY = PreferencesKeys.stringKey("language");

    @Inject
    public UserPreferencesLocalDataSource(
            @Named("user_prefs") RxDataStore<Preferences> dataStore,
            @ApplicationContext Context context
    ) {
        this.dataStore = dataStore;
        this.context = context;
    }

    /**
     * Lưu language code ("vi" hoặc "en")
     * Lưu vào cả DataStore và SharedPreferences (để LocaleHelper có thể đọc synchronous)
     */
    public void saveLanguage(String languageCode) {
        // Lưu vào SharedPreferences trước (synchronous) để LocaleHelper có thể đọc ngay
        LocaleHelper.saveLanguageToPrefs(context, languageCode);
        
        // Lưu vào DataStore (async)
        dataStore.updateDataAsync(prefs -> {
            MutablePreferences mutablePreferences = prefs.toMutablePreferences();
            mutablePreferences.set(LANGUAGE_KEY, languageCode);
            return Single.just(mutablePreferences);
        });
    }

    /**
     * Lấy language code, default là "vi"
     */
    public Flowable<String> getLanguage() {
        return dataStore.data()
                .map(prefs -> {
                    String lang = prefs.get(LANGUAGE_KEY);
                    return lang != null ? lang : "vi"; // Default là tiếng Việt
                })
                .distinctUntilChanged();
    }
    
    /**
     * Lấy language code dạng Single (one-time)
     */
    public Single<String> getLanguageSingle() {
        return getLanguage().first("vi");
    }
    
    /**
     * Lấy language code synchronously từ SharedPreferences (cho LocaleHelper)
     */
    public String getLanguageSync() {
        return LocaleHelper.getLanguage(context);
    }
}

