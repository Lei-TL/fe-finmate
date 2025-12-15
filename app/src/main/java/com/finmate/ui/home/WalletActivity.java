package com.finmate.ui.home;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.finmate.R;
import com.finmate.data.local.database.entity.TransactionEntity;
import com.finmate.data.local.database.entity.WalletEntity;
import com.finmate.ui.activities.AddTransactionActivity;
import com.finmate.ui.activities.SettingsActivity;
import com.finmate.ui.activities.StatisticActivity;
import com.finmate.ui.transaction.TransactionAdapter;
import com.finmate.ui.transaction.TransactionGroupedItem;
import com.finmate.ui.transaction.TransactionUIModel;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.chip.Chip;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class WalletActivity extends AppCompatActivity {

    private WalletViewModel viewModel;
    private RecyclerView rvTransactions;
    private TransactionAdapter transactionAdapter;
    private View layoutEmptyState;
    private Chip chipWalletFilter;
    private String selectedWalletId = null;
    private String selectedWalletName = "";
    private TextView tvBalance, tvIncomeValue, tvExpenseValue, tvWalletName;
    private ProgressBar progIncome, progExpense;
    private ImageView btnBack;
    private BottomNavigationView bottomNavigation;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wallet);

        viewModel = new ViewModelProvider(this).get(WalletViewModel.class);

        mapViews();
        setupRecyclerView();
        setupClickListeners();
        observeViewModel();

        viewModel.loadWalletData();
    }

    private void mapViews() {
        rvTransactions = findViewById(R.id.rvTransactions);
        layoutEmptyState = findViewById(R.id.layoutEmptyState);
        chipWalletFilter = findViewById(R.id.chipWalletFilter);
        tvBalance = findViewById(R.id.tvBalance);
        tvIncomeValue = findViewById(R.id.tvIncomeValue);
        tvExpenseValue = findViewById(R.id.tvExpenseValue);
        progIncome = findViewById(R.id.progIncome);
        progExpense = findViewById(R.id.progExpense);
        tvWalletName = findViewById(R.id.tvWalletName);
        btnBack = findViewById(R.id.btnBack);
        bottomNavigation = findViewById(R.id.bottomNavigation);
        
        bottomNavigation.setSelectedItemId(R.id.nav_wallet);
    }

    private void setupRecyclerView() {
        rvTransactions.setLayoutManager(new LinearLayoutManager(this));
        transactionAdapter = new TransactionAdapter(new ArrayList<>());
        rvTransactions.setAdapter(transactionAdapter);
    }

    private void setupClickListeners() {
        if (chipWalletFilter != null) {
            chipWalletFilter.setOnClickListener(v -> showWalletSelectionMenu());
        }
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }
        setupBottomNavigation();
    }

    private void showWalletSelectionMenu() {
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
        
        // ✅ Sort dates giảm dần (mới nhất lên trên) trước khi tạo grouped items
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
        
        // ✅ Tạo grouped items với header cho mỗi ngày (đã sort)
        for (String dateKey : sortedDateKeys) {
            List<TransactionEntity> dayTransactions = transactionsByDate.get(dateKey);
            
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
                            entity.id,
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
        dayMap.put("Monday", getString(R.string.monday));
        dayMap.put("Tuesday", getString(R.string.tuesday));
        dayMap.put("Wednesday", getString(R.string.wednesday));
        dayMap.put("Thursday", getString(R.string.thursday));
        dayMap.put("Friday", getString(R.string.friday));
        dayMap.put("Saturday", getString(R.string.saturday));
        dayMap.put("Sunday", getString(R.string.sunday));
        
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
        if (bottomNavigation == null) {
            return;
        }
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
