package com.finmate.ui.splash;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.finmate.data.repository.AuthRepository;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.schedulers.Schedulers;
import java.util.concurrent.TimeUnit;

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
                .timeout(3, TimeUnit.SECONDS) // Timeout sau 3 giây để tránh ANR
                .subscribeOn(Schedulers.io()) // Chạy trên background thread
                .observeOn(AndroidSchedulers.mainThread()) // Nhận kết quả trên main thread
                .subscribe(
                        isLoggedIn -> _isLoggedIn.postValue(isLoggedIn),
                        error -> _isLoggedIn.postValue(false) // Nếu lỗi hoặc timeout, coi như chưa đăng nhập
                );
    }
}
