package com.finmate;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.PieChart;
import com.google.android.material.bottomnavigation.BottomNavigationView;

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

        tvAddTitle = findViewById(R.id.tvAddTitle);
        tvCategoryFilter = findViewById(R.id.tvCategoryFilter);

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
        setupBarChart();
        setupPieChart();

        // ================== BOTTOM NAVIGATION ==================
        setupBottomNav();
    }

    // ================== HÀM VẼ BIỂU ĐỒ CỘT ==================
    private void setupBarChart() {
        barChartIncome.getDescription().setEnabled(false);
        barChartIncome.getLegend().setEnabled(false);
        barChartIncome.setDrawGridBackground(false);
        barChartIncome.setNoDataText("Không có dữ liệu");

        // TODO: Sau này bạn bind dữ liệu BE vào đây
    }

    // ================== HÀM VẼ BIỂU ĐỒ TRÒN ==================
    private void setupPieChart() {
        pieChartIncome.getDescription().setEnabled(false);
        pieChartIncome.setDrawHoleEnabled(true);
        pieChartIncome.setHoleRadius(45f);
        pieChartIncome.setTransparentCircleRadius(50f);
        pieChartIncome.setUsePercentValues(true);

        // TODO: Bind dữ liệu vào biểu đồ ở đây
    }

    // ================== HÀM BOTTOM NAV ==================
    private void setupBottomNav() {
        bottomNavigation.setSelectedItemId(R.id.nav_statistic);

        bottomNavigation.setOnNavigationItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_transaction) {
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
                return true; // Màn hình hiện tại
            }
            if (id == R.id.nav_setting) {
                // TODO: Điều hướng đến SettingActivity
                return true;
            }
            return false;
        });
    }
}
