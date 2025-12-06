package com.finmate.ui.home;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.finmate.data.repository.HomeRepository;
import com.finmate.data.local.database.entity.TransactionEntity;
import com.finmate.data.local.database.entity.WalletEntity;

import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class HomeViewModel extends ViewModel {

    private final HomeRepository homeRepository;

    private final MutableLiveData<List<WalletEntity>> _wallets = new MutableLiveData<>();
    public LiveData<List<WalletEntity>> wallets = _wallets;

    private final MutableLiveData<List<TransactionEntity>> _transactions = new MutableLiveData<>();
    public LiveData<List<TransactionEntity>> transactions = _transactions;

    private final MutableLiveData<String> _selectedWalletId = new MutableLiveData<>();
    public LiveData<String> selectedWalletId = _selectedWalletId;

    private final MutableLiveData<Boolean> _isLoading = new MutableLiveData<>();
    public LiveData<Boolean> isLoading = _isLoading;

    @Inject
    public HomeViewModel(HomeRepository homeRepository) {
        this.homeRepository = homeRepository;
    }

    public void loadHomeData() {
        _isLoading.setValue(true);
        homeRepository.fetchWallets(new HomeRepository.DataCallback<List<WalletEntity>>() {
            @Override
            public void onDataLoaded(List<WalletEntity> data) {
                _wallets.postValue(data);
                // Tạm thời chọn ví đầu tiên
                if (data != null && !data.isEmpty()) {
                    selectWallet(String.valueOf(data.get(0).id));
                }
            }

            @Override
            public void onError(String message) {
                // Xử lý lỗi
            }
        });
    }

    public void selectWallet(String walletId) {
        _selectedWalletId.setValue(walletId);
        loadTransactions(walletId);
    }

    private void loadTransactions(String walletId) {
        _isLoading.setValue(true);
        homeRepository.fetchTransactions(walletId, new HomeRepository.DataCallback<List<TransactionEntity>>() {
            @Override
            public void onDataLoaded(List<TransactionEntity> data) {
                _transactions.postValue(data);
                _isLoading.postValue(false);
            }

            @Override
            public void onError(String message) {
                _isLoading.postValue(false);
                // Xử lý lỗi
            }
        });
    }
}
