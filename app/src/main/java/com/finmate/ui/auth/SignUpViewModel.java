package com.finmate.ui.auth;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.finmate.data.repository.AuthRepository;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

@HiltViewModel
public class SignUpViewModel extends ViewModel {

    private final AuthRepository authRepository;
    private final CompositeDisposable disposables = new CompositeDisposable();

    private final MutableLiveData<Boolean> _registerSuccess = new MutableLiveData<>();
    public LiveData<Boolean> registerSuccess = _registerSuccess;

    private final MutableLiveData<String> _errorMessage = new MutableLiveData<>();
    public LiveData<String> errorMessage = _errorMessage;

    private final MutableLiveData<Boolean> _isLoading = new MutableLiveData<>();
    public LiveData<Boolean> isLoading = _isLoading;

    @Inject
    public SignUpViewModel(AuthRepository authRepository) {
        this.authRepository = authRepository;
    }

    public void register(String email, String password) {
        if (email.isEmpty() || password.isEmpty()) {
            _errorMessage.setValue("Email and password cannot be empty");
            return;
        }

        _isLoading.setValue(true);
        disposables.add(authRepository.register(email, password)
                .subscribeOn(Schedulers.io())
                .subscribe(
                        () -> {
                            _isLoading.postValue(false);
                            _registerSuccess.postValue(true);
                        },
                        throwable -> {
                            _isLoading.postValue(false);
                            _errorMessage.postValue(throwable.getMessage() != null ? throwable.getMessage() : "Registration failed");
                        }
                ));
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        disposables.clear();
    }
}
