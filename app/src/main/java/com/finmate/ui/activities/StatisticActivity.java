package com.finmate.ui.activities;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.finmate.R;
import com.finmate.core.ui.LocaleHelper;
import com.finmate.data.local.database.entity.TransactionEntity;
import com.finmate.data.local.database.entity.WalletEntity;
import com.finmate.data.repository.TransactionRepository;
import com.finmate.data.repository.WalletRepository;
import com.finmate.ui.home.WalletActivity;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class StatisticActivity extends AppCompatActivity {

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleHelper.applyLocale(newBase));
    }

    @Inject
    TransactionRepository transactionRepository;
    
    @Inject
    WalletRepository walletRepository;

    ImageView btnBack;
    TextView tvExpenseTab, tvIncomeTab, tvTotalExpense;
    
    // Filter views
    com.google.android.material.chip.Chip chipWalletFilter;
    com.google.android.material.button.MaterialButton btnTimeFilter;

    BarChart barChartExpense;
    PieChart pieChartExpense;

    BottomNavigationView bottomNavigation;
    
    // Filter state
    private String selectedWalletName = null; // null = all wallets
    private Long startDateFilter = null;
    private Long endDateFilter = null;
    private List<WalletEntity> wallets = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_statistic);

        // ÁNH XẠ
        btnBack = findViewById(R.id.btnBack);
        tvExpenseTab = findViewById(R.id.tvExpenseTab);
        tvIncomeTab = findViewById(R.id.tvIncomeTab);
        tvTotalExpense = findViewById(R.id.tvTotalExpense);
        
        // Filter views
        chipWalletFilter = findViewById(R.id.chipWalletFilter);
        btnTimeFilter = findViewById(R.id.btnTimeFilter);

        barChartExpense = findViewById(R.id.barChartExpense);
        pieChartExpense = findViewById(R.id.pieChartExpense);

        bottomNavigation = findViewById(R.id.bottomNavigation);
        
        setupFilters();

        // BACK
        btnBack.setOnClickListener(v -> finish());

        // TAB CHI TIÊU (hiện tại)
        tvExpenseTab.setOnClickListener(v -> {});

        // CHUYỂN QUA THU NHẬP
        tvIncomeTab.setOnClickListener(v -> {
            startActivity(new Intent(this, IncomeStatisticActivity.class));
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        });

        // HIGHLIGHT TAB
        highlightTabs();

        // BOTTOM NAV
        setupBottomNav();
        
        // ✅ Load wallets và transactions
        loadWallets();
        loadTransactions();
    }
    
    private void setupFilters() {
        // Wallet filter
        if (chipWalletFilter != null) {
            chipWalletFilter.setOnClickListener(v -> {
                showWalletSelectionDialog();
            });
        }
        
        // Time filter
        if (btnTimeFilter != null) {
            btnTimeFilter.setOnClickListener(v -> {
                com.finmate.ui.dialogs.TimeFilterBottomSheet bottomSheet = 
                    com.finmate.ui.dialogs.TimeFilterBottomSheet.newInstance();
                bottomSheet.setListener(new com.finmate.ui.dialogs.TimeFilterBottomSheet.TimeFilterListener() {
                    @Override
                    public void onTodaySelected() {
                        Calendar today = Calendar.getInstance();
                        today.set(Calendar.HOUR_OF_DAY, 0);
                        today.set(Calendar.MINUTE, 0);
                        today.set(Calendar.SECOND, 0);
                        today.set(Calendar.MILLISECOND, 0);
                        startDateFilter = today.getTimeInMillis();
                        endDateFilter = today.getTimeInMillis() + 86400000 - 1; // End of day
                        btnTimeFilter.setText(getString(R.string.today));
                        loadTransactions();
                    }

                    @Override
                    public void onSingleDaySelected(java.util.Date date) {
                        Calendar cal = Calendar.getInstance();
                        cal.setTime(date);
                        cal.set(Calendar.HOUR_OF_DAY, 0);
                        cal.set(Calendar.MINUTE, 0);
                        cal.set(Calendar.SECOND, 0);
                        cal.set(Calendar.MILLISECOND, 0);
                        startDateFilter = cal.getTimeInMillis();
                        endDateFilter = cal.getTimeInMillis() + 86400000 - 1;
                        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault());
                        btnTimeFilter.setText(sdf.format(date));
                        loadTransactions();
                    }

                    @Override
                    public void onDateRangeSelected(java.util.Date startDate, java.util.Date endDate) {
                        Calendar startCal = Calendar.getInstance();
                        startCal.setTime(startDate);
                        startCal.set(Calendar.HOUR_OF_DAY, 0);
                        startCal.set(Calendar.MINUTE, 0);
                        startCal.set(Calendar.SECOND, 0);
                        startCal.set(Calendar.MILLISECOND, 0);
                        startDateFilter = startCal.getTimeInMillis();
                        
                        Calendar endCal = Calendar.getInstance();
                        endCal.setTime(endDate);
                        endCal.set(Calendar.HOUR_OF_DAY, 23);
                        endCal.set(Calendar.MINUTE, 59);
                        endCal.set(Calendar.SECOND, 59);
                        endCal.set(Calendar.MILLISECOND, 999);
                        endDateFilter = endCal.getTimeInMillis();
                        
                        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault());
                        btnTimeFilter.setText(sdf.format(startDate) + " - " + sdf.format(endDate));
                        loadTransactions();
                    }

                    @Override
                    public void onClear() {
                        startDateFilter = null;
                        endDateFilter = null;
                        btnTimeFilter.setText(getString(R.string.this_month));
                        loadTransactions();
                    }
                });
                bottomSheet.show(getSupportFragmentManager(), "TimeFilterBottomSheet");
            });
        }
    }
    
    private void showWalletSelectionDialog() {
        if (wallets.isEmpty()) {
            Toast.makeText(this, "Chưa có ví nào", Toast.LENGTH_SHORT).show();
            return;
        }
        
        String[] walletNames = new String[wallets.size() + 1];
        walletNames[0] = "Tất cả ví";
        for (int i = 0; i < wallets.size(); i++) {
            walletNames[i + 1] = wallets.get(i).name;
        }
        
        new androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Chọn ví")
            .setItems(walletNames, (dialog, which) -> {
                if (which == 0) {
                    selectedWalletName = null;
                    chipWalletFilter.setText("Tất cả ví");
                } else {
                    selectedWalletName = wallets.get(which - 1).name;
                    chipWalletFilter.setText(selectedWalletName);
                }
                loadTransactions();
            })
            .show();
    }
    
    private void loadWallets() {
        walletRepository.getAll(new WalletRepository.Callback() {
            @Override
            public void onResult(List<WalletEntity> walletList) {
                wallets = walletList != null ? walletList : new ArrayList<>();
            }
        });
    }
    
    private void loadTransactions() {
        if (selectedWalletName != null) {
            transactionRepository.getByWalletNameAndDateRange(
                selectedWalletName, startDateFilter, endDateFilter,
                new TransactionRepository.OnResultCallback<List<TransactionEntity>>() {
                    @Override
                    public void onResult(List<TransactionEntity> transactions) {
                        processTransactions(transactions);
                    }
                }
            );
        } else {
            transactionRepository.getByDateRange(
                startDateFilter, endDateFilter,
                new TransactionRepository.OnResultCallback<List<TransactionEntity>>() {
                    @Override
                    public void onResult(List<TransactionEntity> transactions) {
                        processTransactions(transactions);
                    }
                }
            );
        }
    }
    
    private void processTransactions(List<TransactionEntity> transactions) {
        if (transactions == null) {
            transactions = new ArrayList<>();
        }
        
        // Filter chỉ EXPENSE
        List<TransactionEntity> expenseTransactions = new ArrayList<>();
        double totalExpense = 0;
        for (TransactionEntity t : transactions) {
            if (t.type != null && t.type.equals("EXPENSE")) {
                expenseTransactions.add(t);
                totalExpense += t.amountDouble;
            }
        }
        
        // Update total expense
        if (tvTotalExpense != null) {
            tvTotalExpense.setText(formatAmount(totalExpense));
        }
        
        // Load charts
        loadBarChartData(expenseTransactions);
        loadPieChartData(expenseTransactions);
    }
    
    private String formatAmount(double amount) {
        if (amount >= 1_000_000_000) {
            return String.format(Locale.getDefault(), "%,.1fB", amount / 1_000_000_000);
        } else if (amount >= 1_000_000) {
            return String.format(Locale.getDefault(), "%,.1fM", amount / 1_000_000);
        } else if (amount >= 1_000) {
            return String.format(Locale.getDefault(), "%,.1fK", amount / 1_000);
        } else {
            return String.format(Locale.getDefault(), "%,.0f", amount);
        }
    }

    // ===================== BAR CHART =====================
    private void loadBarChartData(List<TransactionEntity> transactions) {
        if (transactions == null || transactions.isEmpty()) {
            barChartExpense.clear();
            barChartExpense.setNoDataText("Không có dữ liệu");
            barChartExpense.invalidate();
            return;
        }

        // Group by month
        Map<String, Double> monthlyData = new HashMap<>();
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat monthFormat = new SimpleDateFormat("yyyy-MM", Locale.getDefault());
        
        for (TransactionEntity t : transactions) {
            if (t.date != null && !t.date.isEmpty()) {
                try {
                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                    cal.setTime(dateFormat.parse(t.date));
                    String monthKey = monthFormat.format(cal.getTime());
                    monthlyData.put(monthKey, monthlyData.getOrDefault(monthKey, 0.0) + t.amountDouble);
                } catch (Exception e) {
                    // Skip invalid dates
                }
            }
        }
        
        if (monthlyData.isEmpty()) {
            barChartExpense.clear();
            barChartExpense.setNoDataText("Không có dữ liệu");
            barChartExpense.invalidate();
            return;
        }
        
        // Sort by month and create entries
        List<String> sortedMonths = new ArrayList<>(monthlyData.keySet());
        java.util.Collections.sort(sortedMonths);
        
        List<BarEntry> entries = new ArrayList<>();
        List<String> labels = new ArrayList<>();
        
        for (int i = 0; i < sortedMonths.size(); i++) {
            String month = sortedMonths.get(i);
            entries.add(new BarEntry(i, monthlyData.get(month).floatValue()));
            
            // Format label: "MM/yyyy"
            try {
                SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM", Locale.getDefault());
                SimpleDateFormat outputFormat = new SimpleDateFormat("MM/yyyy", Locale.getDefault());
                labels.add(outputFormat.format(inputFormat.parse(month)));
            } catch (Exception e) {
                labels.add(month);
            }
        }

        BarDataSet dataSet = new BarDataSet(entries, "Chi tiêu");
        dataSet.setColor(Color.parseColor("#FF5252"));
        dataSet.setValueTextColor(Color.WHITE);
        dataSet.setValueTextSize(10f);

        BarData barData = new BarData(dataSet);
        barData.setBarWidth(0.4f);

        barChartExpense.setData(barData);
        barChartExpense.setFitBars(true);
        barChartExpense.setNoDataText("Không có dữ liệu");
        barChartExpense.getDescription().setEnabled(false);
        barChartExpense.getLegend().setEnabled(false);

        XAxis xAxis = barChartExpense.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setTextColor(Color.WHITE);
        xAxis.setDrawGridLines(false);
        xAxis.setValueFormatter(new IndexAxisValueFormatter(labels));
        xAxis.setLabelCount(labels.size(), true);

        barChartExpense.animateY(800);
        barChartExpense.invalidate();
    }

    // ===================== PIE CHART =====================
    private void loadPieChartData(List<TransactionEntity> transactions) {
        if (transactions == null || transactions.isEmpty()) {
            pieChartExpense.clear();
            pieChartExpense.setNoDataText("Không có dữ liệu");
            pieChartExpense.invalidate();
            return;
        }

        // Group by category
        Map<String, Double> categoryData = new HashMap<>();
        for (TransactionEntity t : transactions) {
            String category = (t.category != null && !t.category.isEmpty()) ? t.category : "Khác";
            categoryData.put(category, categoryData.getOrDefault(category, 0.0) + t.amountDouble);
        }
        
        if (categoryData.isEmpty()) {
            pieChartExpense.clear();
            pieChartExpense.setNoDataText("Không có dữ liệu");
            pieChartExpense.invalidate();
            return;
        }
        
        // Sort by amount (descending) and create entries
        List<Map.Entry<String, Double>> sortedCategories = new ArrayList<>(categoryData.entrySet());
        sortedCategories.sort((a, b) -> Double.compare(b.getValue(), a.getValue()));
        
        List<PieEntry> entries = new ArrayList<>();
        int[] colors = {
            Color.parseColor("#FF5252"),
            Color.parseColor("#FF8A65"),
            Color.parseColor("#FF7043"),
            Color.parseColor("#FFB74D"),
            Color.parseColor("#FFA726"),
            Color.parseColor("#FF9800"),
            Color.parseColor("#FB8C00"),
            Color.parseColor("#F57C00")
        };
        
        for (int i = 0; i < sortedCategories.size() && i < 8; i++) {
            Map.Entry<String, Double> entry = sortedCategories.get(i);
            entries.add(new PieEntry(entry.getValue().floatValue(), entry.getKey()));
        }

        PieDataSet dataSet = new PieDataSet(entries, "");
        dataSet.setColors(colors);
        dataSet.setValueTextSize(12f);
        dataSet.setValueTextColor(Color.WHITE);

        PieData pieData = new PieData(dataSet);

        pieChartExpense.setData(pieData);
        pieChartExpense.setUsePercentValues(true);

        pieChartExpense.setTransparentCircleRadius(50f);
        pieChartExpense.setHoleRadius(45f);

        pieChartExpense.getDescription().setEnabled(false);

        Legend legend = pieChartExpense.getLegend();
        legend.setTextColor(Color.WHITE);
        legend.setTextSize(12f);

        pieChartExpense.animateY(800);
        pieChartExpense.invalidate();
    }

    // ===================== HIGHLIGHT TAB =====================
    private void highlightTabs() {
        // Chi tiêu ACTIVE
        tvExpenseTab.setTextColor(Color.WHITE);
        tvExpenseTab.setTextSize(18f);
        tvExpenseTab.setTypeface(tvExpenseTab.getTypeface(), android.graphics.Typeface.BOLD);

        // Thu nhập INACTIVE
        tvIncomeTab.setTextColor(Color.parseColor("#777777"));
        tvIncomeTab.setTypeface(tvIncomeTab.getTypeface(), android.graphics.Typeface.NORMAL);
    }

    // ===================== BOTTOM NAV =====================
    private void setupBottomNav() {
        bottomNavigation.setSelectedItemId(R.id.nav_statistic);

        bottomNavigation.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            Intent intent = null;

            if (id == R.id.nav_home) {
                intent = new Intent(this, com.finmate.ui.home.HomeActivity.class);
            } else if (id == R.id.nav_wallet) {
                intent = new Intent(this, com.finmate.ui.home.WalletActivity.class);
            } else if (id == R.id.nav_add) {
                intent = new Intent(this, AddTransactionActivity.class);
            } else if (id == R.id.nav_statistic) {
                return true; // Đang ở Statistic, không cần navigate
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
}
