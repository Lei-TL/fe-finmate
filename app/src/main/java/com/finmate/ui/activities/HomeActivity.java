package com.finmate.ui.activities;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;

import com.finmate.R;
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

        bottomNavigation.setSelectedItemId(R.id.nav_home);

        setupChart();
        setupBottomNavigation();
        setupRecyclerView();

        // Register RecyclerView for context menu
        registerForContextMenu(rvTransactions);
    }

    @Override
    public boolean onContextItemSelected(@NonNull MenuItem item) {
        int position = transactionAdapter.getLongClickedPosition();
        if (position == -1) {
            return super.onContextItemSelected(item);
        }

        Transaction selectedTransaction = transactionAdapter.getTransactionAt(position);

        switch (item.getItemId()) {
            case R.id.action_edit:
                // Navigate to AddTransactionActivity to edit
                Intent intent = new Intent(this, AddTransactionActivity.class);
                // TODO: Pass transaction data to the activity
                // intent.putExtra("transaction_id", selectedTransaction.getId());
                startActivity(intent);
                return true;

            case R.id.action_delete:
                // Show a confirmation dialog before deleting
                new AlertDialog.Builder(this)
                        .setTitle(R.string.delete)
                        .setMessage("Are you sure you want to delete this transaction?") // TODO: Add to strings.xml
                        .setPositiveButton(R.string.delete, (dialog, which) -> {
                            // TODO: Implement delete logic
                            Toast.makeText(this, "Transaction deleted (simulation)", Toast.LENGTH_SHORT).show();
                        })
                        .setNegativeButton(R.string.cancel, null)
                        .show();
                return true;
        }
        return super.onContextItemSelected(item);
    }

    private void setupChart() {
        ArrayList<Entry> income = new ArrayList<>();
        ArrayList<Entry> expense = new ArrayList<>();

        income.add(new Entry(1, 60));
        income.add(new Entry(2, 80));
        income.add(new Entry(3, 90));
        income.add(new Entry(4, 70));

        expense.add(new Entry(1, 20));
        expense.add(new Entry(2, 40));
        expense.add(new Entry(3, 35));
        expense.add(new Entry(4, 50));

        LineDataSet incomeSet = new LineDataSet(income, "Thu nhập");
        incomeSet.setColor(Color.GREEN);
        incomeSet.setCircleColor(Color.GREEN);

        LineDataSet expenseSet = new LineDataSet(expense, "Chi tiêu");
        expenseSet.setColor(Color.RED);
        expenseSet.setCircleColor(Color.RED);

        LineData data = new LineData(incomeSet, expenseSet);
        lineChart.setData(data);

        XAxis xAxis = lineChart.getXAxis();
        xAxis.setTextColor(Color.WHITE);
        YAxis leftAxis = lineChart.getAxisLeft();
        leftAxis.setTextColor(Color.WHITE);
        lineChart.getAxisRight().setEnabled(false);
        lineChart.getDescription().setEnabled(false);
        Legend legend = lineChart.getLegend();
        legend.setTextColor(Color.WHITE);
        lineChart.animateY(1000);
    }

    private void setupBottomNavigation() {
        bottomNavigation.setOnNavigationItemSelectedListener(item -> {
            Intent intent = null;
            int itemId = item.getItemId();

            if (itemId == R.id.nav_home) {
                return true;
            } else if (itemId == R.id.nav_wallet) {
                intent = new Intent(this, WalletActivity.class);
            } else if (itemId == R.id.nav_add) {
                intent = new Intent(this, AddTransactionActivity.class);
            } else if (itemId == R.id.nav_statistic) {
                intent = new Intent(this, StatisticActivity.class);
            } else if (itemId == R.id.nav_settings) {
                intent = new Intent(this, SettingsActivity.class);
            }

            if (intent != null) {
                startActivity(intent);
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            }
            return true;
        });
    }

    private void setupRecyclerView() {
        transactionList = new ArrayList<>();
        transactionList.add(new Transaction("Ăn uống", "Riêng tôi", "-100,000 đ", "Ví của tôi", "22/04/2022"));
        transactionList.add(new Transaction("Lương", "Công ty", "+15,000,000 đ", "Ví ngân hàng", "21/04/2022"));
        transactionList.add(new Transaction("Xăng xe", "Riêng tôi", "-50,000 đ", "Ví của tôi", "20/04/2022"));

        transactionAdapter = new TransactionAdapter(transactionList);
        rvTransactions.setLayoutManager(new LinearLayoutManager(this));
        rvTransactions.setAdapter(transactionAdapter);
    }
}
