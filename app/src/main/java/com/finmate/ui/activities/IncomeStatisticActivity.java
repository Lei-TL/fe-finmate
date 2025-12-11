package com.finmate.ui.activities;

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
import com.finmate.ui.home.HomeActivity;
import com.finmate.ui.home.WalletActivity;
import com.finmate.ui.settings.SettingsActivity;
import com.finmate.ui.transaction.ChartPoint;
import com.finmate.ui.transaction.StatisticActivity;
import com.finmate.ui.transaction.StatisticResult;
import com.finmate.ui.transaction.StatisticViewModel;
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
public class IncomeStatisticActivity extends AppCompatActivity {

    private static final String TYPE_INCOME = "INCOME";

    ImageView btnBack;
    TextView tvExpenseTab, tvIncomeTab;
    TextView tvTotalIncome, tvDateFilter, tvWalletFilter;
    BarChart barChartIncome;
    PieChart pieChartIncome;
    BottomNavigationView bottomNavigation;

    StatisticViewModel viewModel;
    List<WalletEntity> walletOptions = new ArrayList<>();
    String currentWalletLabel = "Tất cả ví";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_income_statistic);

        viewModel = new ViewModelProvider(this).get(StatisticViewModel.class);

        btnBack = findViewById(R.id.btnBack);
        tvExpenseTab = findViewById(R.id.tvExpenseTab);
        tvIncomeTab = findViewById(R.id.tvIncomeTab);
        tvTotalIncome = findViewById(R.id.tvTotalIncome);
        tvDateFilter = findViewById(R.id.tvDateFilter);
        tvWalletFilter = findViewById(R.id.tvWalletFilter);

        barChartIncome = findViewById(R.id.barChartIncome);
        pieChartIncome = findViewById(R.id.pieChartIncome);

        bottomNavigation = findViewById(R.id.bottomNavigation);

        highlightTabs();
        setupBottomNav();
        setupEvents();
        setupObservers();

        updateFilterLabels(viewModel.getCurrentFilter());
        viewModel.loadWallets();
        viewModel.loadStatistics(TYPE_INCOME);
    }

    private void setupEvents() {
        btnBack.setOnClickListener(v -> finish());

        tvExpenseTab.setOnClickListener(v -> {
            Intent intent = new Intent(this, StatisticActivity.class);
            startActivity(intent);
            finish();
        });

        tvIncomeTab.setOnClickListener(v -> { /* already on income */ });

        tvDateFilter.setOnClickListener(v -> showDateFilterDialog());
        tvWalletFilter.setOnClickListener(v -> showWalletFilterDialog());
    }

    private void setupObservers() {
        viewModel.statistic.observe(this, this::renderStatistic);
        viewModel.wallets.observe(this, wallets -> walletOptions = wallets != null ? wallets : new ArrayList<>());
    }

    private void renderStatistic(StatisticResult result) {
        String formatted = TransactionFormatter.formatAmount(result.getTotalAmount());
        tvTotalIncome.setText("+" + formatted + " VND");

        renderBarChart(result.getTimeline());
        renderPieChart(result.getByCategory());
    }

    private void renderBarChart(List<ChartPoint> data) {
        barChartIncome.clear();

        if (data == null || data.isEmpty()) {
            barChartIncome.setNoDataText("Không có dữ liệu");
            barChartIncome.invalidate();
            return;
        }

        List<BarEntry> entries = new ArrayList<>();
        List<String> labels = new ArrayList<>();
        for (int i = 0; i < data.size(); i++) {
            entries.add(new BarEntry(i, data.get(i).getValue()));
            labels.add(data.get(i).getLabel());
        }

        BarDataSet dataSet = new BarDataSet(entries, "Thu nhập");
        dataSet.setColor(Color.parseColor("#4CAF50"));
        dataSet.setValueTextColor(Color.WHITE);
        dataSet.setValueTextSize(10f);

        BarData barData = new BarData(dataSet);
        barChartIncome.setData(barData);
        barChartIncome.setFitBars(true);

        barChartIncome.getDescription().setEnabled(false);
        barChartIncome.getLegend().setEnabled(false);

        XAxis xAxis = barChartIncome.getXAxis();
        xAxis.setTextColor(Color.WHITE);
        xAxis.setDrawGridLines(false);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);
        xAxis.setValueFormatter(new IndexAxisValueFormatter(labels));
        xAxis.setLabelCount(labels.size());

        barChartIncome.animateY(500);
        barChartIncome.invalidate();
    }

    private void renderPieChart(List<ChartPoint> data) {
        pieChartIncome.clear();

        if (data == null || data.isEmpty()) {
            pieChartIncome.setNoDataText("Không có dữ liệu");
            pieChartIncome.invalidate();
            return;
        }

        List<PieEntry> entries = new ArrayList<>();
        for (ChartPoint point : data) {
            entries.add(new PieEntry(point.getValue(), point.getLabel()));
        }

        PieDataSet dataSet = new PieDataSet(entries, "");
        dataSet.setColors(
                Color.parseColor("#4CAF50"),
                Color.parseColor("#8BC34A"),
                Color.parseColor("#CDDC39"),
                Color.parseColor("#FFC107"),
                Color.parseColor("#AED581")
        );

        dataSet.setValueTextColor(Color.WHITE);
        dataSet.setValueTextSize(12f);

        PieData pieData = new PieData(dataSet);

        pieChartIncome.setData(pieData);
        pieChartIncome.setUsePercentValues(true);
        pieChartIncome.setDrawHoleEnabled(true);
        pieChartIncome.setHoleRadius(40f);
        pieChartIncome.setTransparentCircleRadius(45f);
        pieChartIncome.getDescription().setEnabled(false);

        Legend legend = pieChartIncome.getLegend();
        legend.setTextColor(Color.WHITE);
        legend.setTextSize(12f);

        pieChartIncome.animateY(500);
        pieChartIncome.invalidate();
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
                        viewModel.applyWallet(TYPE_INCOME, null);
                    } else {
                        String selected = names.get(which);
                        currentWalletLabel = selected;
                        viewModel.applyWallet(TYPE_INCOME, selected);
                    }
                    tvWalletFilter.setText(currentWalletLabel);
                })
                .show();
    }

    private void applyFilter(StatisticFilter filter) {
        updateFilterLabels(filter);
        viewModel.applyFilter(TYPE_INCOME, filter);
    }

    private void updateFilterLabels(StatisticFilter filter) {
        tvDateFilter.setText(filter.getDisplayLabel());
        tvWalletFilter.setText(currentWalletLabel);
    }

    private void highlightTabs() {
        tvIncomeTab.setTextColor(Color.WHITE);
        tvIncomeTab.setTextSize(18f);
        tvIncomeTab.setTypeface(tvIncomeTab.getTypeface(), android.graphics.Typeface.BOLD);

        tvExpenseTab.setTextColor(Color.parseColor("#777777"));
        tvExpenseTab.setTypeface(tvExpenseTab.getTypeface(), android.graphics.Typeface.NORMAL);
    }

    private void setupBottomNav() {
        bottomNavigation.setSelectedItemId(R.id.nav_statistic);

        bottomNavigation.setOnNavigationItemSelectedListener(item -> {
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
