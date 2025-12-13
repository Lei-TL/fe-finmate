package com.finmate.ui.home;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;

import com.finmate.R;
import com.finmate.core.ui.LocaleHelper;
import com.finmate.data.local.database.entity.CategoryEntity;
import com.finmate.data.repository.CategoryRepository;
import com.finmate.ui.activities.AddTransactionActivity;
import com.finmate.ui.activities.AddWalletActivity;
import com.finmate.ui.activities.AccountActivity;
import com.finmate.ui.activities.SettingsActivity;
import com.finmate.ui.activities.StatisticActivity;
import com.finmate.ui.transaction.TransactionUIModel;
import com.finmate.ui.transaction.TransactionAdapter;
import com.finmate.ui.transaction.TransactionGroupedItem;
import com.finmate.data.local.database.entity.TransactionEntity;
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
    
    // ✅ CategoryRepository để load categories và lấy icon
    @javax.inject.Inject
    CategoryRepository categoryRepository;
    
    // ✅ TransactionSyncManager để auto-sync pending transactions
    @javax.inject.Inject
    com.finmate.core.sync.TransactionSyncManager transactionSyncManager;

    private LineChart lineChart;
    private BottomNavigationView bottomNavigation;
    private RecyclerView rvTransactions;
    private TransactionAdapter transactionAdapter;
    private ImageView btnMenuMore;
    
    // Filter views
    private com.google.android.material.button.MaterialButton btnTimeFilter;
    private TextView tvCurrentFilter, tvSyncStatus;
    private ImageView imgAvatar;
    private TextView tvName;
    
    // Summary cards
    private View cardBalance, cardIncome, cardExpense;
    private TextView tvBalance, tvIncome, tvExpense;
    private TextView tvBalanceSubtitle, tvIncomeSubtitle, tvExpenseSubtitle;
    
    // Chart container
    private View chartContainer;
    
    // ✅ Time filter state
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
        
        observeViewModel();
        loadCategories(); // ✅ Load categories để lấy icon
        viewModel.loadHomeData(); // Load initial data
        
        // ✅ Bắt đầu auto-sync pending transactions
        if (transactionSyncManager != null) {
            transactionSyncManager.startAutoSync();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // ✅ Chỉ reload transactions nếu đang loading hoặc chưa có data
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
        
        // ✅ Sync pending transactions khi có mạng (chỉ sync, không reload)
        if (transactionSyncManager != null) {
            transactionSyncManager.syncPendingTransactions();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        // ✅ Dừng auto-sync khi activity không active
        if (transactionSyncManager != null) {
            transactionSyncManager.stopAutoSync();
        }
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        // ✅ Clear observers để tránh memory leak
        if (viewModel != null) {
            viewModel.wallets.removeObservers(this);
            viewModel.transactions.removeObservers(this);
        }
        // ✅ CategoryRepository sử dụng LiveData, observers sẽ tự động được clear khi Activity destroy
    }
    
    // ✅ Load categories và tạo map categoryName -> iconName
    private void loadCategories() {
        categoryRepository.getAll().observe(this, categories -> {
            if (categories != null && !categories.isEmpty()) {
                java.util.Map<String, String> categoryIconMap = new java.util.HashMap<>();
                for (CategoryEntity category : categories) {
                    if (category.getName() != null && category.getIcon() != null) {
                        // ✅ Lưu cả tên gốc và tên đã normalize để đảm bảo match
                        String categoryName = category.getName().trim();
                        String iconName = category.getIcon().trim();
                        if (!categoryName.isEmpty() && !iconName.isEmpty()) {
                            categoryIconMap.put(categoryName, iconName);
                            // ✅ Thêm cả lowercase version để match case-insensitive
                            categoryIconMap.put(categoryName.toLowerCase(), iconName);
                        }
                    }
                }
                // ✅ Truyền map vào adapter và notify để refresh
                if (transactionAdapter != null) {
                    transactionAdapter.setCategoryIconMap(categoryIconMap);
                }
            } else {
                // ✅ Nếu chưa có categories, sync từ backend
                categoryRepository.fetchRemoteCategoriesByType("INCOME");
                categoryRepository.fetchRemoteCategoriesByType("EXPENSE");
            }
        });
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
        tvCurrentFilter = findViewById(R.id.tvCurrentFilter);
        tvSyncStatus = findViewById(R.id.tvSyncStatus);
        
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
        // ✅ Lấy fullName từ SharedPreferences
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
        
        // ✅ Avatar click → AccountActivity
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
                    btnTimeFilter.setText(getString(R.string.today));
                    updateFilterSubtitle(getString(R.string.today));
                    
                    // ✅ Filter transactions for today
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
                    
                    // ✅ Lưu time filter state
                    timeFilterStartDate = startOfDay;
                    timeFilterEndDate = endOfDay;
                    
                    viewModel.selectTimeFilter(startOfDay, endOfDay);
                }

                @Override
                public void onSingleDaySelected(java.util.Date date) {
                    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                    String dateText = sdf.format(date);
                    btnTimeFilter.setText(dateText);
                    updateFilterSubtitle(dateText);
                    
                    // ✅ Filter transactions for selected day
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
                    
                    // ✅ Lưu time filter state
                    timeFilterStartDate = startOfDay;
                    timeFilterEndDate = endOfDay;
                    
                    viewModel.selectTimeFilter(startOfDay, endOfDay);
                }

                @Override
                public void onDateRangeSelected(java.util.Date startDate, java.util.Date endDate) {
                    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                    String dateText = sdf.format(startDate) + " - " + sdf.format(endDate);
                    btnTimeFilter.setText(dateText);
                    updateFilterSubtitle(dateText);
                    
                    // ✅ Filter transactions for date range
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
                    
                    // ✅ Lưu time filter state
                    timeFilterStartDate = startTimestamp;
                    timeFilterEndDate = endTimestamp;
                    
                    viewModel.selectTimeFilter(startTimestamp, endTimestamp);
                }

                @Override
                public void onClear() {
                    btnTimeFilter.setText(getString(R.string.today));
                    updateFilterSubtitle(getString(R.string.today));
                    
                    // ✅ Clear filter (set to null = no filter)
                    timeFilterStartDate = null;
                    timeFilterEndDate = null;
                    viewModel.selectTimeFilter(null, null);
                }
            });
            bottomSheet.show(getSupportFragmentManager(), "TimeFilterBottomSheet");
        });
        
        // ✅ Initialize filter subtitle
        updateFilterSubtitle(getString(R.string.today));
    }
    
    // ✅ Cập nhật filter subtitle (chỉ hiển thị time filter)
    private void updateFilterSubtitle(String timeFilter) {
        if (tvCurrentFilter != null) {
            tvCurrentFilter.setText(timeFilter);
        }
    }
    
    private void setupCardClicks() {
        // Balance card -> WalletActivity
        cardBalance.setOnClickListener(v -> {
            startActivity(new Intent(this, WalletActivity.class));
        });
        
        // Income card -> IncomeStatisticActivity
        cardIncome.setOnClickListener(v -> {
            startActivity(new Intent(this, com.finmate.ui.activities.IncomeStatisticActivity.class));
        });
        
        // Expense card -> StatisticActivity
        cardExpense.setOnClickListener(v -> {
            startActivity(new Intent(this, com.finmate.ui.activities.StatisticActivity.class));
        });
    }

    private void observeViewModel() {
        // ✅ Calculate total balance từ tất cả ví
        viewModel.wallets.observe(this, wallets -> {
            if (wallets != null && !wallets.isEmpty()) {
                // ✅ Tất cả ví: tính tổng currentBalance
                double totalBalance = 0;
                for (com.finmate.data.local.database.entity.WalletEntity w : wallets) {
                    totalBalance += w.currentBalance;
                }
                if (tvBalance != null) {
                    tvBalance.setText(formatAmount(totalBalance));
                }
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
            
            // ✅ Update summary cards và chart từ transactions đã filter
            updateSummaryCards(transactionEntities);
            setupChart(transactionEntities);
        });
    }
    
    // ✅ Cập nhật sync status (hiển thị thời gian cập nhật cuối)
    private void updateSyncStatus() {
        if (tvSyncStatus != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
            String time = sdf.format(Calendar.getInstance().getTime());
            tvSyncStatus.setText(getString(R.string.last_updated_at, time));
        }
    }
    
    // ✅ Tính toán và update summary cards (income, expense)
    // ✅ Transactions đã được filter từ ViewModel theo wallet và time filter
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
        
        // ✅ Tính toán từ transactions đã được filter (theo wallet và time)
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
        
        // ✅ Update subtitle theo time filter đã chọn
        String subtitle = getTimeFilterSubtitle();
        if (tvIncomeSubtitle != null) {
            tvIncomeSubtitle.setText(subtitle);
        }
        if (tvExpenseSubtitle != null) {
            tvExpenseSubtitle.setText(subtitle);
        }
    }
    
    // ✅ Format subtitle dựa trên time filter đã chọn
    private String getTimeFilterSubtitle() {
        if (timeFilterStartDate == null && timeFilterEndDate == null) {
            // Không có filter → hiển thị "Tháng này"
            Calendar cal = Calendar.getInstance();
            int nowMonth = cal.get(Calendar.MONTH) + 1;
            int nowYear = cal.get(Calendar.YEAR);
            return String.format(Locale.getDefault(), "Tháng %d/%d", nowMonth, nowYear);
        }
        
        Calendar calStart = Calendar.getInstance();
        calStart.setTimeInMillis(timeFilterStartDate);
        Calendar calEnd = Calendar.getInstance();
        calEnd.setTimeInMillis(timeFilterEndDate);
        
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        
        // Nếu cùng một ngày → "Hôm nay" hoặc "dd/MM/yyyy"
        if (calStart.get(Calendar.YEAR) == calEnd.get(Calendar.YEAR) &&
            calStart.get(Calendar.DAY_OF_YEAR) == calEnd.get(Calendar.DAY_OF_YEAR)) {
            // Kiểm tra xem có phải hôm nay không
            Calendar today = Calendar.getInstance();
            if (calStart.get(Calendar.YEAR) == today.get(Calendar.YEAR) &&
                calStart.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR)) {
                return getString(R.string.today);
            }
            return sdf.format(calStart.getTime());
        }
        
        // Nếu cùng một tháng → "Tháng MM/yyyy"
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

        // ✅ Group transactions theo ngày
        List<TransactionGroupedItem> groupedItems = groupTransactionsByDate(transactionEntities);
        if (transactionAdapter != null) {
            transactionAdapter.updateList(groupedItems);
        }
    }
    
    // ✅ Group transactions theo ngày và tạo header cho mỗi ngày
    private List<TransactionGroupedItem> groupTransactionsByDate(List<TransactionEntity> transactions) {
        List<TransactionGroupedItem> groupedItems = new ArrayList<>();
        
        // Parse và group transactions theo ngày
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        SimpleDateFormat isoFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
        SimpleDateFormat displayDateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        // Sử dụng locale tiếng Việt để format thứ
        SimpleDateFormat dayOfWeekFormat = new SimpleDateFormat("EEEE", new Locale("vi", "VN"));
        
        // Map để lưu transactions theo ngày
        java.util.Map<String, List<TransactionEntity>> transactionsByDate = new java.util.LinkedHashMap<>();
        
        for (TransactionEntity entity : transactions) {
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
        
        // Tạo grouped items với header cho mỗi ngày
        for (java.util.Map.Entry<String, List<TransactionEntity>> entry : transactionsByDate.entrySet()) {
            String dateKey = entry.getKey();
            List<TransactionEntity> dayTransactions = entry.getValue();
            
            try {
                java.util.Date date = dateFormat.parse(dateKey);
                
                // Format ngày: "dd/MM/yyyy"
                String dateHeader = displayDateFormat.format(date);
                
                // Format thứ: "Thứ 2", "Thứ 3", ...
                String dayOfWeek = formatDayOfWeek(dayOfWeekFormat.format(date));
                
                // Thêm header
                groupedItems.add(new TransactionGroupedItem(dateHeader, dayOfWeek));
                
                // Thêm transactions của ngày đó
                for (TransactionEntity entity : dayTransactions) {
                    TransactionUIModel uiModel = new TransactionUIModel(
                            entity.name,
                            entity.category,
                            entity.amount,
                            entity.wallet,
                            entity.date,
                            entity.type // ✅ Truyền type để adapter có thể format màu và dấu
                    );
                    groupedItems.add(new TransactionGroupedItem(uiModel));
                }
            } catch (Exception e) {
                // Ignore parse errors
            }
        }
        
        return groupedItems;
    }
    
    // ✅ Format day of week: "Monday" -> "Thứ 2", "Tuesday" -> "Thứ 3", ...
    private String formatDayOfWeek(String dayOfWeek) {
        // Convert to Vietnamese day of week
        java.util.Map<String, String> dayMap = new java.util.HashMap<>();
        dayMap.put("Monday", "Thứ 2");
        dayMap.put("Tuesday", "Thứ 3");
        dayMap.put("Wednesday", "Thứ 4");
        dayMap.put("Thursday", "Thứ 5");
        dayMap.put("Friday", "Thứ 6");
        dayMap.put("Saturday", "Thứ 7");
        dayMap.put("Sunday", "Chủ nhật");
        
        // Vietnamese locale (full name)
        dayMap.put("Thứ Hai", "Thứ 2");
        dayMap.put("Thứ Ba", "Thứ 3");
        dayMap.put("Thứ Tư", "Thứ 4");
        dayMap.put("Thứ Năm", "Thứ 5");
        dayMap.put("Thứ Sáu", "Thứ 6");
        dayMap.put("Thứ Bảy", "Thứ 7");
        dayMap.put("Chủ Nhật", "Chủ nhật");
        
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
                // ✅ Bỏ "Chọn ví" khỏi menu, chỉ giữ "Thêm ví"
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
        
        // ✅ Disable nested scrolling của RecyclerView vì đã có NestedScrollView bên ngoài
        // NestedScrollView sẽ handle scroll, RecyclerView chỉ hiển thị content
        rvTransactions.setNestedScrollingEnabled(false);
        rvTransactions.setHasFixedSize(false);
    }

    private void setupChart(List<TransactionEntity> transactions) {
        if (transactions == null || transactions.isEmpty()) {
            // ✅ Ẩn chart nếu không có data
            if (chartContainer != null) {
                chartContainer.setVisibility(View.GONE);
            }
            return;
        }
        
        // ✅ Hiển thị chart
        if (chartContainer != null) {
            chartContainer.setVisibility(View.VISIBLE);
        }
        
        // ✅ Tính toán income và expense từ transactions (6 tháng gần nhất)
        Calendar now = Calendar.getInstance();
        String[] months = new String[6];
        double[] monthlyIncome = new double[6];
        double[] monthlyExpense = new double[6];
        
        // ✅ Tạo labels cho 6 tháng gần nhất (bao gồm tháng hiện tại)
        Calendar cal = Calendar.getInstance();
        for (int i = 5; i >= 0; i--) {
            months[i] = String.format(Locale.getDefault(), "T%d", cal.get(Calendar.MONTH) + 1);
            cal.add(Calendar.MONTH, -1);
        }
        
        // ✅ Group transactions by month thực tế
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
                    int index = 5 - diffMonths; // 0 = 6 tháng trước, 5 = tháng này
                    if ("INCOME".equals(t.type)) {
                        monthlyIncome[index] += t.amountDouble;
                    } else if ("EXPENSE".equals(t.type)) {
                        monthlyExpense[index] += t.amountDouble;
                    }
                }
            } catch (Exception e) {
                // Ignore parse errors
            }
        }
        
        // ✅ Tạo chart data
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
        
        // ✅ Cải thiện UI chart
        lineChart.setDrawGridBackground(false);
        lineChart.getDescription().setEnabled(false);
        lineChart.getLegend().setEnabled(true);
        lineChart.getAxisRight().setEnabled(false);
        
        // ✅ X-axis với labels tháng
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
        
        // ✅ Y-axis
        YAxis leftAxis = lineChart.getAxisLeft();
        leftAxis.setTextColor(Color.parseColor("#FFFFFF"));
        leftAxis.setGridColor(Color.parseColor("#33FFFFFF"));
        
        lineChart.invalidate(); // Refresh chart
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
            }
            return true;
        });
    }
    
    // ✅ Format số tiền ngắn gọn để không làm vỡ layout
    private String formatAmount(double amount) {
        if (amount >= 1_000_000_000) {
            // >= 1 tỷ: hiển thị dạng 1.2B
            return String.format(Locale.getDefault(), "%.1fB VND", amount / 1_000_000_000);
        } else if (amount >= 1_000_000) {
            // >= 1 triệu: hiển thị dạng 1.2M
            return String.format(Locale.getDefault(), "%.1fM VND", amount / 1_000_000);
        } else if (amount >= 1_000) {
            // >= 1 nghìn: hiển thị dạng 1.2K
            return String.format(Locale.getDefault(), "%.1fK VND", amount / 1_000);
        } else {
            // < 1 nghìn: hiển thị đầy đủ
            return String.format(Locale.getDefault(), "%,.0f VND", amount);
        }
    }
}
