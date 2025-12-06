package com.finmate.ui.home;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.finmate.R;
import com.finmate.ui.activities.AddTransactionActivity;
import com.finmate.ui.activities.SettingsActivity;
import com.finmate.ui.activities.StatisticActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class WalletActivity extends BaseActivity {

    TextView tvBalance, tvIncomeValue, tvExpenseValue;
    ProgressBar progIncome, progExpense;
    BottomNavigationView bottomNavigation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wallet);

        // ÁNH XẠ VIEW
        tvBalance = findViewById(R.id.tv_balance);
        tvIncomeValue = findViewById(R.id.tv_income_value);
        tvExpenseValue = findViewById(R.id.tv_expense_value);

        progIncome = findViewById(R.id.prog_income);
        progExpense = findViewById(R.id.prog_expense);

        bottomNavigation = findViewById(R.id.bottomNavigation);

        // Chọn đúng tab hiện tại
        bottomNavigation.setSelectedItemId(R.id.nav_wallet);

        setupWalletData();
        setupBottomNavigation();
    }

    // ====================== HIỂN THỊ DỮ LIỆU TĨNH ======================
    private void setupWalletData() {

        int income = 60000000;    // Thu nhập
        int expense = 10000000;   // Chi tiêu
        int balance = income - expense;

        // Gán dữ liệu
        tvBalance.setText(formatMoney(balance) + " VND");
        tvIncomeValue.setText("+" + formatMoney(income) + " VND");
        tvExpenseValue.setText("-" + formatMoney(expense) + " VND");

        // Tính %
        int total = income + expense;
        if (total > 0) {
            int perIncome = (income * 100) / total;
            int perExpense = (expense * 100) / total;
            progIncome.setProgress(perIncome);
            progExpense.setProgress(perExpense);
        } else {
            progIncome.setProgress(0);
            progExpense.setProgress(0);
        }
    }

    // Format tiền VND đẹp
    private String formatMoney(int amount) {
        return String.format("%,d", amount).replace(",", ".");
    }


    // ====================== XỬ LÝ BOTTOM NAVIGATION ======================
    private void setupBottomNavigation() {
        bottomNavigation.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                Intent intent = null;
                if (item.getItemId() == R.id.nav_home) {
                    intent = new Intent(WalletActivity.this, HomeActivity.class);
                } else if (item.getItemId() == R.id.nav_wallet) {
                    // Already on Wallet screen
                    return true;
                } else if (item.getItemId() == R.id.nav_add) {
                    intent = new Intent(WalletActivity.this, AddTransactionActivity.class);
                } else if (item.getItemId() == R.id.nav_statistic) {
                    intent = new Intent(WalletActivity.this, StatisticActivity.class);
                } else if (item.getItemId() == R.id.nav_settings) {
                    intent = new Intent(WalletActivity.this, SettingsActivity.class);
                }

                if (intent != null) {
                    startActivity(intent);
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                }
                return true;
            }
        });
    }
}
