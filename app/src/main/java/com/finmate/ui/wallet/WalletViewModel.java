package com.finmate.ui.wallet;

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
public class WalletViewModel extends ViewModel {

    private final HomeRepository homeRepository;

    private final MutableLiveData<List<WalletEntity>> _wallets = new MutableLiveData<>();
    public LiveData<List<WalletEntity>> wallets = _wallets;

    private final MutableLiveData<List<TransactionEntity>> _transactions = new MutableLiveData<>();
    public LiveData<List<TransactionEntity>> transactions = _transactions;

    private final MutableLiveData<String> _selectedWalletId = new MutableLiveData<>();
    public LiveData<String> selectedWalletId = _selectedWalletId;
    
    private final MutableLiveData<String> _selectedWalletName = new MutableLiveData<>();
    public LiveData<String> selectedWalletName = _selectedWalletName;

    private final MutableLiveData<Boolean> _isLoading = new MutableLiveData<>();
    public LiveData<Boolean> isLoading = _isLoading;

    @Inject
    public WalletViewModel(HomeRepository homeRepository) {
        this.homeRepository = homeRepository;
    }

    public void loadWalletData() {
        _isLoading.postValue(true);
        homeRepository.fetchWallets(new HomeRepository.DataCallback<List<WalletEntity>>() {
            @Override
            public void onDataLoaded(List<WalletEntity> data) {
                _wallets.postValue(data);
                _isLoading.postValue(false);
                // ✅ Mặc định chọn ví đầu tiên hoặc "Tất cả ví"
                if (data != null && !data.isEmpty()) {
                    selectWallet(null, null); // null = all wallets (mặc định)
                }
            }

            @Override
            public void onError(String message) {
                _isLoading.postValue(false);
            }
        });
    }

    // ✅ Time filter state
    private Long timeFilterStartDate = null;
    private Long timeFilterEndDate = null;

    public void selectWallet(String walletId, String walletName) {
        _selectedWalletId.postValue(walletId);
        _selectedWalletName.postValue(walletName);
        loadTransactions(walletId, walletName, timeFilterStartDate, timeFilterEndDate);
    }

    public void selectTimeFilter(Long startDate, Long endDate) {
        timeFilterStartDate = startDate;
        timeFilterEndDate = endDate;
        // ✅ Reload transactions với time filter
        String walletId = _selectedWalletId.getValue();
        String walletName = _selectedWalletName.getValue();
        loadTransactions(walletId, walletName, startDate, endDate);
    }

    private void loadTransactions(String walletId, String walletName, Long startDate, Long endDate) {
        _isLoading.postValue(true);
        homeRepository.fetchTransactions(walletId, walletName, startDate, endDate, new HomeRepository.DataCallback<List<TransactionEntity>>() {
            @Override
            public void onDataLoaded(List<TransactionEntity> data) {
                _transactions.postValue(data);
                _isLoading.postValue(false);
            }

            @Override
            public void onError(String message) {
                _isLoading.postValue(false);
            }
        });
    }
    
    /**
     * ✅ Xóa transaction
     */
    public void deleteTransaction(long localId) {
        homeRepository.deleteTransaction(localId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(() -> {
                    // ✅ Reload transactions sau khi xóa
                    String walletId = _selectedWalletId.getValue();
                    String walletName = _selectedWalletName.getValue();
                    loadTransactions(walletId, walletName, timeFilterStartDate, timeFilterEndDate);
                }, throwable -> {
                    // Handle error
                    android.util.Log.e("WalletViewModel", "Error deleting transaction: " + throwable.getMessage());
                });
    }
}


