package com.finmate.UI.activities;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import com.finmate.adapters.TransactionAdapter;
import com.finmate.models.Transaction;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.List;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.finmate.R;

public class HomeActivity extends BaseActivity {

    LineChart lineChart;
    BottomNavigationView bottomNavigation;
    RecyclerView rvTransactions;
    TransactionAdapter transactionAdapter;
    List<Transaction> transactionList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        lineChart = findViewById(R.id.lineChart);
        bottomNavigation = findViewById(R.id.bottomNavigation);
        rvTransactions = findViewById(R.id.rvTransactions);

        // Đặt mục nav_home được chọn khi vào màn hình
        bottomNavigation.setSelectedItemId(R.id.nav_home);

        setupChart();
        setupBottomNavigation();
        setupRecyclerView();
    }

    // =======================================
    // 🚀 PHẦN 1: BIỂU ĐỒ THU – CHI
    // =======================================
    private void setupChart() {
        ArrayList<Entry> income = new ArrayList<>();
        ArrayList<Entry> expense = new ArrayList<>();

        // ----- Dữ liệu mẫu (tháng 1 - 6) -----
        income.add(new Entry(1, 60));
        income.add(new Entry(2, 80));
        income.add(new Entry(3, 90));
        income.add(new Entry(4, 70));
        income.add(new Entry(5, 85));
        income.add(new Entry(6, 60));

        expense.add(new Entry(1, 20));
        expense.add(new Entry(2, 40));
        expense.add(new Entry(3, 35));
        expense.add(new Entry(4, 50));
        expense.add(new Entry(5, 45));
        expense.add(new Entry(6, 55));

        LineDataSet incomeSet = new LineDataSet(income, "Thu nhập");
        incomeSet.setColor(Color.GREEN);
        incomeSet.setCircleColor(Color.GREEN);
        incomeSet.setLineWidth(2f);
        incomeSet.setValueTextColor(Color.WHITE);

        LineDataSet expenseSet = new LineDataSet(expense, "Chi tiêu");
        expenseSet.setColor(Color.MAGENTA);
        expenseSet.setCircleColor(Color.MAGENTA);
        expenseSet.setLineWidth(2f);
        expenseSet.setValueTextColor(Color.WHITE);

        LineData data = new LineData(incomeSet, expenseSet);
        lineChart.setData(data);

        // Tùy chỉnh trục X
        XAxis xAxis = lineChart.getXAxis();
        xAxis.setTextColor(Color.WHITE);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);

        // Trục Y trái
        YAxis leftAxis = lineChart.getAxisLeft();
        leftAxis.setTextColor(Color.WHITE);

        // Tắt trục Y phải
        lineChart.getAxisRight().setEnabled(false);

        // Chú thích
        Legend legend = lineChart.getLegend();
        legend.setTextColor(Color.WHITE);

        // Tắt mô tả góc
        lineChart.getDescription().setEnabled(false);

        // Animation
        lineChart.animateY(1000);
    }

    // =======================================
    // 🚀 PHẦN 2: BOTTOM NAVIGATION
    // =======================================
    private void setupBottomNavigation() {
        bottomNavigation.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                Intent intent = null;
                int itemId = item.getItemId();

                if (itemId == R.id.nav_home) {
                    // Đang ở màn hình Home, không làm gì
                    return true;
                } else if (itemId == R.id.nav_wallet) {
                    intent = new Intent(HomeActivity.this, WalletActivity.class);
                } else if (itemId == R.id.nav_add) {
                    intent = new Intent(HomeActivity.this, AddTransactionActivity.class);
                } else if (itemId == R.id.nav_statistic) {
                    intent = new Intent(HomeActivity.this, StatisticActivity.class);
                } else if (itemId == R.id.nav_settings) {
                    intent = new Intent(HomeActivity.this, SettingsActivity.class);
                }

                if (intent != null) {
                    startActivity(intent);
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                }
                return true;
            }
        });
    }

    // =======================================
    // 🚀 PHẦN 3: RECYCLERVIEW GIAO DỊCH
    // =======================================
    private void setupRecyclerView() {
        transactionList = new ArrayList<>();
        
        // Thêm dữ liệu giả
        transactionList.add(new Transaction("Ăn uống", "Riêng tôi", "-100,000 đ", "Ví của tôi", "22/04/2022"));
        transactionList.add(new Transaction("Lương", "Công ty", "+15,000,000 đ", "Ví ngân hàng", "21/04/2022"));
        transactionList.add(new Transaction("Xăng xe", "Riêng tôi", "-50,000 đ", "Ví của tôi", "20/04/2022"));
        transactionList.add(new Transaction("Mua sắm", "Gia đình", "-2,000,000 đ", "Ví chung", "19/04/2022"));
        transactionList.add(new Transaction("Thưởng", "Công ty", "+500,000 đ", "Ví ngân hàng", "18/04/2022"));
        transactionList.add(new Transaction("Ăn sáng", "Riêng tôi", "-35,000 đ", "Ví của tôi", "18/04/2022"));
        transactionList.add(new Transaction("Cafe", "Bạn bè", "-45,000 đ", "Ví của tôi", "17/04/2022"));
        transactionList.add(new Transaction("Điện nước", "Gia đình", "-1,200,000 đ", "Ví chung", "15/04/2022"));

        transactionAdapter = new TransactionAdapter(transactionList);
        rvTransactions.setLayoutManager(new LinearLayoutManager(this));
        rvTransactions.setAdapter(transactionAdapter);
    }
}
