package com.finmate.ui.transaction;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.finmate.R;
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
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.List;

public class StatisticActivity extends AppCompatActivity {

    ImageView btnBack;
    TextView tvExpenseTab, tvIncomeTab, tvTotalExpense;

    BarChart barChartExpense;
    PieChart pieChartExpense;

    BottomNavigationView bottomNavigation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_statistic);

        // ÁNH XẠ
        btnBack = findViewById(R.id.btnBack);
        tvExpenseTab = findViewById(R.id.tvExpenseTab);
        tvIncomeTab = findViewById(R.id.tvIncomeTab);
        tvTotalExpense = findViewById(R.id.tvTotalExpense);

        barChartExpense = findViewById(R.id.barChartExpense);
        pieChartExpense = findViewById(R.id.pieChartExpense);

        bottomNavigation = findViewById(R.id.bottomNavigation);

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

        // NẠP DỮ LIỆU CHART
        loadBarChartData();
        loadPieChartData();

        // BOTTOM NAV
        setupBottomNav();
    }

    // ===================== BAR CHART =====================
    private void loadBarChartData() {

        List<BarEntry> entries = new ArrayList<>();
        entries.add(new BarEntry(1, 5000000)); // Tháng 1
        entries.add(new BarEntry(2, 3500000)); // Tháng 2
        entries.add(new BarEntry(3, 4200000)); // Tháng 3
        entries.add(new BarEntry(4, 6100000)); // Tháng 4
        entries.add(new BarEntry(5, 4800000)); // Tháng 5

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

        barChartExpense.animateY(800);
        barChartExpense.invalidate();
    }

    // ===================== PIE CHART =====================
    private void loadPieChartData() {

        List<PieEntry> entries = new ArrayList<>();
        entries.add(new PieEntry(40f, "Ăn uống"));
        entries.add(new PieEntry(25f, "Đi lại"));
        entries.add(new PieEntry(20f, "Giải trí"));
        entries.add(new PieEntry(15f, "Mua sắm"));

        PieDataSet dataSet = new PieDataSet(entries, "");
        dataSet.setColors(
                Color.parseColor("#FF5252"),
                Color.parseColor("#FF8A65"),
                Color.parseColor("#FF7043"),
                Color.parseColor("#FFB74D")
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
