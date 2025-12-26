package com.finmate.ui.home;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;

import com.finmate.R;
import com.finmate.core.ui.LocaleHelper;
import com.finmate.data.local.database.entity.CategoryEntity;
import com.finmate.data.repository.CategoryRepository;
import com.finmate.ui.activities.AddTransactionActivity;
import com.finmate.ui.auth.AccountActivity;
import com.finmate.ui.settings.SettingsActivity;
import com.finmate.ui.statistics.IncomeStatisticActivity;
import com.finmate.ui.statistics.StatisticActivity;
import com.finmate.ui.transaction.TransactionUIModel;
import com.finmate.ui.transaction.TransactionAdapter;
import com.finmate.ui.transaction.TransactionGroupedItem;
import com.finmate.data.local.database.entity.TransactionEntity;
import com.finmate.ui.wallet.WalletActivity;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class HomeActivity extends AppCompatActivity {

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleHelper.applyLocale(newBase));
    }

    private HomeViewModel viewModel;
    
    @javax.inject.Inject
    CategoryRepository categoryRepository;
    
    @javax.inject.Inject
    com.finmate.core.sync.TransactionSyncManager transactionSyncManager;
    
    @javax.inject.Inject
    com.finmate.core.network.NetworkChecker networkChecker;

    private LineChart lineChart;
    private BottomNavigationView bottomNavigation;
    private RecyclerView rvTransactions;
    private TransactionAdapter transactionAdapter;
    private ImageView btnMenuMore;
    
    // Filter views
    private com.google.android.material.button.MaterialButton btnTimeFilter;
    private ImageView imgAvatar;
    private TextView tvName;
    
    // Summary cards
    private View cardBalance, cardIncome, cardExpense;
    private TextView tvBalance, tvIncome, tvExpense;
    private TextView tvBalanceSubtitle, tvIncomeSubtitle, tvExpenseSubtitle;
    
    // Chart container
    private View chartContainer;
    
    // Time filter state
    private Long timeFilterStartDate = null;
    private Long timeFilterEndDate = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        viewModel = new ViewModelProvider(this).get(HomeViewModel.class);

        mapViews();
        setupMenuMore();
        setupRecyclerView();
        setupBottomNavigation();
        setupSwipeToDelete();
        
        observeViewModel();
        loadCategories(); //  Load categories để lấy icon
        
        //  Đọc time filter từ TimeFilterManager khi khởi tạo
        loadTimeFilterFromManager();
        
        viewModel.loadHomeData(); // Load initial data
        
        //  Bắt đầu auto-sync pending transactions
        if (transactionSyncManager != null) {
            transactionSyncManager.startAutoSync();
        }
    }
    
    // Đọc time filter từ TimeFilterManager và áp dụng
    private void loadTimeFilterFromManager() {
        com.finmate.core.ui.TimeFilterManager.TimeFilterState state = 
            com.finmate.core.ui.TimeFilterManager.getTimeFilter(this);
        
        if (state.startDate != null && state.endDate != null) {
            timeFilterStartDate = state.startDate;
            timeFilterEndDate = state.endDate;
            
            // Set text cho button
            if (state.filterText != null) {
                btnTimeFilter.setText(state.filterText);
            }
            
            // Áp dụng filter vào ViewModel
            viewModel.selectTimeFilter(state.startDate, state.endDate);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        
        // Đọc lại time filter từ TimeFilterManager (có thể đã thay đổi từ StatisticActivity)
        loadTimeFilterFromManager();
        
        // Chỉ reload transactions nếu đang loading hoặc chưa có data
        // Tránh gọi API liên tục khi activity resume
        Boolean isLoading = viewModel.isLoading.getValue();
        List<TransactionEntity> currentTransactions = viewModel.transactions.getValue();
        
        // Chỉ reload nếu chưa có data hoặc đang không loading (tránh duplicate calls)
        if ((currentTransactions == null || currentTransactions.isEmpty()) && (isLoading == null || !isLoading)) {
            // Chỉ reload transactions, không reload wallets (tránh trigger lại selectWallet)
            String walletId = viewModel.selectedWalletId.getValue();
            String walletName = viewModel.selectedWalletName.getValue();
            if (walletId != null || walletName != null) {
                viewModel.selectWallet(walletId, walletName);
            } else {
                viewModel.selectWallet(null, null);
            }
        }
        
        // Sync pending transactions khi có mạng (chỉ sync, không reload)
        if (transactionSyncManager != null) {
            transactionSyncManager.syncPendingTransactions();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Dừng auto-sync khi activity không active
        if (transactionSyncManager != null) {
            transactionSyncManager.stopAutoSync();
        }
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Clear observers để tránh memory leak
        if (viewModel != null) {
            viewModel.wallets.removeObservers(this);
            viewModel.transactions.removeObservers(this);
        }
        // CategoryRepository sử dụng LiveData, observers sẽ tự động được clear khi Activity destroy
    }
    
    // Load categories và tạo map categoryName -> iconName
    // Ưu tiên sync từ backend khi có mạng
    private void loadCategories() {
        categoryRepository.getAll().observe(this, categories -> {
            if (categories != null && !categories.isEmpty()) {
                java.util.Map<String, String> categoryIconMap = new java.util.HashMap<>();
                for (CategoryEntity category : categories) {
                    if (category.getName() != null && category.getIcon() != null) {
                        String categoryName = category.getName().trim();
                        String iconName = category.getIcon().trim();
                        if (!categoryName.isEmpty() && !iconName.isEmpty()) {
                            categoryIconMap.put(categoryName, iconName);
                            categoryIconMap.put(categoryName.toLowerCase(), iconName);
                        }
                    }
                }
                if (transactionAdapter != null) {
                    transactionAdapter.setCategoryIconMap(categoryIconMap);
                }
            } else {
                // Nếu chưa có categories, sync từ backend (ưu tiên)
                android.util.Log.d("HomeActivity", "No categories found, syncing from backend...");
                categoryRepository.fetchRemoteCategoriesByType("INCOME");
                categoryRepository.fetchRemoteCategoriesByType("EXPENSE");
            }
        });

        boolean a = networkChecker.isNetworkAvailable();

        // Sync categories từ backend khi có mạng (để đảm bảo có data mới nhất)
        if (networkChecker != null && true) {
            android.util.Log.d("HomeActivity", "Network available, syncing categories from backend...");
            categoryRepository.fetchRemoteCategoriesByType("INCOME");
            categoryRepository.fetchRemoteCategoriesByType("EXPENSE");
        }
    }

    private void mapViews() {
        lineChart = findViewById(R.id.lineChart);
        bottomNavigation = findViewById(R.id.bottomNavigation);
        rvTransactions = findViewById(R.id.rvTransactions);
        btnMenuMore = findViewById(R.id.btnMenuMore);
        chartContainer = findViewById(R.id.chartContainer);
        
        // Header
        imgAvatar = findViewById(R.id.imgAvatar);
        tvName = findViewById(R.id.tvName);
        
        // Filter views
        btnTimeFilter = findViewById(R.id.btnTimeFilter);
        
        // Summary cards
        cardBalance = findViewById(R.id.cardBalance);
        cardIncome = findViewById(R.id.cardIncome);
        cardExpense = findViewById(R.id.cardExpense);
        tvBalance = findViewById(R.id.tvBalance);
        tvIncome = findViewById(R.id.tvIncome);
        tvExpense = findViewById(R.id.tvExpense);
        tvBalanceSubtitle = findViewById(R.id.tvBalanceSubtitle);
        tvIncomeSubtitle = findViewById(R.id.tvIncomeSubtitle);
        tvExpenseSubtitle = findViewById(R.id.tvExpenseSubtitle);

        bottomNavigation.setSelectedItemId(R.id.nav_home);
        
        setupHeader();
        setupFilters();
        setupCardClicks();
        setupEmptyState();
    }
    
    private void setupHeader() {
        // Lấy fullName từ SharedPreferences
        SharedPreferences prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
        String fullName = prefs.getString("full_name", "");
        if (fullName == null || fullName.isEmpty()) {
            // Fallback: lấy từ user_name (backward compatibility) hoặc default
            fullName = prefs.getString("user_name", "");
            if (fullName.isEmpty()) {
                fullName = getString(R.string.user);
            }
        }
        tvName.setText(fullName);
        
        // Avatar click → AccountActivity
        imgAvatar.setOnClickListener(v -> {
            startActivity(new Intent(this, AccountActivity.class));
        });
    }
    
    private void setupEmptyState() {
        View emptyStateLayout = findViewById(R.id.layoutEmptyState);
        if (emptyStateLayout != null) {
            MaterialButton btnEmptyAdd = emptyStateLayout.findViewById(R.id.btnEmptyAdd);
            if (btnEmptyAdd != null) {
                btnEmptyAdd.setOnClickListener(v -> {
                    startActivity(new Intent(this, AddTransactionActivity.class));
                });
            }
        }
    }
    
    private void setupFilters() {
        // Time filter
        btnTimeFilter.setOnClickListener(v -> {
            com.finmate.ui.dialogs.TimeFilterBottomSheet bottomSheet = 
                com.finmate.ui.dialogs.TimeFilterBottomSheet.newInstance();
            bottomSheet.setListener(new com.finmate.ui.dialogs.TimeFilterBottomSheet.TimeFilterListener() {
                @Override
                public void onTodaySelected() {
                    String filterText = getString(R.string.today);
                    btnTimeFilter.setText(filterText);
                    
                    // Filter transactions for today
                    Calendar cal = Calendar.getInstance();
                    cal.set(Calendar.HOUR_OF_DAY, 0);
                    cal.set(Calendar.MINUTE, 0);
                    cal.set(Calendar.SECOND, 0);
                    cal.set(Calendar.MILLISECOND, 0);
                    Long startOfDay = cal.getTimeInMillis();
                    
                    cal.set(Calendar.HOUR_OF_DAY, 23);
                    cal.set(Calendar.MINUTE, 59);
                    cal.set(Calendar.SECOND, 59);
                    cal.set(Calendar.MILLISECOND, 999);
                    Long endOfDay = cal.getTimeInMillis();
                    
                    // Lưu time filter state
                    timeFilterStartDate = startOfDay;
                    timeFilterEndDate = endOfDay;
                    
                    // Lưu vào TimeFilterManager để share với các activities khác
                    com.finmate.core.ui.TimeFilterManager.saveTimeFilter(HomeActivity.this, startOfDay, endOfDay, filterText);
                    
                    viewModel.selectTimeFilter(startOfDay, endOfDay);
                }

                @Override
                public void onSingleDaySelected(java.util.Date date) {
                    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                    String dateText = sdf.format(date);
                    btnTimeFilter.setText(dateText);
                    
                    // Filter transactions for selected day
                    Calendar cal = Calendar.getInstance();
                    cal.setTime(date);
                    cal.set(Calendar.HOUR_OF_DAY, 0);
                    cal.set(Calendar.MINUTE, 0);
                    cal.set(Calendar.SECOND, 0);
                    cal.set(Calendar.MILLISECOND, 0);
                    Long startOfDay = cal.getTimeInMillis();
                    
                    cal.set(Calendar.HOUR_OF_DAY, 23);
                    cal.set(Calendar.MINUTE, 59);
                    cal.set(Calendar.SECOND, 59);
                    cal.set(Calendar.MILLISECOND, 999);
                    Long endOfDay = cal.getTimeInMillis();
                    
                    // Lưu time filter state
                    timeFilterStartDate = startOfDay;
                    timeFilterEndDate = endOfDay;
                    
                    // Lưu vào TimeFilterManager để share với các activities khác
                    com.finmate.core.ui.TimeFilterManager.saveTimeFilter(HomeActivity.this, startOfDay, endOfDay, dateText);
                    
                    viewModel.selectTimeFilter(startOfDay, endOfDay);
                }

                @Override
                public void onDateRangeSelected(java.util.Date startDate, java.util.Date endDate) {
                    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                    String dateText = sdf.format(startDate) + " - " + sdf.format(endDate);
                    btnTimeFilter.setText(dateText);
                    
                    // Filter transactions for date range
                    Calendar calStart = Calendar.getInstance();
                    calStart.setTime(startDate);
                    calStart.set(Calendar.HOUR_OF_DAY, 0);
                    calStart.set(Calendar.MINUTE, 0);
                    calStart.set(Calendar.SECOND, 0);
                    calStart.set(Calendar.MILLISECOND, 0);
                    Long startTimestamp = calStart.getTimeInMillis();
                    
                    Calendar calEnd = Calendar.getInstance();
                    calEnd.setTime(endDate);
                    calEnd.set(Calendar.HOUR_OF_DAY, 23);
                    calEnd.set(Calendar.MINUTE, 59);
                    calEnd.set(Calendar.SECOND, 59);
                    calEnd.set(Calendar.MILLISECOND, 999);
                    Long endTimestamp = calEnd.getTimeInMillis();
                    
                    // Lưu time filter state
                    timeFilterStartDate = startTimestamp;
                    timeFilterEndDate = endTimestamp;
                    
                    // Lưu vào TimeFilterManager để share với các activities khác
                    com.finmate.core.ui.TimeFilterManager.saveTimeFilter(HomeActivity.this, startTimestamp, endTimestamp, dateText);
                    
                    viewModel.selectTimeFilter(startTimestamp, endTimestamp);
                }

                @Override
                public void onClear() {
                    String filterText = getString(R.string.today);
                    btnTimeFilter.setText(filterText);
                    
                    // Clear filter (set to null = no filter)
                    timeFilterStartDate = null;
                    timeFilterEndDate = null;
                    
                    // Clear trong TimeFilterManager
                    com.finmate.core.ui.TimeFilterManager.clearTimeFilter(HomeActivity.this);
                    
                    viewModel.selectTimeFilter(null, null);
                }
            });
            bottomSheet.show(getSupportFragmentManager(), "TimeFilterBottomSheet");
        });
    }
    
    private void setupCardClicks() {
        // Balance card -> WalletActivity
        cardBalance.setOnClickListener(v -> {
            startActivity(new Intent(this, WalletActivity.class));
        });
        
        // Income card -> IncomeStatisticActivity
        cardIncome.setOnClickListener(v -> {
            startActivity(new Intent(this, IncomeStatisticActivity.class));
        });
        
        // Expense card -> StatisticActivity
        cardExpense.setOnClickListener(v -> {
            startActivity(new Intent(this, com.finmate.ui.statistics.StatisticActivity.class));
        });
    }

    private void observeViewModel() {
        // Calculate total balance from all wallets
        viewModel.wallets.observe(this, wallets -> {
            double totalBalance = 0;
            if (wallets != null && !wallets.isEmpty()) {
                // All wallets: calculate total currentBalance
                for (com.finmate.data.local.database.entity.WalletEntity w : wallets) {
                    totalBalance += w.currentBalance;
                }
            }
            if (tvBalance != null) {
                tvBalance.setText(formatAmount(totalBalance));
            }
        });
        
        viewModel.transactions.observe(this, transactionEntities -> {
            updateTransactionList(transactionEntities);
            
            // Show/hide empty state
            View emptyStateLayout = findViewById(R.id.layoutEmptyState);
            if (emptyStateLayout != null) {
                if (transactionEntities == null || transactionEntities.isEmpty()) {
                    emptyStateLayout.setVisibility(View.VISIBLE);
                    rvTransactions.setVisibility(View.GONE);
                } else {
                    emptyStateLayout.setVisibility(View.GONE);
                    rvTransactions.setVisibility(View.VISIBLE);
                }
            }
            
            // Update summary cards and chart from filtered transactions
            updateSummaryCards(transactionEntities);
            setupChart(transactionEntities); // LiveData observer already runs on main thread
        });
    }
    
    // Calculate and update summary cards (income, expense)
    // Transactions are already filtered by ViewModel by wallet and time filter
    private void updateSummaryCards(List<TransactionEntity> transactions) {
        if (transactions == null || transactions.isEmpty()) {
            if (tvIncome != null) tvIncome.setText("0 VND");
            if (tvExpense != null) tvExpense.setText("0 VND");
            String subtitle = getTimeFilterSubtitle();
            if (tvIncomeSubtitle != null) tvIncomeSubtitle.setText(subtitle);
            if (tvExpenseSubtitle != null) tvExpenseSubtitle.setText(subtitle);
            return;
        }
        
        double totalIncome = 0;
        double totalExpense = 0;
        
        // Calculate from filtered transactions (by wallet and time)
        for (TransactionEntity t : transactions) {
            if (t.type != null && t.type.equals("INCOME")) {
                totalIncome += t.amountDouble;
            } else if (t.type != null && t.type.equals("EXPENSE")) {
                totalExpense += t.amountDouble;
            }
        }
        
        if (tvIncome != null) {
            tvIncome.setText(formatAmount(totalIncome));
        }
        if (tvExpense != null) {
            tvExpense.setText(formatAmount(totalExpense));
        }
        
        // Update subtitle based on selected time filter
        String subtitle = getTimeFilterSubtitle();
        if (tvIncomeSubtitle != null) {
            tvIncomeSubtitle.setText(subtitle);
        }
        if (tvExpenseSubtitle != null) {
            tvExpenseSubtitle.setText(subtitle);
        }
    }
    
    // Format subtitle based on selected time filter
    private String getTimeFilterSubtitle() {
        if (timeFilterStartDate == null && timeFilterEndDate == null) {
            // No filter → show "This month"
            return getString(R.string.this_month);
        }
        
        Calendar calStart = Calendar.getInstance();
        calStart.setTimeInMillis(timeFilterStartDate);
        Calendar calEnd = Calendar.getInstance();
        calEnd.setTimeInMillis(timeFilterEndDate);
        
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        
        // If same day → "Today" or "dd/MM/yyyy"
        if (calStart.get(Calendar.YEAR) == calEnd.get(Calendar.YEAR) &&
            calStart.get(Calendar.DAY_OF_YEAR) == calEnd.get(Calendar.DAY_OF_YEAR)) {
            // Check if it's today
            Calendar today = Calendar.getInstance();
            if (calStart.get(Calendar.YEAR) == today.get(Calendar.YEAR) &&
                calStart.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR)) {
                return getString(R.string.today);
            }
            return sdf.format(calStart.getTime());
        }
        
        // If same month → "Tháng MM/yyyy"
        if (calStart.get(Calendar.YEAR) == calEnd.get(Calendar.YEAR) &&
            calStart.get(Calendar.MONTH) == calEnd.get(Calendar.MONTH)) {
            int month = calStart.get(Calendar.MONTH) + 1;
            int year = calStart.get(Calendar.YEAR);
            return String.format(Locale.getDefault(), "Tháng %d/%d", month, year);
        }
        
        // Date range → "dd/MM/yyyy - dd/MM/yyyy"
        return sdf.format(calStart.getTime()) + " - " + sdf.format(calEnd.getTime());
    }

    private void updateTransactionList(List<TransactionEntity> transactionEntities) {
        if (transactionEntities == null || transactionEntities.isEmpty()) {
            if (transactionAdapter != null) {
                transactionAdapter.updateList(new ArrayList<>());
            }
            return;
        }

        // Group transactions by date
        List<TransactionGroupedItem> groupedItems = groupTransactionsByDate(transactionEntities);
        if (transactionAdapter != null) {
            transactionAdapter.updateList(groupedItems);
        }
    }
    
    private void setupSwipeToDelete() {
        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getAdapterPosition();
                TransactionGroupedItem item = transactionAdapter.getItem(position);
                if (item != null && item.isTransaction()) {
                    viewModel.deleteTransaction(item.getTransaction().localId);
                }
            }
        }).attachToRecyclerView(rvTransactions);
    }
    
    // Group transactions by date and create headers for each day
    private List<TransactionGroupedItem> groupTransactionsByDate(List<TransactionEntity> transactions) {
        List<TransactionGroupedItem> groupedItems = new ArrayList<>();
        
        // Sắp xếp transactions theo date giảm dần (mới nhất lên trên) trước khi group
        List<TransactionEntity> sortedTransactions = new ArrayList<>(transactions);
        sortedTransactions.sort((t1, t2) -> {
            try {
                SimpleDateFormat isoFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                
                java.util.Date date1 = null, date2 = null;
                try {
                    date1 = isoFormat.parse(t1.date);
                } catch (Exception e) {
                    try {
                        date1 = dateFormat.parse(t1.date);
                    } catch (Exception e2) {
                        return 0;
                    }
                }
                
                try {
                    date2 = isoFormat.parse(t2.date);
                } catch (Exception e) {
                    try {
                        date2 = dateFormat.parse(t2.date);
                    } catch (Exception e2) {
                        return 0;
                    }
                }
                
                // Giảm dần: date2.compareTo(date1) - mới nhất lên trên
                int dateCompare = date2.compareTo(date1);
                if (dateCompare != 0) {
                    return dateCompare;
                }
                // Nếu date giống nhau, sort theo id DESC (mới nhất lên trên)
                return Integer.compare(t2.id, t1.id);
            } catch (Exception e) {
                return 0;
            }
        });
        
        // Parse and group transactions by date
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        SimpleDateFormat isoFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
        SimpleDateFormat displayDateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        // Use Vietnamese locale to format day of week
        SimpleDateFormat dayOfWeekFormat = new SimpleDateFormat("EEEE", new Locale("vi", "VN"));
        
        // Map to store transactions by date (LinkedHashMap giữ thứ tự)
        java.util.Map<String, List<TransactionEntity>> transactionsByDate = new java.util.LinkedHashMap<>();
        
        for (TransactionEntity entity : sortedTransactions) {
            if (entity.date == null || entity.date.isEmpty()) continue;
            
            try {
                java.util.Date date;
                // Try parsing as ISO format first
                try {
                    date = isoFormat.parse(entity.date);
                } catch (Exception e) {
                    // Fallback to simple date format
                    date = dateFormat.parse(entity.date);
                }
                
                String dateKey = dateFormat.format(date);
                
                if (!transactionsByDate.containsKey(dateKey)) {
                    transactionsByDate.put(dateKey, new ArrayList<>());
                }
                transactionsByDate.get(dateKey).add(entity);
            } catch (Exception e) {
                // Ignore parse errors
            }
        }
        
        // Sort dates giảm dần (mới nhất lên trên) trước khi tạo grouped items
        List<String> sortedDateKeys = new ArrayList<>(transactionsByDate.keySet());
        sortedDateKeys.sort((d1, d2) -> {
            try {
                java.util.Date date1 = dateFormat.parse(d1);
                java.util.Date date2 = dateFormat.parse(d2);
                return date2.compareTo(date1); // Giảm dần
            } catch (Exception e) {
                return 0;
            }
        });
        
        // Create grouped items with headers for each day (đã sort)
        for (String dateKey : sortedDateKeys) {
            List<TransactionEntity> dayTransactions = transactionsByDate.get(dateKey);
            
            try {
                java.util.Date date = dateFormat.parse(dateKey);
                
                // Format date: "dd/MM/yyyy"
                String dateHeader = displayDateFormat.format(date);
                
                // Format day of week: "Thứ 2", "Thứ 3", ...
                String dayOfWeek = formatDayOfWeek(dayOfWeekFormat.format(date));
                
                // Add header
                groupedItems.add(new TransactionGroupedItem(dateHeader, dayOfWeek));
                
                // Transactions trong mỗi ngày đã được sort từ sortedTransactions, chỉ cần add
                for (TransactionEntity entity : dayTransactions) {
                    TransactionUIModel uiModel = new TransactionUIModel(
                            entity.id,
                            entity.name,
                            entity.category,
                            entity.amount,
                            entity.wallet,
                            entity.date,
                            entity.type // Pass type so adapter can format color and sign
                    );
                    groupedItems.add(new TransactionGroupedItem(uiModel));
                }
            } catch (Exception e) {
                // Ignore parse errors
            }
        }
        
        return groupedItems;
    }
    
    // Format day of week: "Monday" -> "Thứ 2", "Tuesday" -> "Thứ 3", ...
    private String formatDayOfWeek(String dayOfWeek) {
        // Convert to Vietnamese day of week
        java.util.Map<String, String> dayMap = new java.util.HashMap<>();
        dayMap.put("Monday", getString(R.string.monday));
        dayMap.put("Tuesday", getString(R.string.tuesday));
        dayMap.put("Wednesday", getString(R.string.wednesday));
        dayMap.put("Thursday", getString(R.string.thursday));
        dayMap.put("Friday", getString(R.string.friday));
        dayMap.put("Saturday", getString(R.string.saturday));
        dayMap.put("Sunday", getString(R.string.sunday));
        
        // Vietnamese locale (full name)
        dayMap.put("Thứ Hai", getString(R.string.monday));
        dayMap.put("Thứ Ba", getString(R.string.tuesday));
        dayMap.put("Thứ Tư", getString(R.string.wednesday));
        dayMap.put("Thứ Năm", getString(R.string.thursday));
        dayMap.put("Thứ Sáu", getString(R.string.friday));
        dayMap.put("Thứ Bảy", getString(R.string.saturday));
        dayMap.put("Chủ Nhật", getString(R.string.sunday));
        
        // Check if already formatted
        if (dayMap.containsKey(dayOfWeek)) {
            return dayMap.get(dayOfWeek);
        }
        
        // Try to match partial (case insensitive)
        String lowerDay = dayOfWeek.toLowerCase();
        for (java.util.Map.Entry<String, String> entry : dayMap.entrySet()) {
            if (entry.getKey().toLowerCase().contains(lowerDay) || lowerDay.contains(entry.getKey().toLowerCase())) {
                return entry.getValue();
            }
        }
        
        return dayOfWeek; // Return original if no match
    }

    private void setupMenuMore() {
        btnMenuMore.setOnClickListener(v -> {
            PopupMenu popup = new PopupMenu(HomeActivity.this, btnMenuMore);
            popup.getMenuInflater().inflate(R.menu.menu_wallet_options, popup.getMenu());

            popup.setOnMenuItemClickListener(item -> {
                int id = item.getItemId();
                if (id == R.id.action_add_wallet) {
                    startActivity(new Intent(HomeActivity.this, AddWalletActivity.class));
                    return true;
                }
                return false;
            });
            popup.show();
        });
    }


    private void setupRecyclerView() {
        rvTransactions.setLayoutManager(new LinearLayoutManager(this));
        transactionAdapter = new TransactionAdapter(new ArrayList<>()); 
        rvTransactions.setAdapter(transactionAdapter);
        
        // Disable nested scrolling of RecyclerView because there is a NestedScrollView outside
        // NestedScrollView will handle scroll, RecyclerView just displays content
        rvTransactions.setNestedScrollingEnabled(false);
        rvTransactions.setHasFixedSize(false);
    }

    private void setupChart(List<TransactionEntity> transactions) {
        // Check if lineChart is null (safety check)
        if (lineChart == null) {
            android.util.Log.w("HomeActivity", "lineChart is null, cannot setup chart");
            return;
        }
        
        android.util.Log.d("HomeActivity", "setupChart called with " + (transactions != null ? transactions.size() : 0) + " transactions");
        
        if (transactions == null || transactions.isEmpty()) {
            // Hide chart if no data
            android.util.Log.d("HomeActivity", "No transactions, hiding chart");
            if (chartContainer != null) {
                chartContainer.setVisibility(View.GONE);
            }
            return;
        }
        
        // Show chart
        if (chartContainer != null) {
            chartContainer.setVisibility(View.VISIBLE);
            android.util.Log.d("HomeActivity", "Chart container is now visible");
        }
        
        try {
            // Calculate income and expense from transactions (last 6 months)
            Calendar now = Calendar.getInstance();
            String[] months = new String[6];
            double[] monthlyIncome = new double[6];
            double[] monthlyExpense = new double[6];
            
            // Create labels for last 6 months (including current month)
            Calendar cal = Calendar.getInstance();
            for (int i = 5; i >= 0; i--) {
                months[i] = String.format(Locale.getDefault(), "T%d", cal.get(Calendar.MONTH) + 1);
                cal.add(Calendar.MONTH, -1);
            }
            
            // Group transactions by actual month
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            // Try ISO format first, then other formats
            SimpleDateFormat isoFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
            
            for (TransactionEntity t : transactions) {
                if (t.date == null || t.date.isEmpty()) continue;
                
                try {
                    java.util.Date date;
                    // Try parsing as ISO format first
                    try {
                        date = isoFormat.parse(t.date);
                    } catch (Exception e) {
                        // Fallback to simple date format
                        date = dateFormat.parse(t.date);
                    }
                    
                    Calendar transactionCal = Calendar.getInstance();
                    transactionCal.setTime(date);
                    
                    // Calculate difference in months
                    int diffMonths = (now.get(Calendar.YEAR) - transactionCal.get(Calendar.YEAR)) * 12
                            + (now.get(Calendar.MONTH) - transactionCal.get(Calendar.MONTH));
                    
                    // Only include transactions from last 6 months (0 to 5)
                    if (diffMonths >= 0 && diffMonths < 6) {
                        int index = 5 - diffMonths; // 0 = 6 months ago, 5 = this month
                        if ("INCOME".equals(t.type)) {
                            monthlyIncome[index] += t.amountDouble;
                        } else if ("EXPENSE".equals(t.type)) {
                            monthlyExpense[index] += t.amountDouble;
                        }
                    }
                } catch (Exception e) {
                    // Ignore parse errors for individual transactions
                    android.util.Log.d("HomeActivity", "Error parsing transaction date: " + t.date, e);
                }
            }
            
            //  Create chart data
            ArrayList<Entry> income = new ArrayList<>();
            ArrayList<Entry> expense = new ArrayList<>();

            for (int i = 0; i < 6; i++) {
                income.add(new Entry(i + 1, (float) monthlyIncome[i]));
                expense.add(new Entry(i + 1, (float) monthlyExpense[i]));
            }

            LineDataSet incomeSet = new LineDataSet(income, getString(R.string.income_label));
            incomeSet.setColor(Color.GREEN);
            incomeSet.setLineWidth(2f);
            incomeSet.setCircleColor(Color.GREEN);
            incomeSet.setCircleRadius(5f);
            incomeSet.setDrawValues(false);

            LineDataSet expenseSet = new LineDataSet(expense, getString(R.string.expense_label));
            expenseSet.setColor(Color.RED);
            expenseSet.setLineWidth(2f);
            expenseSet.setCircleColor(Color.RED);
            expenseSet.setCircleRadius(5f);
            expenseSet.setDrawValues(false);

            LineData data = new LineData(incomeSet, expenseSet);
            lineChart.setData(data);
            
            //  Improve chart UI
            lineChart.setDrawGridBackground(false);
            lineChart.getDescription().setEnabled(false);
            lineChart.getLegend().setEnabled(true);
            lineChart.getAxisRight().setEnabled(false);
            
            //  X-axis with month labels
            XAxis xAxis = lineChart.getXAxis();
            xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
            xAxis.setGranularity(1f);
            xAxis.setDrawGridLines(false);
            xAxis.setValueFormatter(new ValueFormatter() {
                @Override
                public String getFormattedValue(float value) {
                    int index = (int) value - 1;
                    return (index >= 0 && index < months.length) ? months[index] : "";
                }
            });
            
            //  Y-axis
            YAxis leftAxis = lineChart.getAxisLeft();
            leftAxis.setTextColor(Color.parseColor("#333333"));
            leftAxis.setGridColor(Color.parseColor("#33000000"));
            
            lineChart.invalidate(); // Refresh chart
            android.util.Log.d("HomeActivity", "Chart setup completed successfully");
        } catch (Exception e) {
            android.util.Log.e("HomeActivity", "Error setting up chart", e);
            e.printStackTrace();
            //  Hide chart on error
            if (chartContainer != null) {
                chartContainer.setVisibility(View.GONE);
            }
        }
    }

    private void setupBottomNavigation() {
        bottomNavigation.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            Intent intent = null;

            if (id == R.id.nav_home) {
                return true; // Đang ở Home, không cần navigate
            } else if (id == R.id.nav_wallet) {
                intent = new Intent(this, WalletActivity.class);
            } else if (id == R.id.nav_add) {
                intent = new Intent(this, AddTransactionActivity.class);
            } else if (id == R.id.nav_statistic) {
                intent = new Intent(this, StatisticActivity.class);
            } else if (id == R.id.nav_settings) {
                intent = new Intent(this, SettingsActivity.class);
            }

            if (intent != null) {
                startActivity(intent);
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                finish(); // Close current activity when navigating
            }
            return true;
        });
    }
    
    // Format amount concisely to avoid breaking the layout
    private String formatAmount(double amount) {
        if (amount >= 1_000_000_000) {
            // >= 1 billion: show as 1.2B
            return String.format(Locale.getDefault(), "%.1fB VND", amount / 1_000_000_000);
        } else if (amount >= 1_000_000) {
            // >= 1 million: show as 1.2M
            return String.format(Locale.getDefault(), "%.1fM VND", amount / 1_000_000);
        } else if (amount >= 1_000) {
            // >= 1 thousand: show as 1.2K
            return String.format(Locale.getDefault(), "%.1fK VND", amount / 1_000);
        } else {
            // < 1 thousand: show full amount
            return String.format(Locale.getDefault(), "%,.0f VND", amount);
        }
    }
}
