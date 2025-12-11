package com.finmate.ui.transaction;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.finmate.R;
import com.finmate.core.util.TransactionFormatter;
import com.finmate.data.local.database.entity.WalletEntity;
import com.finmate.data.repository.model.StatisticFilter;
import com.finmate.ui.activities.AddTransactionActivity;
import com.finmate.ui.activities.IncomeStatisticActivity;
import com.finmate.ui.home.HomeActivity;
import com.finmate.ui.home.WalletActivity;
import com.finmate.ui.settings.SettingsActivity;
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

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class StatisticActivity extends AppCompatActivity {

    private static final String TYPE_EXPENSE = "EXPENSE";

    ImageView btnBack;
    TextView tvExpenseTab, tvIncomeTab, tvTotalExpense, tvDateFilter, tvWalletFilter;
    BarChart barChartExpense;
    PieChart pieChartExpense;
    BottomNavigationView bottomNavigation;

    StatisticViewModel viewModel;
    List<WalletEntity> walletOptions = new ArrayList<>();
    String currentWalletLabel = "Tất cả ví";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_statistic);

        viewModel = new ViewModelProvider(this).get(StatisticViewModel.class);

        btnBack = findViewById(R.id.btnBack);
        tvExpenseTab = findViewById(R.id.tvExpenseTab);
        tvIncomeTab = findViewById(R.id.tvIncomeTab);
        tvTotalExpense = findViewById(R.id.tvTotalExpense);
        tvDateFilter = findViewById(R.id.tvDateFilter);
        tvWalletFilter = findViewById(R.id.tvWalletFilter);

        barChartExpense = findViewById(R.id.barChartExpense);
        pieChartExpense = findViewById(R.id.pieChartExpense);

        bottomNavigation = findViewById(R.id.bottomNavigation);

        highlightTabs();
        setupBottomNav();
        setupEvents();
        setupObservers();

        updateFilterLabels(viewModel.getCurrentFilter());
        viewModel.loadWallets();
        viewModel.loadStatistics(TYPE_EXPENSE);
    }

    private void setupEvents() {
        btnBack.setOnClickListener(v -> finish());

        tvExpenseTab.setOnClickListener(v -> { /* already on expense */ });

        tvIncomeTab.setOnClickListener(v -> {
            startActivity(new Intent(this, IncomeStatisticActivity.class));
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        });

        tvDateFilter.setOnClickListener(v -> showDateFilterDialog());
        tvWalletFilter.setOnClickListener(v -> showWalletFilterDialog());
    }

    private void setupObservers() {
        viewModel.statistic.observe(this, this::renderStatistic);
        viewModel.wallets.observe(this, wallets -> {
            walletOptions = wallets != null ? wallets : new ArrayList<>();
        });
    }

    private void renderStatistic(StatisticResult result) {
        String formatted = TransactionFormatter.formatAmount(result.getTotalAmount());
        tvTotalExpense.setText("-" + formatted + " VND");

        renderBarChart(result.getTimeline());
        renderPieChart(result.getByCategory());
    }

    private void renderBarChart(List<ChartPoint> data) {
        barChartExpense.clear();

        if (data == null || data.isEmpty()) {
            barChartExpense.setNoDataText("Không có dữ liệu");
            barChartExpense.invalidate();
            return;
        }

        List<BarEntry> entries = new ArrayList<>();
        List<String> labels = new ArrayList<>();
        for (int i = 0; i < data.size(); i++) {
            entries.add(new BarEntry(i, data.get(i).getValue()));
            labels.add(data.get(i).getLabel());
        }

        BarDataSet dataSet = new BarDataSet(entries, "Chi tiêu");
        dataSet.setColor(Color.parseColor("#FF5252"));
        dataSet.setValueTextColor(Color.WHITE);
        dataSet.setValueTextSize(10f);

        BarData barData = new BarData(dataSet);
        barData.setBarWidth(0.4f);

        barChartExpense.setData(barData);
        barChartExpense.setFitBars(true);
        barChartExpense.getDescription().setEnabled(false);
        barChartExpense.getLegend().setEnabled(false);

        XAxis xAxis = barChartExpense.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setTextColor(Color.WHITE);
        xAxis.setDrawGridLines(false);
        xAxis.setGranularity(1f);
        xAxis.setValueFormatter(new IndexAxisValueFormatter(labels));
        xAxis.setLabelCount(labels.size());

        barChartExpense.animateY(500);
        barChartExpense.invalidate();
    }

    private void renderPieChart(List<ChartPoint> data) {
        pieChartExpense.clear();

        if (data == null || data.isEmpty()) {
            pieChartExpense.setNoDataText("Không có dữ liệu");
            pieChartExpense.invalidate();
            return;
        }

        List<PieEntry> entries = new ArrayList<>();
        for (ChartPoint point : data) {
            entries.add(new PieEntry(point.getValue(), point.getLabel()));
        }

        PieDataSet dataSet = new PieDataSet(entries, "");
        dataSet.setColors(
                Color.parseColor("#FF5252"),
                Color.parseColor("#FF8A65"),
                Color.parseColor("#FF7043"),
                Color.parseColor("#FFB74D"),
                Color.parseColor("#FFCC80")
        );

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

        pieChartExpense.animateY(500);
        pieChartExpense.invalidate();
    }

    private void showDateFilterDialog() {
        String[] options = new String[]{
                "7 ngày gần nhất",
                "30 ngày gần nhất",
                "Tháng này",
                "Năm nay",
                "Chọn ngày tùy chỉnh"
        };

        new AlertDialog.Builder(this)
                .setTitle("Khoảng thời gian")
                .setItems(options, (dialog, which) -> {
                    switch (which) {
                        case 0:
                            applyFilter(StatisticFilter.lastDays(7));
                            break;
                        case 1:
                            applyFilter(StatisticFilter.lastDays(30));
                            break;
                        case 2:
                            applyFilter(StatisticFilter.currentMonth());
                            break;
                        case 3:
                            applyFilter(StatisticFilter.yearToDate());
                            break;
                        case 4:
                            showCustomRangePicker();
                            break;
                    }
                })
                .show();
    }

    private void showCustomRangePicker() {
        Calendar now = Calendar.getInstance();
        DatePickerDialog startPicker = new DatePickerDialog(
                this,
                (view, y, m, d) -> {
                    Calendar start = Calendar.getInstance();
                    start.set(y, m, d);
                    showEndDatePicker(start);
                },
                now.get(Calendar.YEAR),
                now.get(Calendar.MONTH),
                now.get(Calendar.DAY_OF_MONTH)
        );
        startPicker.setTitle("Chọn ngày bắt đầu");
        startPicker.show();
    }

    private void showEndDatePicker(Calendar start) {
        Calendar now = Calendar.getInstance();
        DatePickerDialog endPicker = new DatePickerDialog(
                this,
                (view, y, m, d) -> {
                    Calendar end = Calendar.getInstance();
                    end.set(y, m, d);
                    applyFilter(StatisticFilter.custom(start, end, "Tùy chỉnh"));
                },
                now.get(Calendar.YEAR),
                now.get(Calendar.MONTH),
                now.get(Calendar.DAY_OF_MONTH)
        );
        endPicker.setTitle("Chọn ngày kết thúc");
        endPicker.show();
    }

    private void showWalletFilterDialog() {
        List<String> names = new ArrayList<>();
        names.add("Tất cả ví");
        for (WalletEntity wallet : walletOptions) {
            names.add(wallet.name);
        }

        new AlertDialog.Builder(this)
                .setTitle("Chọn ví")
                .setItems(names.toArray(new String[0]), (dialog, which) -> {
                    if (which == 0) {
                        currentWalletLabel = "Tất cả ví";
                        viewModel.applyWallet(TYPE_EXPENSE, null);
                    } else {
                        String selected = names.get(which);
                        currentWalletLabel = selected;
                        viewModel.applyWallet(TYPE_EXPENSE, selected);
                    }
                    tvWalletFilter.setText(currentWalletLabel);
                })
                .show();
    }

    private void applyFilter(StatisticFilter filter) {
        updateFilterLabels(filter);
        viewModel.applyFilter(TYPE_EXPENSE, filter);
    }

    private void updateFilterLabels(StatisticFilter filter) {
        tvDateFilter.setText(filter.getDisplayLabel());
        tvWalletFilter.setText(currentWalletLabel);
    }

    private void highlightTabs() {
        tvExpenseTab.setTextColor(Color.WHITE);
        tvExpenseTab.setTextSize(18f);
        tvExpenseTab.setTypeface(tvExpenseTab.getTypeface(), android.graphics.Typeface.BOLD);

        tvIncomeTab.setTextColor(Color.parseColor("#777777"));
        tvIncomeTab.setTypeface(tvIncomeTab.getTypeface(), android.graphics.Typeface.NORMAL);
    }

    private void setupBottomNav() {
        bottomNavigation.setSelectedItemId(R.id.nav_statistic);

        bottomNavigation.setOnItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_home) {
                startActivity(new Intent(this, HomeActivity.class));
                return true;
            }

            if (id == R.id.nav_wallet) {
                startActivity(new Intent(this, WalletActivity.class));
                return true;
            }

            if (id == R.id.nav_add) {
                startActivity(new Intent(this, AddTransactionActivity.class));
                return true;
            }

            if (id == R.id.nav_statistic) {
                return true;
            }

            if (id == R.id.nav_settings) {
                startActivity(new Intent(this, SettingsActivity.class));
                return true;
            }

            return false;
        });
    }
}
