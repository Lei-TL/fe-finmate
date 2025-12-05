package com.finmate.ui.activities;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.finmate.R;
import com.finmate.adapters.TransactionAdapter;
import com.finmate.ui.models.TransactionUIModel;
import com.finmate.entities.TransactionEntity;
import com.finmate.data.repository.TransactionRepository;
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

    private LineChart lineChart;
    private BottomNavigationView bottomNavigation;
    private RecyclerView rvTransactions;

    private TransactionAdapter transactionAdapter;
    private List<TransactionUIModel> transactionList;

    private TransactionRepository repository;

    private ImageView btnMenuMore;  // MENU 3 CHẤM

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        repository = new TransactionRepository(this);

        mapViews();
        setupMenuMore();         // <–– Thêm MENU 3 CHẤM
        setupChart();
        setupBottomNavigation();
        setupRecyclerView();
        loadTransactionsFromDB();
    }

    private void mapViews() {
        lineChart = findViewById(R.id.lineChart);
        bottomNavigation = findViewById(R.id.bottomNavigation);
        rvTransactions = findViewById(R.id.rvTransactions);
        btnMenuMore = findViewById(R.id.btnMenuMore); // ánh xạ nút 3 chấm

        bottomNavigation.setSelectedItemId(R.id.nav_home);
    }

    // =======================================
    // 🚀 MENU 3 CHẤM: CHỌN VÍ – THÊM VÍ
    // =======================================
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
        // Tạm thời dùng dữ liệu mẫu
        String[] wallets = {"Ví của tôi", "Ví ngân hàng", "Ví tiền mặt"};

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Chọn ví");

        builder.setItems(wallets, (dialog, which) -> {
            Toast.makeText(this, "Đã chọn: " + wallets[which], Toast.LENGTH_SHORT).show();
        });

        builder.show();
    }

    // =======================================
    // 🚀 LOAD DATA TRANSACTION
    // =======================================
    private void loadTransactionsFromDB() {
        repository.getAll(entities -> {
            List<TransactionUIModel> uiList = new ArrayList<>();

            for (TransactionEntity e : entities) {
                uiList.add(new TransactionUIModel(
                        e.name,
                        e.category,
                        e.amount,
                        e.wallet,
                        e.date
                ));
            }

            runOnUiThread(() -> {
                transactionList.clear();
                transactionList.addAll(uiList);
                transactionAdapter.notifyDataSetChanged();
            });
        });
    }

    // =======================================
    // 🚀 RECYCLERVIEW
    // =======================================
    private void setupRecyclerView() {
        transactionList = new ArrayList<>();
        transactionAdapter = new TransactionAdapter(transactionList);

        rvTransactions.setLayoutManager(new LinearLayoutManager(this));
        rvTransactions.setAdapter(transactionAdapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadTransactionsFromDB();
    }

    // =======================================
    // 🚀 BIỂU ĐỒ
    // =======================================
    private void setupChart() {
        ArrayList<Entry> income = new ArrayList<>();
        ArrayList<Entry> expense = new ArrayList<>();

        income.add(new Entry(1, 60));
        income.add(new Entry(2, 80));
        income.add(new Entry(3, 90));
        expense.add(new Entry(1, 20));
        expense.add(new Entry(2, 40));
        expense.add(new Entry(3, 35));

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
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);

        YAxis leftAxis = lineChart.getAxisLeft();
        leftAxis.setTextColor(Color.WHITE);

        lineChart.getAxisRight().setEnabled(false);
        lineChart.getDescription().setEnabled(false);

        Legend legend = lineChart.getLegend();
        legend.setTextColor(Color.WHITE);

        lineChart.animateY(800);
    }

    // =======================================
    // 🚀 NAVIGATION
    // =======================================
    private void setupBottomNavigation() {
        bottomNavigation.setOnNavigationItemSelectedListener(item -> {
            Intent intent = null;

            if (item.getItemId() == R.id.nav_home) {
                return true;
            } else if (item.getItemId() == R.id.nav_wallet) {
                intent = new Intent(this, WalletActivity.class);
            } else if (item.getItemId() == R.id.nav_add) {
                intent = new Intent(this, AddTransactionActivity.class);
            } else if (item.getItemId() == R.id.nav_statistic) {
                intent = new Intent(this, StatisticActivity.class);
            } else if (item.getItemId() == R.id.nav_settings) {
                intent = new Intent(this, SettingsActivity.class);
            }

            if (intent != null) startActivity(intent);
            return true;
        });
    }
}
