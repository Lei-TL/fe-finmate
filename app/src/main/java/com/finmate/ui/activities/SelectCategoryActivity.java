package com.finmate.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.finmate.R;
import com.finmate.ui.transaction.CategoryUIModel;
import com.finmate.ui.transaction.CategoryGridAdapter;

import java.util.ArrayList;
import java.util.List;

public class SelectCategoryActivity extends AppCompatActivity {

    private ImageView btnBack;
    private TextView tabExpense, tabIncome;
    private RecyclerView rvCategories;

    private List<CategoryUIModel> expenseList;
    private List<CategoryUIModel> incomeList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_category);

        btnBack = findViewById(R.id.btnBack);
        tabExpense = findViewById(R.id.tabExpense);
        tabIncome = findViewById(R.id.tabIncome);
        rvCategories = findViewById(R.id.rvCategories);

        btnBack.setOnClickListener(v -> finish());

        setupData();
        loadExpenseList(); // Tải danh sách chi tiêu làm mặc định

        tabExpense.setOnClickListener(v -> loadExpenseList());
        tabIncome.setOnClickListener(v -> loadIncomeList());
    }

    // Sửa lại để dùng đúng UIModel và đúng thứ tự tham số
    private void setupData() {
        // Chi tiêu
        expenseList = new ArrayList<>();
        expenseList.add(new CategoryUIModel(R.drawable.ic_food, "Ăn uống"));
        expenseList.add(new CategoryUIModel(R.drawable.ic_car, "Đi lại"));
        expenseList.add(new CategoryUIModel(R.drawable.ic_shopping, "Mua sắm"));
        expenseList.add(new CategoryUIModel(R.drawable.ic_bill, "Hóa đơn"));

        // Thu nhập
        incomeList = new ArrayList<>();
        incomeList.add(new CategoryUIModel(R.drawable.ic_salary, "Lương"));
        incomeList.add(new CategoryUIModel(R.drawable.ic_bonus, "Thưởng"));
        incomeList.add(new CategoryUIModel(R.drawable.ic_business, "Kinh doanh"));
    }

    // Sửa lại cách khởi tạo adapter và thêm logic cập nhật UI cho tab
    private void loadExpenseList() {
        CategoryGridAdapter adapter = new CategoryGridAdapter(this, expenseList, category -> {
            Intent result = new Intent();
            result.putExtra("selectedCategoryName", category.getName());
            result.putExtra("selectedCategoryIcon", category.getIcon()); // Trả về cả icon
            setResult(RESULT_OK, result);
            finish();
        });

        rvCategories.setLayoutManager(new GridLayoutManager(this, 3));
        rvCategories.setAdapter(adapter);

        // Cập nhật giao diện tab
        tabExpense.setBackgroundResource(R.drawable.tab_active_bg);
        tabIncome.setBackgroundResource(R.drawable.tab_inactive_bg);
    }

    // Sửa lại cách khởi tạo adapter và thêm logic cập nhật UI cho tab
    private void loadIncomeList() {
        CategoryGridAdapter adapter = new CategoryGridAdapter(this, incomeList, category -> {
            Intent result = new Intent();
            result.putExtra("selectedCategoryName", category.getName());
            result.putExtra("selectedCategoryIcon", category.getIcon()); // Trả về cả icon
            setResult(RESULT_OK, result);
            finish();
        });

        rvCategories.setLayoutManager(new GridLayoutManager(this, 3));
        rvCategories.setAdapter(adapter);

        // Cập nhật giao diện tab
        tabIncome.setBackgroundResource(R.drawable.tab_active_bg);
        tabExpense.setBackgroundResource(R.drawable.tab_inactive_bg);
        // (chạy chương trình)

    }
}
