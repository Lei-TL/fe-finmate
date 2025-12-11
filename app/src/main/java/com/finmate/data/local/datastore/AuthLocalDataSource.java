package com.finmate.data.local.datastore;

import androidx.datastore.preferences.core.MutablePreferences;
import androidx.datastore.preferences.core.Preferences;
import androidx.datastore.preferences.core.PreferencesKeys;
import androidx.datastore.rxjava3.RxDataStore;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.inject.Named;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Single;

@Singleton
public class AuthLocalDataSource {

    private final RxDataStore<Preferences> dataStore;

    private static final Preferences.Key<String> KEY_ACCESS_TOKEN =
            PreferencesKeys.stringKey("access_token");
    private static final Preferences.Key<String> KEY_REFRESH_TOKEN =
            PreferencesKeys.stringKey("refresh_token");

    @Inject
    public AuthLocalDataSource(@Named("authDataStore") RxDataStore<Preferences> dataStore) {
        this.dataStore = dataStore;
    }

    public Flowable<String> getAccessTokenFlow() {
        return dataStore.data()
                .map(prefs -> prefs.get(KEY_ACCESS_TOKEN) != null ? prefs.get(KEY_ACCESS_TOKEN) : "");
    }

    public Single<String> getAccessTokenSingle() {
        return dataStore.data().firstOrError()
                .map(prefs -> prefs.get(KEY_ACCESS_TOKEN) != null ? prefs.get(KEY_ACCESS_TOKEN) : "");
    }

    public Single<String> getRefreshTokenSingle() {
        return dataStore.data().firstOrError()
                .map(prefs -> prefs.get(KEY_REFRESH_TOKEN) != null ? prefs.get(KEY_REFRESH_TOKEN) : "");
    }

    public Completable saveTokens(String accessToken, String refreshToken) {
        return dataStore.updateDataAsync(prefs -> {
            MutablePreferences mutable = prefs.toMutablePreferences();
            mutable.set(KEY_ACCESS_TOKEN, accessToken);
            mutable.set(KEY_REFRESH_TOKEN, refreshToken);
            return Single.just(mutable);
        }).ignoreElement();
    }

    public Completable clearTokens() {
        return dataStore.updateDataAsync(prefs -> {
            MutablePreferences mutable = prefs.toMutablePreferences();
            mutable.remove(KEY_ACCESS_TOKEN);
            mutable.remove(KEY_REFRESH_TOKEN);
            return Single.just(mutable);
        }).ignoreElement();
    }
}
