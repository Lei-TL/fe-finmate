package com.finmate.ui.home;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.finmate.R;
import com.finmate.data.local.database.entity.TransactionEntity;
import com.finmate.data.local.database.entity.WalletEntity;
import com.finmate.ui.activities.AddTransactionActivity;
import com.finmate.ui.activities.SettingsActivity;
import com.finmate.ui.activities.StatisticActivity;
import com.finmate.ui.base.BaseActivity;
import com.finmate.ui.transaction.TransactionAdapter;
import com.finmate.ui.transaction.TransactionGroupedItem;
import com.finmate.ui.transaction.TransactionUIModel;
import com.finmate.data.repository.CategoryRepository;
import com.finmate.data.local.database.entity.CategoryEntity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.chip.Chip;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class WalletActivity extends BaseActivity {

    private WalletViewModel viewModel;
    
    // ✅ CategoryRepository để load categories và lấy icon
    @javax.inject.Inject
    CategoryRepository categoryRepository;
    
    private Chip chipWalletFilter, chipTimeFilter;
    private TextView tvBalance, tvIncomeValue, tvExpenseValue, tvWalletName;
    private ProgressBar progIncome, progExpense;
    private BottomNavigationView bottomNavigation;
    private RecyclerView rvTransactions;
    private TransactionAdapter transactionAdapter;
    private View layoutEmptyState;
    private android.widget.ImageView btnAddTransaction;
    
    private String selectedWalletId = null;
    private String selectedWalletName = "";
    
    // ✅ Time filter state
    private Long timeFilterStartDate = null;
    private Long timeFilterEndDate = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wallet);

        viewModel = new ViewModelProvider(this).get(WalletViewModel.class);

        // ÁNH XẠ VIEW
        chipWalletFilter = findViewById(R.id.chipWalletFilter);
        chipTimeFilter = findViewById(R.id.chipTimeFilter);
        tvBalance = findViewById(R.id.tv_balance);
        tvWalletName = findViewById(R.id.tv_wallet_name);
        tvIncomeValue = findViewById(R.id.tv_income_value);
        tvExpenseValue = findViewById(R.id.tv_expense_value);
        btnAddTransaction = findViewById(R.id.btnAddTransaction);

        progIncome = findViewById(R.id.prog_income);
        progExpense = findViewById(R.id.prog_expense);

        bottomNavigation = findViewById(R.id.bottomNavigation);
        rvTransactions = findViewById(R.id.rvTransactions);
        layoutEmptyState = findViewById(R.id.layoutEmptyState);

        // Chọn đúng tab hiện tại
        bottomNavigation.setSelectedItemId(R.id.nav_wallet);

        setupRecyclerView();
        setupWalletFilter();
        setupTimeFilter();
        setupAddTransactionButton();
        observeViewModel();
        setupBottomNavigation();
        loadCategories(); // ✅ Load categories để lấy icon
        
        viewModel.loadWalletData();
    }
    
    // ✅ Load categories và tạo map categoryName -> iconName
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
    
    private void setupRecyclerView() {
        rvTransactions.setLayoutManager(new LinearLayoutManager(this));
        transactionAdapter = new TransactionAdapter(new ArrayList<>());
        rvTransactions.setAdapter(transactionAdapter);
        rvTransactions.setNestedScrollingEnabled(false);
        rvTransactions.setHasFixedSize(false);
    }

    private void setupWalletFilter() {
        chipWalletFilter.setOnClickListener(v -> showWalletMenu());
    }
    
    private void setupTimeFilter() {
        // ✅ Mặc định: Tháng này
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.DAY_OF_MONTH, 1);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        timeFilterStartDate = cal.getTimeInMillis();
        
        cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH));
        cal.set(Calendar.HOUR_OF_DAY, 23);
        cal.set(Calendar.MINUTE, 59);
        cal.set(Calendar.SECOND, 59);
        cal.set(Calendar.MILLISECOND, 999);
        timeFilterEndDate = cal.getTimeInMillis();
        
        chipTimeFilter.setText(getString(R.string.this_month));
        
        chipTimeFilter.setOnClickListener(v -> {
            com.finmate.ui.dialogs.TimeFilterBottomSheet bottomSheet = 
                com.finmate.ui.dialogs.TimeFilterBottomSheet.newInstance();
            bottomSheet.setListener(new com.finmate.ui.dialogs.TimeFilterBottomSheet.TimeFilterListener() {
                @Override
                public void onTodaySelected() {
                    chipTimeFilter.setText(getString(R.string.today));
                    
                    Calendar cal = Calendar.getInstance();
                    cal.set(Calendar.HOUR_OF_DAY, 0);
                    cal.set(Calendar.MINUTE, 0);
                    cal.set(Calendar.SECOND, 0);
                    cal.set(Calendar.MILLISECOND, 0);
                    timeFilterStartDate = cal.getTimeInMillis();
                    
                    cal.set(Calendar.HOUR_OF_DAY, 23);
                    cal.set(Calendar.MINUTE, 59);
                    cal.set(Calendar.SECOND, 59);
                    cal.set(Calendar.MILLISECOND, 999);
                    timeFilterEndDate = cal.getTimeInMillis();
                    
                    viewModel.selectTimeFilter(timeFilterStartDate, timeFilterEndDate);
                }

                @Override
                public void onSingleDaySelected(java.util.Date date) {
                    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                    String dateText = sdf.format(date);
                    chipTimeFilter.setText(dateText);
                    
                    Calendar cal = Calendar.getInstance();
                    cal.setTime(date);
                    cal.set(Calendar.HOUR_OF_DAY, 0);
                    cal.set(Calendar.MINUTE, 0);
                    cal.set(Calendar.SECOND, 0);
                    cal.set(Calendar.MILLISECOND, 0);
                    timeFilterStartDate = cal.getTimeInMillis();
                    
                    cal.set(Calendar.HOUR_OF_DAY, 23);
                    cal.set(Calendar.MINUTE, 59);
                    cal.set(Calendar.SECOND, 59);
                    cal.set(Calendar.MILLISECOND, 999);
                    timeFilterEndDate = cal.getTimeInMillis();
                    
                    viewModel.selectTimeFilter(timeFilterStartDate, timeFilterEndDate);
                }

                @Override
                public void onDateRangeSelected(java.util.Date startDate, java.util.Date endDate) {
                    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                    String dateText = sdf.format(startDate) + " - " + sdf.format(endDate);
                    chipTimeFilter.setText(dateText);
                    
                    Calendar calStart = Calendar.getInstance();
                    calStart.setTime(startDate);
                    calStart.set(Calendar.HOUR_OF_DAY, 0);
                    calStart.set(Calendar.MINUTE, 0);
                    calStart.set(Calendar.SECOND, 0);
                    calStart.set(Calendar.MILLISECOND, 0);
                    timeFilterStartDate = calStart.getTimeInMillis();
                    
                    Calendar calEnd = Calendar.getInstance();
                    calEnd.setTime(endDate);
                    calEnd.set(Calendar.HOUR_OF_DAY, 23);
                    calEnd.set(Calendar.MINUTE, 59);
                    calEnd.set(Calendar.SECOND, 59);
                    calEnd.set(Calendar.MILLISECOND, 999);
                    timeFilterEndDate = calEnd.getTimeInMillis();
                    
                    viewModel.selectTimeFilter(timeFilterStartDate, timeFilterEndDate);
                }

                @Override
                public void onClear() {
                    chipTimeFilter.setText(getString(R.string.this_month));
                    
                    // Reset về tháng này
                    Calendar cal = Calendar.getInstance();
                    cal.set(Calendar.DAY_OF_MONTH, 1);
                    cal.set(Calendar.HOUR_OF_DAY, 0);
                    cal.set(Calendar.MINUTE, 0);
                    cal.set(Calendar.SECOND, 0);
                    cal.set(Calendar.MILLISECOND, 0);
                    timeFilterStartDate = cal.getTimeInMillis();
                    
                    cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH));
                    cal.set(Calendar.HOUR_OF_DAY, 23);
                    cal.set(Calendar.MINUTE, 59);
                    cal.set(Calendar.SECOND, 59);
                    cal.set(Calendar.MILLISECOND, 999);
                    timeFilterEndDate = cal.getTimeInMillis();
                    
                    viewModel.selectTimeFilter(timeFilterStartDate, timeFilterEndDate);
                }
            });
            bottomSheet.show(getSupportFragmentManager(), "TimeFilterBottomSheet");
        });
    }
    
    private void setupAddTransactionButton() {
        btnAddTransaction.setOnClickListener(v -> {
            Intent intent = new Intent(this, AddTransactionActivity.class);
            // ✅ Nếu đã chọn ví cụ thể, có thể truyền walletId để pre-select
            if (selectedWalletId != null) {
                intent.putExtra("walletId", selectedWalletId);
            }
            startActivity(intent);
        });
    }
    
    private void showWalletMenu() {
        List<WalletEntity> wallets = viewModel.wallets.getValue();
        
        if (wallets == null || wallets.isEmpty()) {
            Toast.makeText(this, getString(R.string.no_wallets), Toast.LENGTH_SHORT).show();
            return;
        }

        PopupMenu popupMenu = new PopupMenu(this, chipWalletFilter);
        
        // Thêm "Tất cả ví" vào menu
        popupMenu.getMenu().add(0, 0, 0, getString(R.string.all_wallets));
        
        // Thêm các ví từ database vào menu
        for (int i = 0; i < wallets.size(); i++) {
            WalletEntity wallet = wallets.get(i);
            popupMenu.getMenu().add(0, i + 1, i + 1, wallet.name);
        }
        
        popupMenu.setOnMenuItemClickListener(item -> {
            int itemId = item.getItemId();
            
            if (itemId == 0) {
                selectedWalletName = getString(R.string.all_wallets);
                selectedWalletId = null;
                chipWalletFilter.setText(selectedWalletName);
                updateWalletName(selectedWalletName);
                viewModel.selectWallet(null, null);
            } else {
                int walletIndex = itemId - 1;
                if (walletIndex >= 0 && walletIndex < wallets.size()) {
                    WalletEntity selectedWallet = wallets.get(walletIndex);
                    selectedWalletId = selectedWallet.id;
                    selectedWalletName = selectedWallet.name;
                    chipWalletFilter.setText(selectedWalletName);
                    updateWalletName(selectedWalletName);
                    viewModel.selectWallet(selectedWalletId, selectedWalletName);
                }
            }
            return true;
        });
        
        popupMenu.show();
    }
    
    private void observeViewModel() {
        viewModel.wallets.observe(this, wallets -> {
            if (wallets != null && !wallets.isEmpty() && selectedWalletName.isEmpty()) {
                selectedWalletName = getString(R.string.all_wallets);
                selectedWalletId = null;
                chipWalletFilter.setText(selectedWalletName);
                // ✅ Cập nhật tên ví trên card
                updateWalletName(selectedWalletName);
                // ✅ Tự động load transactions cho "Tất cả ví" khi wallets được load lần đầu
                viewModel.selectWallet(null, null);
            }
            updateBalance(wallets);
        });
        
        viewModel.transactions.observe(this, transactions -> {
            updateWalletData(transactions);
            updateTransactionList(transactions);
        });
    }
    
    private void updateWalletName(String walletName) {
        if (tvWalletName != null) {
            tvWalletName.setText(walletName);
        }
    }
    
    private void updateTransactionList(List<TransactionEntity> transactionEntities) {
        if (transactionEntities == null || transactionEntities.isEmpty()) {
            if (transactionAdapter != null) {
                transactionAdapter.updateList(new ArrayList<>());
            }
            if (layoutEmptyState != null) {
                layoutEmptyState.setVisibility(View.VISIBLE);
                rvTransactions.setVisibility(View.GONE);
            }
            return;
        }

        // Group transactions theo ngày
        List<TransactionGroupedItem> groupedItems = groupTransactionsByDate(transactionEntities);
        if (transactionAdapter != null) {
            transactionAdapter.updateList(groupedItems);
        }
        
        if (layoutEmptyState != null) {
            layoutEmptyState.setVisibility(View.GONE);
            rvTransactions.setVisibility(View.VISIBLE);
        }
    }
    
    private List<TransactionGroupedItem> groupTransactionsByDate(List<TransactionEntity> transactions) {
        List<TransactionGroupedItem> groupedItems = new ArrayList<>();
        
        // ✅ Sắp xếp transactions theo date giảm dần (mới nhất lên trên)
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
                
                // Giảm dần: date2.compareTo(date1)
                return date2.compareTo(date1);
            } catch (Exception e) {
                return 0;
            }
        });
        
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        SimpleDateFormat isoFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
        SimpleDateFormat displayDateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        SimpleDateFormat dayOfWeekFormat = new SimpleDateFormat("EEEE", new Locale("vi", "VN"));
        
        // ✅ Calendar để so sánh ngày
        Calendar today = Calendar.getInstance();
        today.set(Calendar.HOUR_OF_DAY, 0);
        today.set(Calendar.MINUTE, 0);
        today.set(Calendar.SECOND, 0);
        today.set(Calendar.MILLISECOND, 0);
        
        Calendar yesterday = Calendar.getInstance();
        yesterday.add(Calendar.DAY_OF_YEAR, -1);
        yesterday.set(Calendar.HOUR_OF_DAY, 0);
        yesterday.set(Calendar.MINUTE, 0);
        yesterday.set(Calendar.SECOND, 0);
        yesterday.set(Calendar.MILLISECOND, 0);
        
        java.util.Map<String, List<TransactionEntity>> transactionsByDate = new java.util.LinkedHashMap<>();
        
        for (TransactionEntity entity : sortedTransactions) {
            if (entity.date == null || entity.date.isEmpty()) continue;
            
            try {
                java.util.Date date;
                try {
                    date = isoFormat.parse(entity.date);
                } catch (Exception e) {
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
        
        // ✅ Tạo grouped items với header cho mỗi ngày (giữ thứ tự từ LinkedHashMap)
        for (java.util.Map.Entry<String, List<TransactionEntity>> entry : transactionsByDate.entrySet()) {
            String dateKey = entry.getKey();
            List<TransactionEntity> dayTransactions = entry.getValue();
            
            try {
                java.util.Date date = dateFormat.parse(dateKey);
                Calendar dateCal = Calendar.getInstance();
                dateCal.setTime(date);
                dateCal.set(Calendar.HOUR_OF_DAY, 0);
                dateCal.set(Calendar.MINUTE, 0);
                dateCal.set(Calendar.SECOND, 0);
                dateCal.set(Calendar.MILLISECOND, 0);
                
                // ✅ Xác định label: "Hôm nay", "Hôm qua", hoặc dd/MM/yyyy
                String dateHeader;
                if (isSameDay(dateCal, today)) {
                    dateHeader = getString(R.string.today);
                } else if (isSameDay(dateCal, yesterday)) {
                    dateHeader = getString(R.string.yesterday);
                } else {
                    dateHeader = displayDateFormat.format(date);
                }
                
                String dayOfWeek = formatDayOfWeek(dayOfWeekFormat.format(date));
                
                groupedItems.add(new TransactionGroupedItem(dateHeader, dayOfWeek));
                
                for (TransactionEntity entity : dayTransactions) {
                    TransactionUIModel uiModel = new TransactionUIModel(
                            entity.name,
                            entity.category,
                            entity.amount,
                            entity.wallet,
                            entity.date,
                            entity.type
                    );
                    groupedItems.add(new TransactionGroupedItem(uiModel));
                }
            } catch (Exception e) {
                // Ignore parse errors
            }
        }
        
        return groupedItems;
    }
    
    // ✅ Helper method để so sánh 2 ngày (chỉ so sánh năm/tháng/ngày)
    private boolean isSameDay(Calendar cal1, Calendar cal2) {
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
               cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR);
    }
    
    private String formatDayOfWeek(String dayOfWeek) {
        java.util.Map<String, String> dayMap = new java.util.HashMap<>();
        dayMap.put("Monday", "Thứ 2");
        dayMap.put("Tuesday", "Thứ 3");
        dayMap.put("Wednesday", "Thứ 4");
        dayMap.put("Thursday", "Thứ 5");
        dayMap.put("Friday", "Thứ 6");
        dayMap.put("Saturday", "Thứ 7");
        dayMap.put("Sunday", "Chủ nhật");
        
        return dayMap.getOrDefault(dayOfWeek, dayOfWeek);
    }
    
    private void updateBalance(List<WalletEntity> wallets) {
        if (wallets == null || wallets.isEmpty() || tvBalance == null) {
            return;
        }
        
        double totalBalance = 0;
        if (selectedWalletId != null) {
            for (WalletEntity w : wallets) {
                if (w.id.equals(selectedWalletId)) {
                    totalBalance = w.currentBalance;
                    break;
                }
            }
        } else {
            for (WalletEntity w : wallets) {
                totalBalance += w.currentBalance;
            }
        }
        tvBalance.setText(formatMoney((long) totalBalance) + " VND");
    }
    
    private void updateWalletData(List<TransactionEntity> transactions) {
        if (transactions == null || transactions.isEmpty()) {
            tvIncomeValue.setText("+0 VND");
            tvExpenseValue.setText("-0 VND");
            progIncome.setProgress(0);
            progExpense.setProgress(0);
            return;
        }
        
        double totalIncome = 0;
        double totalExpense = 0;
        
        for (TransactionEntity t : transactions) {
            if (t.type != null && t.type.equals("INCOME")) {
                totalIncome += t.amountDouble;
            } else if (t.type != null && t.type.equals("EXPENSE")) {
                totalExpense += t.amountDouble;
            }
        }
        
        tvIncomeValue.setText("+" + formatMoney((long) totalIncome) + " VND");
        tvExpenseValue.setText("-" + formatMoney((long) totalExpense) + " VND");
        
        double total = totalIncome + totalExpense;
        if (total > 0) {
            int perIncome = (int) ((totalIncome * 100) / total);
            int perExpense = (int) ((totalExpense * 100) / total);
            progIncome.setProgress(perIncome);
            progExpense.setProgress(perExpense);
        } else {
            progIncome.setProgress(0);
            progExpense.setProgress(0);
        }
    }

    // Format tiền VND đẹp
    private String formatMoney(long amount) {
        return String.format(Locale.getDefault(), "%,d", amount).replace(",", ".");
    }


    // ====================== XỬ LÝ BOTTOM NAVIGATION ======================
    private void setupBottomNavigation() {
        bottomNavigation.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            Intent intent = null;

            if (id == R.id.nav_home) {
                // Dùng HomeActivity trong cùng package (home package)
                intent = new Intent(WalletActivity.this, HomeActivity.class);
            } else if (id == R.id.nav_wallet) {
                return true; // Đang ở Wallet, không cần navigate
            } else if (id == R.id.nav_add) {
                intent = new Intent(WalletActivity.this, AddTransactionActivity.class);
            } else if (id == R.id.nav_statistic) {
                intent = new Intent(WalletActivity.this, StatisticActivity.class);
            } else if (id == R.id.nav_settings) {
                intent = new Intent(WalletActivity.this, SettingsActivity.class);
            }

            if (intent != null) {
                startActivity(intent);
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            }
            return true;
        });
    }
}
