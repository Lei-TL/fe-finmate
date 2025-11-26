package com.finmate.UI.activities;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.finmate.R;

public class StatisticActivity extends AppCompatActivity {

    ImageView btnBack;
    TextView tvExpenseTab, tvIncomeTab, tvTotalExpense;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_statistic);

        btnBack = findViewById(R.id.btnBack);
        tvExpenseTab = findViewById(R.id.tvExpenseTab);
        tvIncomeTab = findViewById(R.id.tvIncomeTab);
        tvTotalExpense = findViewById(R.id.tvTotalExpense);

        btnBack.setOnClickListener(v -> finish());
    }
}
