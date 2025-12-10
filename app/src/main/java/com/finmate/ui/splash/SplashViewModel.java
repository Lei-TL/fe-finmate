package com.finmate.ui.splash;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.finmate.data.repository.AuthRepository;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class SplashViewModel extends ViewModel {

    private final AuthRepository authRepository;

    private final MutableLiveData<Boolean> _isLoggedIn = new MutableLiveData<>();
    public LiveData<Boolean> isLoggedIn = _isLoggedIn;

    @Inject
    public SplashViewModel(AuthRepository authRepository) {
        this.authRepository = authRepository;
        checkLoginStatus();
    }

    private void checkLoginStatus() {
        authRepository.checkLoginStatus()
                .subscribe(
                        isLoggedIn -> _isLoggedIn.postValue(isLoggedIn),
                        error -> _isLoggedIn.postValue(false)
                );
    }
}
