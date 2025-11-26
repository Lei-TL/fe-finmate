package com.finmate.UI.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.finmate.R;
import com.finmate.adapters.CategoryGridAdapter;
import com.finmate.adapters.CategoryListAdapter;
import com.finmate.UI.dialogs.AddCategoryDialog;
import com.finmate.models.CategoryModel;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.util.ArrayList;

public class CategoryIncomeActivity extends AppCompatActivity {

    private RecyclerView rvCategories;
    private CategoryGridAdapter gridAdapter;
    private CategoryListAdapter listAdapter;

    private ArrayList<CategoryModel> incomeList;

    private boolean isGrid = true;

    private ImageView btnBack, btnMore;
    private TextView tabExpense, tabIncome;
    private View btnAddNew;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category_income);

        initViews();
        loadIncomeCategories();
        setupGrid();
        handleEvents();
    }

    private void initViews() {
        rvCategories = findViewById(R.id.rvIncomeCategories);

        btnBack = findViewById(R.id.btnBack);
        btnMore = findViewById(R.id.btnMore);

        tabExpense = findViewById(R.id.tabExpense);
        tabIncome = findViewById(R.id.tabIncome);

        btnAddNew = findViewById(R.id.btnAddIncomeCategory);
    }

    private void loadIncomeCategories() {
        incomeList = new ArrayList<>();

        incomeList.add(new CategoryModel(R.drawable.ic_salary, "Tiền lương"));
        incomeList.add(new CategoryModel(R.drawable.ic_bonus, "Tiền thưởng"));
        incomeList.add(new CategoryModel(R.drawable.ic_invest, "Tiền đầu tư"));
        incomeList.add(new CategoryModel(R.drawable.ic_other_income, "Tiền khác"));
    }

    // ================= GRID ====================
    private void setupGrid() {
        isGrid = true;

        rvCategories.setLayoutManager(new GridLayoutManager(this, 3));

        // FIX LỖI: TRUYỀN ĐỦ 2 THAM SỐ
        gridAdapter = new CategoryGridAdapter(this, incomeList);
        rvCategories.setAdapter(gridAdapter);

        tabIncome.setBackgroundResource(R.drawable.tab_active_bg);
        tabExpense.setBackgroundResource(R.drawable.tab_inactive_bg);
    }

    // ================= LIST ====================
    private void setupList() {
        isGrid = false;

        rvCategories.setLayoutManager(new LinearLayoutManager(this));

        // FIX LỖI: TRUYỀN ĐỦ 2 THAM SỐ
        listAdapter = new CategoryListAdapter(this, incomeList);
        rvCategories.setAdapter(listAdapter);

        tabIncome.setBackgroundResource(R.drawable.tab_active_bg);
        tabExpense.setBackgroundResource(R.drawable.tab_inactive_bg);
    }

    // ================= EVENTS ====================
    private void handleEvents() {

        btnBack.setOnClickListener(v -> finish());

        // Chuyển sang màn hình chi tiêu
        tabExpense.setOnClickListener(v -> {
            Intent intent = new Intent(this, CategoryExpenseActivity.class);
            startActivity(intent);
            finish();
        });

        tabIncome.setOnClickListener(v -> {}); // Đang ở tab hiện tại

        btnMore.setOnClickListener(v -> openBottomSheet());

        btnAddNew.setOnClickListener(v -> openAddNewCategoryDialog());
    }

    // ================= BOTTOM SHEET ====================
    private void openBottomSheet() {
        BottomSheetDialog dialog = new BottomSheetDialog(this);
        dialog.setContentView(R.layout.bottom_sheet_category_options);

        View btnDelete = dialog.findViewById(R.id.btnDelete);
        View btnList = dialog.findViewById(R.id.btnList);
        View btnGrid = dialog.findViewById(R.id.btnGrid);
        View btnDuplicate = dialog.findViewById(R.id.btnDuplicate);
        View btnShare = dialog.findViewById(R.id.btnShare);
        View btnHelp = dialog.findViewById(R.id.btnHelp);

        if (btnList != null) {
            btnList.setOnClickListener(v -> {
                setupList();
                dialog.dismiss();
            });
        }

        if (btnGrid != null) {
            btnGrid.setOnClickListener(v -> {
                setupGrid();
                dialog.dismiss();
            });
        }

        if (btnDelete != null)
            btnDelete.setOnClickListener(v ->
                    Toast.makeText(this, "Chức năng đang phát triển", Toast.LENGTH_SHORT).show());

        if (btnDuplicate != null)
            btnDuplicate.setOnClickListener(v ->
                    Toast.makeText(this, "Chức năng đang phát triển", Toast.LENGTH_SHORT).show());

        if (btnShare != null)
            btnShare.setOnClickListener(v ->
                    Toast.makeText(this, "Chức năng đang phát triển", Toast.LENGTH_SHORT).show());

        if (btnHelp != null)
            btnHelp.setOnClickListener(v ->
                    Toast.makeText(this, "Chức năng đang phát triển", Toast.LENGTH_SHORT).show());

        dialog.show();
    }

    // ================= ADD CATEGORY DIALOG ====================
    private void openAddNewCategoryDialog() {
        AddCategoryDialog dialog = new AddCategoryDialog(this);
        dialog.show();
    }
}
