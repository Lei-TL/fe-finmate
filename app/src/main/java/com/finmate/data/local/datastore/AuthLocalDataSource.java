package com.finmate.data.local.datastore;

import androidx.datastore.preferences.core.MutablePreferences;
import androidx.datastore.preferences.core.Preferences;
import androidx.datastore.preferences.core.PreferencesKeys;
import androidx.datastore.rxjava3.RxDataStore;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Single;

@Singleton
public class AuthLocalDataSource {

    private final RxDataStore<Preferences> dataStore;
    
    private static final Preferences.Key<String> ACCESS_TOKEN_KEY = PreferencesKeys.stringKey("access_token");
    private static final Preferences.Key<String> REFRESH_TOKEN_KEY = PreferencesKeys.stringKey("refresh_token");

    @Inject
    public AuthLocalDataSource(RxDataStore<Preferences> dataStore) {
        this.dataStore = dataStore;
    }

    public void saveTokens(String accessToken, String refreshToken) {
        dataStore.updateDataAsync(prefs -> {
            MutablePreferences mutablePreferences = prefs.toMutablePreferences();
            mutablePreferences.set(ACCESS_TOKEN_KEY, accessToken);
            mutablePreferences.set(REFRESH_TOKEN_KEY, refreshToken);
            return Single.just(mutablePreferences);
        });
    }

    public Flowable<String> getAccessToken() {
        return dataStore.data()
                .map(prefs -> prefs.get(ACCESS_TOKEN_KEY) != null ? prefs.get(ACCESS_TOKEN_KEY) : "")
                .distinctUntilChanged();
    }
    
    public Single<String> getAccessTokenSingle() {
        return getAccessToken().first("");
    }

    public void clearTokens() {
        dataStore.updateDataAsync(prefs -> {
            MutablePreferences mutablePreferences = prefs.toMutablePreferences();
            mutablePreferences.remove(ACCESS_TOKEN_KEY);
            mutablePreferences.remove(REFRESH_TOKEN_KEY);
            return Single.just(mutablePreferences);
        });
    }

    public Single<String> getRefreshTokenSingle() {
        return dataStore.data()
                .map(prefs -> {
                    String token = prefs.get(REFRESH_TOKEN_KEY);
                    return token != null ? token : "";
                })
                .first("");
    }

}
