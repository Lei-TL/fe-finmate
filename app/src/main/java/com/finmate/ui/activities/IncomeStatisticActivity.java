package com.finmate.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.finmate.R;
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
import com.google.android.material.bottomnavigation.BottomNavigationView;

import android.graphics.Color;

import java.util.ArrayList;
import java.util.List;

public class IncomeStatisticActivity extends AppCompatActivity {

    ImageView btnBack;
    TextView tvExpenseTab, tvIncomeTab;
    TextView tvTotalIncome, tvAddTitle, tvCategoryFilter;
    BarChart barChartIncome;
    PieChart pieChartIncome;
    BottomNavigationView bottomNavigation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_income_statistic);

        // ================== ÁNH XẠ VIEW ==================
        btnBack = findViewById(R.id.btnBack);
        tvExpenseTab = findViewById(R.id.tvExpenseTab);
        tvIncomeTab = findViewById(R.id.tvIncomeTab);
        tvTotalIncome = findViewById(R.id.tvTotalIncome);

        barChartIncome = findViewById(R.id.barChartIncome);
        pieChartIncome = findViewById(R.id.pieChartIncome);

        bottomNavigation = findViewById(R.id.bottomNavigation);

        // ================== BACK ==================
        btnBack.setOnClickListener(v -> finish());

        // ================== TAB CHUYỂN QUA CHI TIÊU ==================
        tvExpenseTab.setOnClickListener(v -> {
            Intent intent = new Intent(this, StatisticActivity.class);
            startActivity(intent);
            finish();
        });

        // Tab Thu nhập đang active -> Không làm gì
        tvIncomeTab.setOnClickListener(v -> {});

        // ================== CHART SETUP ==================
        loadBarChartData();
        loadPieChartData();

        // ================== BOTTOM NAVIGATION ==================
        setupBottomNav();
    }

    // ================== DỮ LIỆU MẪU THU NHẬP THEO THÁNG ==================
    private void loadBarChartData() {
        List<BarEntry> entries = new ArrayList<>();
        entries.add(new BarEntry(1, 5000000));
        entries.add(new BarEntry(2, 8000000));
        entries.add(new BarEntry(3, 6000000));
        entries.add(new BarEntry(4, 9000000));
        entries.add(new BarEntry(5, 10000000));

        BarDataSet dataSet = new BarDataSet(entries, "Thu nhập");
        dataSet.setColor(Color.parseColor("#4CAF50"));
        dataSet.setValueTextColor(Color.WHITE);
        dataSet.setValueTextSize(10f);

        BarData barData = new BarData(dataSet);
        barData.setBarWidth(0.35f);

        barChartIncome.setData(barData);
        barChartIncome.setFitBars(true);

        // Tắt mô tả
        barChartIncome.getDescription().setEnabled(false);
        barChartIncome.getLegend().setEnabled(false);

        // Cài đặt trục X
        XAxis xAxis = barChartIncome.getXAxis();
        xAxis.setTextColor(Color.WHITE);
        xAxis.setDrawGridLines(false);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);

        barChartIncome.invalidate();
    }

    // ================== BIỂU ĐỒ TRÒN ==================
    private void loadPieChartData() {
        List<PieEntry> entries = new ArrayList<>();
        entries.add(new PieEntry(40f, "Lương"));
        entries.add(new PieEntry(25f, "Thưởng"));
        entries.add(new PieEntry(15f, "Kinh doanh"));
        entries.add(new PieEntry(20f, "Khác"));

        PieDataSet dataSet = new PieDataSet(entries, "");
        dataSet.setColors(
                Color.parseColor("#4CAF50"),
                Color.parseColor("#8BC34A"),
                Color.parseColor("#CDDC39"),
                Color.parseColor("#FFC107")
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

        pieChartIncome.invalidate();
    }

    // ================== BOTTOM NAV ==================
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
