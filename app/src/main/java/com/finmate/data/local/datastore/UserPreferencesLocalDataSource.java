package com.finmate.data.local.datastore;

import androidx.datastore.preferences.core.MutablePreferences;
import androidx.datastore.preferences.core.Preferences;
import androidx.datastore.preferences.core.PreferencesKeys;
import androidx.datastore.rxjava3.RxDataStore;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Single;

@Singleton
public class UserPreferencesLocalDataSource {

    private final RxDataStore<Preferences> dataStore;

    private static final Preferences.Key<String> KEY_LANGUAGE =
            PreferencesKeys.stringKey("language");
    private static final Preferences.Key<String> KEY_THEME =
            PreferencesKeys.stringKey("theme");
    private static final Preferences.Key<String> KEY_USER_NAME =
            PreferencesKeys.stringKey("user_name");
    private static final Preferences.Key<String> KEY_USER_EMAIL =
            PreferencesKeys.stringKey("user_email");

    @Inject
    public UserPreferencesLocalDataSource(@Named("settingsDataStore") RxDataStore<Preferences> dataStore) {
        this.dataStore = dataStore;
    }

    public Flowable<String> languageFlow() {
        return dataStore.data().map(prefs -> prefs.get(KEY_LANGUAGE) != null ? prefs.get(KEY_LANGUAGE) : "");
    }

    public Flowable<String> themeFlow() {
        return dataStore.data().map(prefs -> prefs.get(KEY_THEME) != null ? prefs.get(KEY_THEME) : "");
    }

    public Single<String> getLanguage() {
        return dataStore.data().firstOrError()
                .map(prefs -> prefs.get(KEY_LANGUAGE) != null ? prefs.get(KEY_LANGUAGE) : "");
    }

    public Single<String> getTheme() {
        return dataStore.data().firstOrError()
                .map(prefs -> prefs.get(KEY_THEME) != null ? prefs.get(KEY_THEME) : "");
    }

    public Single<String> getUserName() {
        return dataStore.data().firstOrError()
                .map(prefs -> prefs.get(KEY_USER_NAME) != null ? prefs.get(KEY_USER_NAME) : "");
    }

    public Single<String> getUserEmail() {
        return dataStore.data().firstOrError()
                .map(prefs -> prefs.get(KEY_USER_EMAIL) != null ? prefs.get(KEY_USER_EMAIL) : "");
    }

    public Completable saveAppearance(String language, String theme) {
        return dataStore.updateDataAsync(prefs -> {
            MutablePreferences mutable = prefs.toMutablePreferences();
            if (language != null) {
                mutable.set(KEY_LANGUAGE, language);
            }
            if (theme != null) {
                mutable.set(KEY_THEME, theme);
            }
            return Single.just(mutable);
        }).ignoreElement();
    }

    public Completable saveUserProfile(String name, String email) {
        return dataStore.updateDataAsync(prefs -> {
            MutablePreferences mutable = prefs.toMutablePreferences();
            if (name != null) {
                mutable.set(KEY_USER_NAME, name);
            }
            if (email != null) {
                mutable.set(KEY_USER_EMAIL, email);
            }
            return Single.just(mutable);
        }).ignoreElement();
    }
}


