package com.finmate.ui.home;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.finmate.R;
import com.finmate.ui.activities.AddTransactionActivity;
import com.finmate.ui.activities.AddWalletActivity;
import com.finmate.ui.activities.SettingsActivity;
import com.finmate.ui.activities.StatisticActivity;
import com.finmate.ui.transaction.TransactionUIModel;
import com.finmate.ui.transaction.TransactionAdapter;
import com.finmate.data.local.database.entity.TransactionEntity;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.List;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class HomeActivity extends AppCompatActivity {

    private HomeViewModel viewModel;

    private LineChart lineChart;
    private BottomNavigationView bottomNavigation;
    private RecyclerView rvTransactions;
    private TransactionAdapter transactionAdapter;
    private ImageView btnMenuMore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        viewModel = new ViewModelProvider(this).get(HomeViewModel.class);

        mapViews();
        setupMenuMore();
        setupRecyclerView();
        setupBottomNavigation();
        
        observeViewModel();
        viewModel.loadHomeData(); // Load initial data
    }

    private void mapViews() {
        lineChart = findViewById(R.id.lineChart);
        bottomNavigation = findViewById(R.id.bottomNavigation);
        rvTransactions = findViewById(R.id.rvTransactions);
        btnMenuMore = findViewById(R.id.btnMenuMore);

        bottomNavigation.setSelectedItemId(R.id.nav_home);
    }

    private void observeViewModel() {
        viewModel.transactions.observe(this, this::updateTransactionList);
        viewModel.wallets.observe(this, wallets -> {
            // Update wallet selector UI if needed
        });
        // Observe chart data later
    }

    private void updateTransactionList(List<TransactionEntity> transactionEntities) {
        if (transactionEntities == null) return;

        List<TransactionUIModel> uiModels = new ArrayList<>();
        for (TransactionEntity entity : transactionEntities) {
            uiModels.add(new TransactionUIModel(
                    entity.name,
                    entity.category,
                    entity.amount,
                    entity.wallet,
                    entity.date
            ));
        }

        transactionAdapter.updateList(uiModels); 
    }

    private void setupMenuMore() {
        btnMenuMore.setOnClickListener(v -> {
            PopupMenu popup = new PopupMenu(HomeActivity.this, btnMenuMore);
            popup.getMenuInflater().inflate(R.menu.menu_wallet_options, popup.getMenu());

            popup.setOnMenuItemClickListener(item -> {
                int id = item.getItemId();
                if (id == R.id.action_choose_wallet) {
                    openChooseWalletDialog();
                    return true;
                }
                if (id == R.id.action_add_wallet) {
                    startActivity(new Intent(HomeActivity.this, AddWalletActivity.class));
                    return true;
                }
                return false;
            });
            popup.show();
        });
    }

    private void openChooseWalletDialog() {
        viewModel.wallets.observe(this, wallets -> {
            if (wallets == null || wallets.isEmpty()) return;

            String[] walletNames = new String[wallets.size()];
            for (int i = 0; i < wallets.size(); i++) {
                walletNames[i] = wallets.get(i).name;
            }

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Chọn ví");
            builder.setItems(walletNames, (dialog, which) -> {
                String selectedWalletId = String.valueOf(wallets.get(which).id);
                viewModel.selectWallet(selectedWalletId);
                Toast.makeText(this, "Đã chọn: " + walletNames[which], Toast.LENGTH_SHORT).show();
            });
            builder.show();
        });
    }

    private void setupRecyclerView() {
        rvTransactions.setLayoutManager(new LinearLayoutManager(this));
        transactionAdapter = new TransactionAdapter(new ArrayList<>()); 
        rvTransactions.setAdapter(transactionAdapter);
    }

    private void setupChart(List<TransactionEntity> transactions) {
        // This should be derived from the transaction list
        ArrayList<Entry> income = new ArrayList<>();
        ArrayList<Entry> expense = new ArrayList<>();

        income.add(new Entry(1, 60));
        expense.add(new Entry(1, 20));

        LineDataSet incomeSet = new LineDataSet(income, "Thu nhập");
        incomeSet.setColor(Color.GREEN);

        LineDataSet expenseSet = new LineDataSet(expense, "Chi tiêu");
        expenseSet.setColor(Color.RED);

        LineData data = new LineData(incomeSet, expenseSet);
        lineChart.setData(data);
        lineChart.invalidate(); // Refresh chart
    }

    private void setupBottomNavigation() {
        bottomNavigation.setOnNavigationItemSelectedListener(item -> {
            Intent intent = null;
            int id = item.getItemId();

            if (id == R.id.nav_home) return true;
            if (id == R.id.nav_wallet) intent = new Intent(this, WalletActivity.class);
            if (id == R.id.nav_add) intent = new Intent(this, AddTransactionActivity.class);
            if (id == R.id.nav_statistic) intent = new Intent(this, StatisticActivity.class);
            if (id == R.id.nav_settings) intent = new Intent(this, SettingsActivity.class);

            if (intent != null) startActivity(intent);
            return true;
        });
    }
}
