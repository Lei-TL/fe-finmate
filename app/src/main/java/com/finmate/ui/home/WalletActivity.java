package com.finmate.ui.home;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.ProgressBar;
import android.widget.TextView;
import com.finmate.ui.base.BaseActivity;
import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.finmate.R;
import com.finmate.core.util.TransactionFormatter;
import com.finmate.data.local.database.entity.TransactionEntity;
import com.finmate.data.repository.TransactionSyncManager;
import com.finmate.ui.activities.AddTransactionActivity;
import com.finmate.ui.base.BaseActivity;
import com.finmate.ui.settings.SettingsActivity;
import com.finmate.ui.transaction.StatisticActivity;
import com.finmate.ui.transaction.TransactionAdapter;
import com.finmate.ui.transaction.TransactionUIModel;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class WalletActivity extends BaseActivity {

    TextView tvBalance, tvIncomeValue, tvExpenseValue;
    ProgressBar progIncome, progExpense;
    BottomNavigationView bottomNavigation;
    RecyclerView rvTransactions;
    TransactionAdapter transactionAdapter;
    HomeViewModel viewModel;

    @Inject
    TransactionSyncManager transactionSyncManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wallet);

        viewModel = new ViewModelProvider(this).get(HomeViewModel.class);

        // ÁNH XẠ VIEW
        tvBalance = findViewById(R.id.tv_balance);
        tvIncomeValue = findViewById(R.id.tv_income_value);
        tvExpenseValue = findViewById(R.id.tv_expense_value);

        progIncome = findViewById(R.id.prog_income);
        progExpense = findViewById(R.id.prog_expense);

        bottomNavigation = findViewById(R.id.bottomNavigation);
        rvTransactions = findViewById(R.id.rvTransactions);

        // Chọn đúng tab hiện tại
        bottomNavigation.setSelectedItemId(R.id.nav_wallet);

        setupRecyclerView();
        setupWalletData();
        setupBottomNavigation();
        observeViewModel();
    }

    @Override
    protected void onResume() {
        super.onResume();
        viewModel.loadHomeData();
        transactionSyncManager.syncPendingTransactions();
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

    private void setupRecyclerView() {
        rvTransactions.setLayoutManager(new LinearLayoutManager(this));
        transactionAdapter = new TransactionAdapter(new ArrayList<>());
        rvTransactions.setAdapter(transactionAdapter);
    }

    private void observeViewModel() {
        viewModel.transactions.observe(this, this::updateTransactionList);
        viewModel.wallets.observe(this, wallets -> {
            // Tự động chọn ví đầu tiên nếu có
            if (wallets != null && !wallets.isEmpty() && viewModel.selectedWalletId.getValue() == null) {
                viewModel.selectWallet(String.valueOf(wallets.get(0).id));
            }
        });
    }

    private void updateTransactionList(List<TransactionEntity> transactionEntities) {
        if (transactionEntities == null) return;

        List<TransactionUIModel> uiModels = new ArrayList<>();
        for (TransactionEntity entity : transactionEntities) {
            // Format amount và date cho UI tại đây
            uiModels.add(new TransactionUIModel(
                    entity.getName(),
                    entity.getCategoryName(),
                    TransactionFormatter.formatAmount(entity.getAmount()),
                    entity.getWalletName(),
                    TransactionFormatter.formatDate(entity.getOccurredAt())
            ));
        }

        transactionAdapter.updateList(uiModels);
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
