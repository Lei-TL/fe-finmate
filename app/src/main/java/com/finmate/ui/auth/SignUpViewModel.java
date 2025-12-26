package com.finmate.ui.auth;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.finmate.data.repository.AuthRepository;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class SignUpViewModel extends ViewModel {

    private final AuthRepository authRepository;
    
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

    public void register(String email, String password, String fullName, String avatarUrl) {
        if (email.isEmpty() || password.isEmpty()) {
            _errorMessage.setValue("Email and password cannot be empty");
            return;
        }
        
        _isLoading.setValue(true);
        authRepository.register(email, password, fullName, avatarUrl, new AuthRepository.RegisterCallback() {
            @Override
            public void onSuccess() {
                _isLoading.setValue(false);
                _registerSuccess.setValue(true);
            }

            @Override
            public void onError(String message) {
                _isLoading.setValue(false);
                _errorMessage.setValue(message);
            }
        });
    }
}
