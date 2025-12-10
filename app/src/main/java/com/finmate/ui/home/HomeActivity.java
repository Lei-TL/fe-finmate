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
import com.finmate.core.util.TransactionFormatter;
import com.finmate.data.repository.TransactionSyncManager;
import com.finmate.ui.activities.AddTransactionActivity;
import com.finmate.ui.activities.AddWalletActivity;
import com.finmate.ui.settings.SettingsActivity;
import com.finmate.ui.transaction.StatisticActivity;
import com.finmate.ui.transaction.TransactionUIModel;
import com.finmate.ui.transaction.TransactionAdapter;
import com.finmate.data.local.database.entity.TransactionEntity;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class HomeActivity extends AppCompatActivity {

    @Inject
    TransactionSyncManager transactionSyncManager;

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
    }

    @Override
    protected void onResume() {
        super.onResume();
        viewModel.loadHomeData();
        transactionSyncManager.syncPendingTransactions();
    }

    private void mapViews() {
        lineChart = findViewById(R.id.lineChart);
        bottomNavigation = findViewById(R.id.bottomNavigation);
        rvTransactions = findViewById(R.id.rvTransactions);
        btnMenuMore = findViewById(R.id.btnMenuMore);

        bottomNavigation.setSelectedItemId(R.id.nav_home);
    }

    private void observeViewModel() {
        viewModel.transactions.observe(this, transactions -> {
            updateTransactionList(transactions);
            setupChart(transactions);
        });
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
            // Format amount và date cho UI tại đây, không lưu format trong Entity
            uiModels.add(new TransactionUIModel(
                    entity.name,
                    entity.category,
                    TransactionFormatter.formatAmount(entity.amount),  // Format amount
                    entity.wallet,
                    TransactionFormatter.formatDate(entity.occurredAt)  // Format date
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
        List<com.finmate.data.local.database.entity.WalletEntity> wallets = viewModel.wallets.getValue();
        if (wallets == null || wallets.isEmpty()) {
            Toast.makeText(this, "Chưa có ví nào", Toast.LENGTH_SHORT).show();
            return;
        }

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
    }

    private void setupRecyclerView() {
        rvTransactions.setLayoutManager(new LinearLayoutManager(this));
        transactionAdapter = new TransactionAdapter(new ArrayList<>()); 
        rvTransactions.setAdapter(transactionAdapter);
    }

    private void setupChart(List<TransactionEntity> transactions) {
        if (transactions == null || transactions.isEmpty()) {
            lineChart.clear();
            lineChart.invalidate();
            return;
        }

        // Tính toán income và expense theo tháng (6 tháng gần nhất)
        Map<Integer, Double> incomeByMonth = new HashMap<>();
        Map<Integer, Double> expenseByMonth = new HashMap<>();
        
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US);
        SimpleDateFormat dateFormatWithMillis = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS", Locale.US);
        
        // Khởi tạo 6 tháng gần nhất với giá trị 0
        Calendar tempCalendar = Calendar.getInstance();
        for (int i = 5; i >= 0; i--) {
            tempCalendar.setTimeInMillis(calendar.getTimeInMillis());
            tempCalendar.add(Calendar.MONTH, -i);
            int monthKey = tempCalendar.get(Calendar.YEAR) * 100 + tempCalendar.get(Calendar.MONTH);
            incomeByMonth.put(monthKey, 0.0);
            expenseByMonth.put(monthKey, 0.0);
        }

        // Phân loại transactions theo tháng
        for (TransactionEntity transaction : transactions) {
            Date date = null;
            // Thử parse với format có milliseconds trước
            try {
                date = dateFormatWithMillis.parse(transaction.occurredAt);
            } catch (ParseException e) {
                // Nếu không được, thử format không có milliseconds
                try {
                    date = dateFormat.parse(transaction.occurredAt);
                } catch (ParseException ex) {
                    // Bỏ qua transaction không parse được
                    continue;
                }
            }
            
            if (date != null) {
                calendar.setTime(date);
                int monthKey = calendar.get(Calendar.YEAR) * 100 + calendar.get(Calendar.MONTH);
                
                // Chỉ tính toán nếu tháng nằm trong 6 tháng gần nhất
                if (incomeByMonth.containsKey(monthKey)) {
                    // Giả định: amount > 0 là income, amount < 0 là expense
                    // Hoặc có thể dựa vào category type nếu có
                    if (transaction.amount > 0) {
                        double currentIncome = incomeByMonth.getOrDefault(monthKey, 0.0);
                        incomeByMonth.put(monthKey, currentIncome + Math.abs(transaction.amount));
                    } else {
                        double currentExpense = expenseByMonth.getOrDefault(monthKey, 0.0);
                        expenseByMonth.put(monthKey, currentExpense + Math.abs(transaction.amount));
                    }
                }
            }
        }

        // Tạo entries cho chart (6 tháng gần nhất)
        ArrayList<Entry> incomeEntries = new ArrayList<>();
        ArrayList<Entry> expenseEntries = new ArrayList<>();
        
        List<Integer> sortedMonths = new ArrayList<>(incomeByMonth.keySet());
        sortedMonths.sort(Integer::compareTo);
        
        int index = 1;
        for (Integer monthKey : sortedMonths) {
            double income = incomeByMonth.getOrDefault(monthKey, 0.0);
            double expense = expenseByMonth.getOrDefault(monthKey, 0.0);
            
            // Chuyển đổi sang triệu VND để hiển thị đẹp hơn
            incomeEntries.add(new Entry(index, (float) (income / 1000000)));
            expenseEntries.add(new Entry(index, (float) (expense / 1000000)));
            index++;
        }

        // Tạo dataset cho income
        LineDataSet incomeSet = new LineDataSet(incomeEntries, "Thu nhập");
        incomeSet.setColor(Color.parseColor("#4CAF50"));
        incomeSet.setLineWidth(2.5f);
        incomeSet.setCircleColor(Color.parseColor("#4CAF50"));
        incomeSet.setCircleRadius(4f);
        incomeSet.setValueTextColor(Color.WHITE);
        incomeSet.setValueTextSize(10f);
        incomeSet.setDrawValues(false); // Ẩn giá trị trên điểm để chart gọn hơn

        // Tạo dataset cho expense
        LineDataSet expenseSet = new LineDataSet(expenseEntries, "Chi tiêu");
        expenseSet.setColor(Color.parseColor("#FF5252"));
        expenseSet.setLineWidth(2.5f);
        expenseSet.setCircleColor(Color.parseColor("#FF5252"));
        expenseSet.setCircleRadius(4f);
        expenseSet.setValueTextColor(Color.WHITE);
        expenseSet.setValueTextSize(10f);
        expenseSet.setDrawValues(false);

        LineData data = new LineData(incomeSet, expenseSet);

        // Cấu hình chart
        lineChart.setData(data);
        lineChart.getDescription().setEnabled(false);
        lineChart.setTouchEnabled(true);
        lineChart.setDragEnabled(true);
        lineChart.setScaleEnabled(true);
        lineChart.setPinchZoom(true);
        lineChart.setDrawGridBackground(false);
        lineChart.setNoDataText("Không có dữ liệu");

        // Cấu hình legend
        Legend legend = lineChart.getLegend();
        legend.setEnabled(true);
        legend.setTextColor(Color.WHITE);
        legend.setTextSize(12f);
        legend.setForm(Legend.LegendForm.LINE);
        legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.CENTER);

        // Cấu hình trục X
        XAxis xAxis = lineChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setTextColor(Color.WHITE);
        xAxis.setDrawGridLines(false);
        xAxis.setGranularity(1f);
        xAxis.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                int monthIndex = (int) value;
                if (monthIndex >= 1 && monthIndex <= 6) {
                    String[] monthLabels = {"T1", "T2", "T3", "T4", "T5", "T6"};
                    return monthLabels[monthIndex - 1];
                }
                return "";
            }
        });

        // Cấu hình trục Y
        YAxis leftAxis = lineChart.getAxisLeft();
        leftAxis.setTextColor(Color.WHITE);
        leftAxis.setDrawGridLines(true);
        leftAxis.setGridColor(Color.parseColor("#33FFFFFF"));
        leftAxis.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return String.format(Locale.getDefault(), "%.1fM", value);
            }
        });

        YAxis rightAxis = lineChart.getAxisRight();
        rightAxis.setEnabled(false);

        lineChart.animateX(800);
        lineChart.invalidate();
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
