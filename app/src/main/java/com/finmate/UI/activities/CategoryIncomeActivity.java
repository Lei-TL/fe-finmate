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
import com.finmate.UI.dialogs.AddCategoryDialog;
import com.finmate.UI.models.CategoryUIModel;
import com.finmate.adapters.CategoryGridAdapter;
import com.finmate.adapters.CategoryListAdapter;
import com.finmate.entities.CategoryEntity;
import com.finmate.repository.CategoryRepository;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.util.ArrayList;
import java.util.List;

public class CategoryIncomeActivity extends AppCompatActivity {

    private RecyclerView rvCategories;
    private CategoryGridAdapter gridAdapter;
    private CategoryListAdapter listAdapter;

    private List<CategoryUIModel> incomeList = new ArrayList<>();
    private boolean isGrid = true;

    private ImageView btnBack, btnMore;
    private TextView tabExpense, tabIncome;
    private View btnAddNew;

    private CategoryRepository repo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category_income);

        repo = new CategoryRepository(this);

        initViews();
        setupGrid();   // Default dùng dạng Grid
        handleEvents();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadCategoriesFromDB();  // Tải lại dữ liệu khi quay lại màn hình
    }

    private void initViews() {
        rvCategories = findViewById(R.id.rvIncomeCategories);
        btnBack = findViewById(R.id.btnBack);
        btnMore = findViewById(R.id.btnMore);
        tabExpense = findViewById(R.id.tabExpense);
        tabIncome = findViewById(R.id.tabIncome);
        btnAddNew = findViewById(R.id.btnAddIncomeCategory);
    }

    // ================= LOAD CATEGORY FROM ROOM ====================
    private void loadCategoriesFromDB() {
        repo.getByType("income", entities -> {
            List<CategoryUIModel> uiModels = new ArrayList<>();

            for (CategoryEntity entity : entities) {
                uiModels.add(new CategoryUIModel(entity.iconRes, entity.name));
            }

            runOnUiThread(() -> {
                this.incomeList = uiModels;

                if (isGrid && gridAdapter != null) {
                    gridAdapter.updateList(uiModels);
                } else if (!isGrid && listAdapter != null) {
                    listAdapter.updateList(uiModels);
                }
            });
        });
    }

    // ================= GRID VIEW ====================
    private void setupGrid() {
        isGrid = true;
        rvCategories.setLayoutManager(new GridLayoutManager(this, 3));
        gridAdapter = new CategoryGridAdapter(this, incomeList);
        rvCategories.setAdapter(gridAdapter);

        tabIncome.setBackgroundResource(R.drawable.tab_active_bg);
        tabExpense.setBackgroundResource(R.drawable.tab_inactive_bg);
    }

    // ================= LIST VIEW ====================
    private void setupList() {
        isGrid = false;
        rvCategories.setLayoutManager(new LinearLayoutManager(this));
        listAdapter = new CategoryListAdapter(this, incomeList);
        rvCategories.setAdapter(listAdapter);

        tabIncome.setBackgroundResource(R.drawable.tab_active_bg);
        tabExpense.setBackgroundResource(R.drawable.tab_inactive_bg);
    }

    // ================= EVENTS ====================
    private void handleEvents() {
        btnBack.setOnClickListener(v -> finish());

        tabExpense.setOnClickListener(v -> {
            startActivity(new Intent(this, CategoryExpenseActivity.class));
            finish();
        });

        tabIncome.setOnClickListener(v -> {});

        btnMore.setOnClickListener(v -> openBottomSheet());
        btnAddNew.setOnClickListener(v -> openAddNewCategoryDialog());
    }

    // ================= BOTTOM SHEET ====================
    private void openBottomSheet() {
        BottomSheetDialog dialog = new BottomSheetDialog(this);
        dialog.setContentView(R.layout.bottom_sheet_category_options);

        View btnList = dialog.findViewById(R.id.btnList);
        if (btnList != null) btnList.setOnClickListener(v -> {
            setupList();
            dialog.dismiss();
        });

        View btnGrid = dialog.findViewById(R.id.btnGrid);
        if (btnGrid != null) btnGrid.setOnClickListener(v -> {
            setupGrid();
            dialog.dismiss();
        });

        View btnDelete = dialog.findViewById(R.id.btnDelete);
        if (btnDelete != null)
            btnDelete.setOnClickListener(v ->
                    Toast.makeText(this, "Chức năng đang phát triển", Toast.LENGTH_SHORT).show());

        dialog.show();
    }

    // ================= ADD NEW CATEGORY ====================
    private void openAddNewCategoryDialog() {
        AddCategoryDialog dialog = new AddCategoryDialog(
                this,
                "income", // loại danh mục
                (name, type) -> saveCategory(name, type)
        );
        dialog.show();
    }

    // ================= SAVE CATEGORY TO DB ====================
    private void saveCategory(String name, String type) {
        CategoryEntity entity = new CategoryEntity(
                name,
                type,
                R.drawable.ic_default_category
        );

        repo.insert(entity);

        Toast.makeText(this, "Đã thêm danh mục", Toast.LENGTH_SHORT).show();
        loadCategoriesFromDB();
    }
}
