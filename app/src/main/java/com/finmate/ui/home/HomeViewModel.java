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
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.schedulers.Schedulers;

@HiltViewModel
public class HomeViewModel extends ViewModel {

    private final HomeRepository homeRepository;

    private final MutableLiveData<List<WalletEntity>> _wallets = new MutableLiveData<>();
    public LiveData<List<WalletEntity>> wallets = _wallets;

    private final MutableLiveData<List<TransactionEntity>> _transactions = new MutableLiveData<>();
    public LiveData<List<TransactionEntity>> transactions = _transactions;

    private final MutableLiveData<String> _selectedWalletId = new MutableLiveData<>();
    public LiveData<String> selectedWalletId = _selectedWalletId;
    
    private final MutableLiveData<String> _selectedWalletName = new MutableLiveData<>();
    public LiveData<String> selectedWalletName = _selectedWalletName;

    // ✅ Time filter state
    private Long timeFilterStartDate = null; // Timestamp in milliseconds
    private Long timeFilterEndDate = null; // Timestamp in milliseconds

    private final MutableLiveData<Boolean> _isLoading = new MutableLiveData<>();
    public LiveData<Boolean> isLoading = _isLoading;

    @Inject
    public HomeViewModel(HomeRepository homeRepository) {
        this.homeRepository = homeRepository;
    }

    public void loadHomeData() {
        _isLoading.postValue(true); // Dùng postValue để an toàn
        homeRepository.fetchWallets(new HomeRepository.DataCallback<List<WalletEntity>>() {
            @Override
            public void onDataLoaded(List<WalletEntity> data) {
                _wallets.postValue(data);
                _isLoading.postValue(false); // ✅ Tắt loading sau khi load xong
                // ✅ Mặc định load tất cả transactions (không filter theo ví)
                selectWallet(null, null); // null = all wallets
            }

            @Override
            public void onError(String message) {
                _isLoading.postValue(false); // Đảm bảo tắt loading khi có lỗi
                // Xử lý lỗi
            }
        });
    }

    public void selectWallet(String walletId, String walletName) {
        _selectedWalletId.postValue(walletId); // Dùng postValue để an toàn khi gọi từ background thread
        _selectedWalletName.postValue(walletName);
        loadTransactions(walletId, walletName, timeFilterStartDate, timeFilterEndDate);
    }
    
    // ✅ Overload method để backward compatible
    public void selectWallet(String walletId) {
        selectWallet(walletId, null);
    }

    // ✅ Select time filter
    public void selectTimeFilter(Long startDate, Long endDate) {
        timeFilterStartDate = startDate;
        timeFilterEndDate = endDate;
        // Reload transactions with new time filter
        String walletId = _selectedWalletId.getValue();
        String walletName = _selectedWalletName.getValue();
        loadTransactions(walletId, walletName, startDate, endDate);
    }

    private void loadTransactions(String walletId, String walletName) {
        loadTransactions(walletId, walletName, timeFilterStartDate, timeFilterEndDate);
    }

    // ✅ Flag để tránh gọi API nhiều lần cùng lúc
    private boolean isLoadingTransactions = false;
    
    private void loadTransactions(String walletId, String walletName, Long startDate, Long endDate) {
        // ✅ Tránh gọi API nếu đang loading
        if (isLoadingTransactions) {
            return;
        }
        
        isLoadingTransactions = true;
        _isLoading.postValue(true); // Dùng postValue để an toàn
        homeRepository.fetchTransactions(walletId, walletName, startDate, endDate, new HomeRepository.DataCallback<List<TransactionEntity>>() {
            @Override
            public void onDataLoaded(List<TransactionEntity> data) {
                isLoadingTransactions = false;
                _transactions.postValue(data);
                _isLoading.postValue(false);
            }

            @Override
            public void onError(String message) {
                isLoadingTransactions = false;
                _isLoading.postValue(false);
                // Xử lý lỗi
            }
        });
    }

    public void deleteTransaction(long localId) {
        homeRepository.deleteTransaction(localId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(() -> {
                    // Reload transactions after deletion
                    loadTransactions(_selectedWalletId.getValue(), _selectedWalletName.getValue());
                }, throwable -> {
                    // Handle error
                });
    }
}
