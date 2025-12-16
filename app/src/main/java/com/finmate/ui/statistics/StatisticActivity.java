package com.finmate.ui.statistics;

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
import com.finmate.ui.activities.AddTransactionActivity;
import com.finmate.ui.settings.SettingsActivity;
import com.finmate.ui.wallet.WalletActivity;
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
        
        // ✅ Đọc time filter từ TimeFilterManager khi khởi tạo
        loadTimeFilterFromManager();
        
        // ✅ Load wallets và transactions
        loadWallets();
        loadTransactions();
    }
    
    // ✅ Đọc time filter từ TimeFilterManager và áp dụng
    private void loadTimeFilterFromManager() {
        com.finmate.core.ui.TimeFilterManager.TimeFilterState state = 
            com.finmate.core.ui.TimeFilterManager.getTimeFilter(this);
        
        if (state.startDate != null && state.endDate != null) {
            startDateFilter = state.startDate;
            endDateFilter = state.endDate;
            
            // Set text cho button
            if (state.filterText != null) {
                btnTimeFilter.setText(state.filterText);
            }
        }
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
                        String filterText = getString(R.string.today);
                        btnTimeFilter.setText(filterText);
                        
                        // ✅ Lưu vào TimeFilterManager để share với HomeActivity
                        com.finmate.core.ui.TimeFilterManager.saveTimeFilter(StatisticActivity.this, startDateFilter, endDateFilter, filterText);
                        
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
                        String filterText = sdf.format(date);
                        btnTimeFilter.setText(filterText);
                        
                        // ✅ Lưu vào TimeFilterManager để share với HomeActivity
                        com.finmate.core.ui.TimeFilterManager.saveTimeFilter(StatisticActivity.this, startDateFilter, endDateFilter, filterText);
                        
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
                        String filterText = sdf.format(startDate) + " - " + sdf.format(endDate);
                        btnTimeFilter.setText(filterText);
                        
                        // ✅ Lưu vào TimeFilterManager để share với HomeActivity
                        com.finmate.core.ui.TimeFilterManager.saveTimeFilter(StatisticActivity.this, startDateFilter, endDateFilter, filterText);
                        
                        loadTransactions();
                    }

                    @Override
                    public void onClear() {
                        startDateFilter = null;
                        endDateFilter = null;
                        String filterText = getString(R.string.this_month);
                        btnTimeFilter.setText(filterText);
                        
                        // ✅ Clear trong TimeFilterManager
                        com.finmate.core.ui.TimeFilterManager.clearTimeFilter(StatisticActivity.this);
                        
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
                        // ✅ processTransactions phải chạy trên main thread vì nó update UI (charts)
                        runOnUiThread(() -> {
                            processTransactions(transactions);
                        });
                    }
                }
            );
        } else {
            transactionRepository.getByDateRange(
                startDateFilter, endDateFilter,
                new TransactionRepository.OnResultCallback<List<TransactionEntity>>() {
                    @Override
                    public void onResult(List<TransactionEntity> transactions) {
                        // ✅ processTransactions phải chạy trên main thread vì nó update UI (charts)
                        runOnUiThread(() -> {
                            processTransactions(transactions);
                        });
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
            tvTotalExpense.setText("-" + formatAmount(totalExpense) + " VND");
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

    // ===================== BAR CHART - STACKED BY DAY AND CATEGORY =====================
    private void loadBarChartData(List<TransactionEntity> transactions) {
        if (transactions == null || transactions.isEmpty()) {
            barChartExpense.clear();
            barChartExpense.setNoDataText("Không có dữ liệu");
            barChartExpense.invalidate();
            return;
        }

        // ✅ Group by day and category: Map<dayKey, Map<category, amount>>
        Map<String, Map<String, Double>> dailyCategoryData = new HashMap<>();
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat dayFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        
        // Collect all unique categories
        java.util.Set<String> allCategories = new java.util.HashSet<>();
        
        for (TransactionEntity t : transactions) {
            if (t.date != null && !t.date.isEmpty()) {
                try {
                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                    cal.setTime(dateFormat.parse(t.date));
                    String dayKey = dayFormat.format(cal.getTime());
                    
                    String category = (t.category != null && !t.category.isEmpty()) ? t.category : "Khác";
                    allCategories.add(category);
                    
                    // Initialize day map if not exists
                    if (!dailyCategoryData.containsKey(dayKey)) {
                        dailyCategoryData.put(dayKey, new HashMap<>());
                    }
                    
                    // Add amount to category for this day
                    Map<String, Double> categoryMap = dailyCategoryData.get(dayKey);
                    categoryMap.put(category, categoryMap.getOrDefault(category, 0.0) + t.amountDouble);
                } catch (Exception e) {
                    // Skip invalid dates
                }
            }
        }
        
        if (dailyCategoryData.isEmpty()) {
            barChartExpense.clear();
            barChartExpense.setNoDataText("Không có dữ liệu");
            barChartExpense.invalidate();
            return;
        }
        
        // ✅ Sort days
        List<String> sortedDays = new ArrayList<>(dailyCategoryData.keySet());
        java.util.Collections.sort(sortedDays);
        
        // ✅ Limit to last 30 days if too many
        int maxDays = 30;
        if (sortedDays.size() > maxDays) {
            sortedDays = sortedDays.subList(sortedDays.size() - maxDays, sortedDays.size());
        }
        
        // ✅ Convert categories to sorted list (by total amount across all days)
        Map<String, Double> categoryTotals = new HashMap<>();
        for (Map<String, Double> categoryMap : dailyCategoryData.values()) {
            for (Map.Entry<String, Double> entry : categoryMap.entrySet()) {
                categoryTotals.put(entry.getKey(), categoryTotals.getOrDefault(entry.getKey(), 0.0) + entry.getValue());
            }
        }
        List<Map.Entry<String, Double>> sortedCategories = new ArrayList<>(categoryTotals.entrySet());
        sortedCategories.sort((a, b) -> Double.compare(b.getValue(), a.getValue()));
        
        // ✅ Limit to top 8 categories
        int maxCategories = Math.min(8, sortedCategories.size());
        List<String> categoryOrder = new ArrayList<>();
        for (int i = 0; i < maxCategories; i++) {
            categoryOrder.add(sortedCategories.get(i).getKey());
        }
        
        // ✅ Create stacked bar entries
        List<BarEntry> entries = new ArrayList<>();
        List<String> labels = new ArrayList<>();
        
        for (int i = 0; i < sortedDays.size(); i++) {
            String dayKey = sortedDays.get(i);
            Map<String, Double> categoryMap = dailyCategoryData.get(dayKey);
            
            // Create stacked values array for this day
            float[] stackedValues = new float[maxCategories];
            for (int j = 0; j < maxCategories; j++) {
                String category = categoryOrder.get(j);
                stackedValues[j] = categoryMap.getOrDefault(category, 0.0).floatValue();
            }
            
            entries.add(new BarEntry(i, stackedValues));
            
            // Format label: "dd/MM"
            try {
                SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                SimpleDateFormat outputFormat = new SimpleDateFormat("dd/MM", Locale.getDefault());
                labels.add(outputFormat.format(inputFormat.parse(dayKey)));
            } catch (Exception e) {
                labels.add(dayKey.substring(5)); // "MM-dd"
            }
        }

        // ✅ Lấy màu text từ theme
        int textColor = getResources().getColor(android.R.color.white, getTheme());
        try {
            android.content.res.TypedArray a = obtainStyledAttributes(new int[]{com.finmate.R.attr.textColorPrimary});
            textColor = a.getColor(0, Color.WHITE);
            a.recycle();
        } catch (Exception e) {
            // Fallback to white
        }
        
        // ✅ Category colors (same as pie chart)
        int[] categoryColors = {
            Color.parseColor("#FF5252"),
            Color.parseColor("#FF8A65"),
            Color.parseColor("#FF7043"),
            Color.parseColor("#FFB74D"),
            Color.parseColor("#FFA726"),
            Color.parseColor("#FF9800"),
            Color.parseColor("#FB8C00"),
            Color.parseColor("#F57C00")
        };
        
        BarDataSet dataSet = new BarDataSet(entries, "Chi tiêu");
        dataSet.setColors(categoryColors);
        dataSet.setValueTextColor(textColor);
        dataSet.setValueTextSize(8f);
        dataSet.setStackLabels(categoryOrder.toArray(new String[0])); // Set category labels for legend
        // ✅ Custom formatter để không hiển thị số 0 trên các cột
        dataSet.setValueFormatter(new com.github.mikephil.charting.formatter.ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                if (value == 0f) {
                    return ""; // Không hiển thị số 0
                }
                // Format số lớn với K/M/B
                if (value >= 1_000_000_000) {
                    return String.format(Locale.getDefault(), "%.1fB", value / 1_000_000_000);
                } else if (value >= 1_000_000) {
                    return String.format(Locale.getDefault(), "%.1fM", value / 1_000_000);
                } else if (value >= 1_000) {
                    return String.format(Locale.getDefault(), "%.1fK", value / 1_000);
                } else {
                    return String.format(Locale.getDefault(), "%.0f", value);
                }
            }
        });

        BarData barData = new BarData(dataSet);
        barData.setBarWidth(0.6f);

        barChartExpense.setData(barData);
        barChartExpense.setFitBars(true);
        barChartExpense.setNoDataText("Không có dữ liệu");
        barChartExpense.getDescription().setEnabled(false);
        
        // ✅ Enable legend to show categories
        Legend legend = barChartExpense.getLegend();
        legend.setEnabled(true);
        legend.setTextColor(textColor);
        legend.setTextSize(10f);
        legend.setVerticalAlignment(Legend.LegendVerticalAlignment.BOTTOM);
        legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.CENTER);
        legend.setOrientation(Legend.LegendOrientation.HORIZONTAL);
        legend.setDrawInside(false);

        XAxis xAxis = barChartExpense.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setTextColor(textColor);
        xAxis.setDrawGridLines(false);
        xAxis.setGranularity(1f); // Đảm bảo mỗi cột đều có label
        
        // ✅ Custom formatter để hiển thị tất cả labels nhưng xoay thẳng đứng
        xAxis.setValueFormatter(new IndexAxisValueFormatter(labels) {
            @Override
            public String getFormattedValue(float value) {
                int index = (int) value;
                if (index >= 0 && index < labels.size()) {
                    return labels.get(index);
                }
                return "";
            }
        });
        
        // ✅ Hiển thị labels ngang, điều chỉnh số lượng để không bị đè
        if (labels.size() > 10) {
            // Nếu nhiều ngày, chỉ hiển thị một số labels để tránh chồng khi ngang
            int step = labels.size() > 20 ? 3 : (labels.size() > 15 ? 2 : 1);
            xAxis.setLabelCount((labels.size() / step) + 1, false);
            // Custom formatter để chỉ hiển thị labels ở các vị trí step
            xAxis.setValueFormatter(new com.github.mikephil.charting.formatter.ValueFormatter() {
                @Override
                public String getFormattedValue(float value) {
                    int index = (int) value;
                    if (index >= 0 && index < labels.size() && index % step == 0) {
                        return labels.get(index);
                    }
                    return ""; // Không hiển thị labels ở các vị trí khác
                }
            });
        } else {
            // Nếu ít ngày, hiển thị tất cả
            xAxis.setLabelCount(labels.size(), false);
        }
        
        xAxis.setLabelRotationAngle(0f); // Ngang (không xoay)
        xAxis.setTextSize(9f); // Giảm kích thước text để tránh chồng
        xAxis.setYOffset(2f); // Điều chỉnh vị trí
        xAxis.setAvoidFirstLastClipping(true); // Tránh cắt labels ở đầu và cuối

        // ✅ Cấu hình YAxis: Tắt bên phải, ẩn số 0 bên trái
        com.github.mikephil.charting.components.YAxis yAxisLeft = barChartExpense.getAxisLeft();
        yAxisLeft.setEnabled(true);
        yAxisLeft.setTextColor(textColor);
        yAxisLeft.setDrawGridLines(true);
        yAxisLeft.setDrawZeroLine(false); // Không vẽ đường zero line
        // Custom formatter để không hiển thị số 0
        yAxisLeft.setValueFormatter(new com.github.mikephil.charting.formatter.ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                if (value == 0f) {
                    return ""; // Không hiển thị số 0
                }
                // Format số lớn với K/M/B
                if (value >= 1_000_000_000) {
                    return String.format(Locale.getDefault(), "%.1fB", value / 1_000_000_000);
                } else if (value >= 1_000_000) {
                    return String.format(Locale.getDefault(), "%.1fM", value / 1_000_000);
                } else if (value >= 1_000) {
                    return String.format(Locale.getDefault(), "%.1fK", value / 1_000);
                } else {
                    return String.format(Locale.getDefault(), "%.0f", value);
                }
            }
        });
        
        com.github.mikephil.charting.components.YAxis yAxisRight = barChartExpense.getAxisRight();
        yAxisRight.setEnabled(false); // Tắt YAxis bên phải

        // ✅ Chart animation phải chạy trên main thread
        runOnUiThread(() -> {
            barChartExpense.animateY(800);
            barChartExpense.invalidate();
        });
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

        // ✅ Lấy màu text từ theme
        int textColor = getResources().getColor(android.R.color.white, getTheme());
        try {
            android.content.res.TypedArray a = obtainStyledAttributes(new int[]{com.finmate.R.attr.textColorPrimary});
            textColor = a.getColor(0, Color.WHITE);
            a.recycle();
        } catch (Exception e) {
            // Fallback to white
        }
        
        PieDataSet dataSet = new PieDataSet(entries, "");
        dataSet.setColors(colors);
        dataSet.setValueTextSize(12f);
        dataSet.setValueTextColor(textColor);

        PieData pieData = new PieData(dataSet);

        pieChartExpense.setData(pieData);
        pieChartExpense.setUsePercentValues(true);

        pieChartExpense.setTransparentCircleRadius(50f);
        pieChartExpense.setHoleRadius(45f);

        pieChartExpense.getDescription().setEnabled(false);

        Legend legend = pieChartExpense.getLegend();
        legend.setTextColor(textColor);
        legend.setTextSize(12f);

        pieChartExpense.animateY(800);
        pieChartExpense.invalidate();
    }

    // ===================== HIGHLIGHT TAB =====================
    private void highlightTabs() {
        // Chi tiêu ACTIVE
        int textColorPrimary = getResources().getColor(android.R.color.white, getTheme());
        try {
            android.content.res.TypedArray a = obtainStyledAttributes(new int[]{com.finmate.R.attr.textColorPrimary});
            textColorPrimary = a.getColor(0, Color.WHITE);
            a.recycle();
        } catch (Exception e) {
            // Fallback to white
        }
        tvExpenseTab.setTextColor(textColorPrimary);
        tvExpenseTab.setTextSize(18f);
        tvExpenseTab.setTypeface(tvExpenseTab.getTypeface(), android.graphics.Typeface.BOLD);

        // Thu nhập INACTIVE
        int textColorSecondary = getResources().getColor(android.R.color.darker_gray, getTheme());
        try {
            android.content.res.TypedArray a = obtainStyledAttributes(new int[]{com.finmate.R.attr.textColorSecondary});
            textColorSecondary = a.getColor(0, Color.parseColor("#777777"));
            a.recycle();
        } catch (Exception e) {
            // Fallback to gray
        }
        tvIncomeTab.setTextColor(textColorSecondary);
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
                intent = new Intent(this, WalletActivity.class);
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
