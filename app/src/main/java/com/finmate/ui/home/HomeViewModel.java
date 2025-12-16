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
    
    // ✅ Chart data: Monthly aggregate (tối ưu - chỉ 6 rows thay vì hàng nghìn transactions)
    private final MutableLiveData<List<com.finmate.data.local.database.entity.MonthlyAggregate>> _chartData = new MutableLiveData<>();
    public LiveData<List<com.finmate.data.local.database.entity.MonthlyAggregate>> chartData = _chartData;

    private final MutableLiveData<String> _selectedWalletId = new MutableLiveData<>();
    public LiveData<String> selectedWalletId = _selectedWalletId;
    
    private final MutableLiveData<String> _selectedWalletName = new MutableLiveData<>();
    public LiveData<String> selectedWalletName = _selectedWalletName;

    // ✅ Time filter state
    private Long timeFilterStartDate = null; // Timestamp in milliseconds
    private Long timeFilterEndDate = null; // Timestamp in milliseconds

    private final MutableLiveData<Boolean> _isLoading = new MutableLiveData<>();
    public LiveData<Boolean> isLoading = _isLoading;
    
    private final MutableLiveData<Boolean> _isLoadingMore = new MutableLiveData<>(false);
    public LiveData<Boolean> isLoadingMore = _isLoadingMore;
    
    // ✅ Flag để track xem đã sync trong session này chưa
    private boolean hasSyncedInSession = false;
    
    // ✅ Pagination state
    private static final int PAGE_SIZE = 20; // Số transactions mỗi trang
    private int currentPage = 0;
    private boolean hasMore = true;
    private List<TransactionEntity> allTransactions = new java.util.ArrayList<>(); // Tất cả transactions đã load
    
    // ✅ Cache chart data để tránh reload không cần thiết
    private List<com.finmate.data.local.database.entity.MonthlyAggregate> cachedChartData = null;
    private long lastChartDataLoadTime = 0;
    private static final long CHART_DATA_CACHE_DURATION = 30000; // Cache 30 giây
    
    /**
     * ✅ Reset sync flag - gọi khi quay lại Home để sync lại
     */
    public void resetSyncFlag() {
        hasSyncedInSession = false;
        android.util.Log.d("HomeViewModel", "Sync flag reset, will sync on next loadHomeData");
    }

    @Inject
    public HomeViewModel(HomeRepository homeRepository) {
        this.homeRepository = homeRepository;
    }

    /**
     * ✅ Logic mới: Sync một lần khi khởi tạo (nếu online), sau đó chỉ load từ local
     */
    public void loadHomeData() {
        // 1) Sync từ backend (nếu online và chưa sync trong session này)
        if (!hasSyncedInSession) {
            syncFromBackend(() -> {
                // 2) Sau khi sync xong (hoặc không có mạng), load từ local
                loadFromLocal();
            });
        } else {
            // Đã sync rồi, chỉ load từ local
            loadFromLocal();
        }
    }
    
    /**
     * ✅ Sync từ backend (nếu online)
     */
    private void syncFromBackend(Runnable onComplete) {
        homeRepository.syncFromBackend(new HomeRepository.SyncCallback() {
            @Override
            public void onSyncComplete() {
                hasSyncedInSession = true; // ✅ Đánh dấu đã sync, khóa luồng này
                android.util.Log.d("HomeViewModel", "Sync completed, will only load from local in this session");
                if (onComplete != null) {
                    onComplete.run();
                }
            }

            @Override
            public void onSyncSkipped() {
                // Không có mạng hoặc đã sync rồi → skip
                android.util.Log.d("HomeViewModel", "Sync skipped (no network or already synced)");
                if (onComplete != null) {
                    onComplete.run();
                }
            }

            @Override
            public void onSyncError(String message) {
                android.util.Log.e("HomeViewModel", "Sync error: " + message);
                // Lỗi sync → vẫn load từ local
                if (onComplete != null) {
                    onComplete.run();
                }
            }
        });
    }
    
    /**
     * ✅ Load data từ local Room database
     */
    private void loadFromLocal() {
        _isLoading.postValue(true);
        
        // Load wallets từ local
        homeRepository.loadWalletsFromLocal(new HomeRepository.DataCallback<List<WalletEntity>>() {
            @Override
            public void onDataLoaded(List<WalletEntity> data) {
                _wallets.postValue(data);
                _isLoading.postValue(false);
                // ✅ Mặc định load tất cả transactions (không filter theo ví)
                selectWallet(null, null); // null = all wallets
                
                // ✅ Load chart data (tất cả transactions, không filter)
                loadChartData();
            }

            @Override
            public void onError(String message) {
                _isLoading.postValue(false);
                android.util.Log.e("HomeViewModel", "Error loading wallets from local: " + message);
            }
        });
    }
    
    /**
     * ✅ Load chart data: Monthly aggregate (tối ưu - chỉ 6 rows)
     * ✅ Có cache để tránh reload không cần thiết
     */
    private void loadChartData() {
        long currentTime = System.currentTimeMillis();
        
        // ✅ Nếu có cache và chưa hết hạn → dùng cache
        if (cachedChartData != null && (currentTime - lastChartDataLoadTime) < CHART_DATA_CACHE_DURATION) {
            android.util.Log.d("HomeViewModel", "Using cached chart data");
            _chartData.postValue(cachedChartData);
            return;
        }
        
        // ✅ Load mới từ database
        android.util.Log.d("HomeViewModel", "Loading chart data from database...");
        homeRepository.loadMonthlyAggregateForChart(new HomeRepository.DataCallback<List<com.finmate.data.local.database.entity.MonthlyAggregate>>() {
            @Override
            public void onDataLoaded(List<com.finmate.data.local.database.entity.MonthlyAggregate> data) {
                // ✅ Cache kết quả
                cachedChartData = data != null ? new java.util.ArrayList<>(data) : new java.util.ArrayList<>();
                lastChartDataLoadTime = System.currentTimeMillis();
                _chartData.postValue(cachedChartData);
            }

            @Override
            public void onError(String message) {
                android.util.Log.e("HomeViewModel", "Error loading chart data: " + message);
                _chartData.postValue(new java.util.ArrayList<>());
            }
        });
    }
    
    /**
     * ✅ Invalidate cache khi có transaction mới/thay đổi
     */
    public void invalidateChartCache() {
        cachedChartData = null;
        lastChartDataLoadTime = 0;
        android.util.Log.d("HomeViewModel", "Chart cache invalidated");
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
        // ✅ Reset pagination khi filter thay đổi
        currentPage = 0;
        hasMore = true;
        allTransactions.clear();
        
        loadMoreTransactions(walletId, walletName, startDate, endDate);
    }
    
    /**
     * ✅ Load more transactions (pagination)
     */
    public void loadMoreTransactions() {
        String walletId = _selectedWalletId.getValue();
        String walletName = _selectedWalletName.getValue();
        loadMoreTransactions(walletId, walletName, timeFilterStartDate, timeFilterEndDate);
    }
    
    private void loadMoreTransactions(String walletId, String walletName, Long startDate, Long endDate) {
        // ✅ Tránh gọi nếu đang loading hoặc không còn data
        if (isLoadingTransactions || !hasMore) {
            android.util.Log.d("HomeViewModel", "Already loading or no more data, skipping...");
            return;
        }
        
        android.util.Log.d("HomeViewModel", "loadMoreTransactions called: page=" + currentPage + ", walletId=" + walletId + ", walletName=" + walletName);
        
        isLoadingTransactions = true;
        if (currentPage == 0) {
            _isLoading.postValue(true);
        } else {
            _isLoadingMore.postValue(true);
        }
        
        int offset = currentPage * PAGE_SIZE;
        
        // ✅ Load từ local với pagination
        homeRepository.loadTransactionsFromLocal(walletId, walletName, startDate, endDate, PAGE_SIZE, offset, new HomeRepository.DataCallback<List<TransactionEntity>>() {
            @Override
            public void onDataLoaded(List<TransactionEntity> data) {
                isLoadingTransactions = false;
                
                if (data == null || data.isEmpty()) {
                    hasMore = false;
                    android.util.Log.d("HomeViewModel", "No more transactions to load");
                } else {
                    // ✅ Append vào danh sách hiện tại
                    allTransactions.addAll(data);
                    currentPage++;
                    hasMore = data.size() == PAGE_SIZE; // Còn data nếu load đủ PAGE_SIZE
                    
                    android.util.Log.d("HomeViewModel", "Transactions loaded: " + data.size() + " items, total: " + allTransactions.size() + ", hasMore: " + hasMore);
                }
                
                // ✅ Update LiveData với tất cả transactions đã load
                _transactions.postValue(new java.util.ArrayList<>(allTransactions));
                
                if (currentPage == 1) {
                    _isLoading.postValue(false);
                } else {
                    _isLoadingMore.postValue(false);
                }
            }

            @Override
            public void onError(String message) {
                isLoadingTransactions = false;
                android.util.Log.e("HomeViewModel", "Error loading transactions from local: " + message);
                
                if (currentPage == 0) {
                    _isLoading.postValue(false);
                    _transactions.postValue(new java.util.ArrayList<>());
                } else {
                    _isLoadingMore.postValue(false);
                }
            }
        });
    }

    public void deleteTransaction(long localId) {
        homeRepository.deleteTransaction(localId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(() -> {
                    // ✅ Invalidate chart cache khi xóa transaction
                    invalidateChartCache();
                    // Reload transactions after deletion
                    loadTransactions(_selectedWalletId.getValue(), _selectedWalletName.getValue());
                    // ✅ Reload chart data
                    loadChartData();
                }, throwable -> {
                    // Handle error
                });
    }
}
