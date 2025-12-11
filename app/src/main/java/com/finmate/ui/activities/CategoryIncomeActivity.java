package com.finmate.ui.activities;

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
import com.finmate.ui.dialogs.AddCategoryDialog;
import com.finmate.ui.transaction.CategoryUIModel;
import com.finmate.ui.transaction.CategoryGridAdapter;
import com.finmate.ui.transaction.CategoryListAdapter;
import com.finmate.data.local.database.entity.CategoryEntity;
import com.finmate.data.repository.CategoryLocalRepository;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class CategoryIncomeActivity extends AppCompatActivity {

    @Inject
    CategoryLocalRepository categoryRepository;

    private RecyclerView rvCategories;
    private CategoryGridAdapter gridAdapter;
    private CategoryListAdapter listAdapter;

    private List<CategoryUIModel> incomeList = new ArrayList<>();
    private boolean isGrid = true;

    private ImageView btnBack, btnMore;
    private TextView tabExpense, tabIncome;
    private View btnAddNew;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category_income);

        initViews();
        setupGrid();      // mặc định dạng GRID
        handleEvents();
        loadCategoriesFromDB();
    }

    private void initViews() {
        rvCategories = findViewById(R.id.rvCategories);

        btnBack = findViewById(R.id.btnBack);
        btnMore = findViewById(R.id.btnMore);

        tabExpense = findViewById(R.id.tabExpense);
        tabIncome = findViewById(R.id.tabIncome);

        btnAddNew = findViewById(R.id.btnAddNewCategory);
    }

    private void loadCategoriesFromDB() {
        if (categoryRepository == null) return;

        categoryRepository.getByType("income").observe(this, entities -> {
            List<CategoryUIModel> uiModels = new ArrayList<>();
            for (CategoryEntity entity : entities) {
                uiModels.add(new CategoryUIModel(entity.getIconRes(), entity.getName()));
            }
            incomeList = uiModels;

            if (isGrid && gridAdapter != null) {
                gridAdapter.updateList(uiModels);
            } else if (!isGrid && listAdapter != null) {
                listAdapter.updateList(uiModels);
            }
        });
        categoryRepository.fetchRemoteCategoriesByType("income");
    }

    private void setupGrid() {
        isGrid = true;

        rvCategories.setLayoutManager(new GridLayoutManager(this, 3));
        gridAdapter = new CategoryGridAdapter(this, incomeList);
        rvCategories.setAdapter(gridAdapter);

        highlightTabs();
    }

    private void setupList() {
        isGrid = false;

        rvCategories.setLayoutManager(new LinearLayoutManager(this));
        listAdapter = new CategoryListAdapter(this, incomeList);
        rvCategories.setAdapter(listAdapter);

        highlightTabs();
    }

    private void highlightTabs() {
        tabIncome.setBackgroundResource(R.drawable.tab_active_bg);
        tabExpense.setBackgroundResource(R.drawable.tab_inactive_bg);

        tabIncome.setTextColor(getColor(R.color.white));
        tabExpense.setTextColor(getColor(R.color.black));
    }

    private void handleEvents() {

        btnBack.setOnClickListener(v -> finish());

        tabExpense.setOnClickListener(v -> {
            startActivity(new Intent(this, CategoryExpenseActivity.class));
            finish();
        });

        tabIncome.setOnClickListener(v -> { /* đang ở đây */ });

        btnMore.setOnClickListener(v -> openBottomSheet());

        btnAddNew.setOnClickListener(v -> openAddNewCategoryDialog());
    }

    private void openBottomSheet() {

        BottomSheetDialog dialog = new BottomSheetDialog(this);
        View view = getLayoutInflater().inflate(R.layout.bottom_sheet_category_options, null);

        TextView tvDelete = view.findViewById(R.id.tvDelete);
        TextView tvToggleView = view.findViewById(R.id.tvToggleView);
        TextView tvDuplicate = view.findViewById(R.id.tvDuplicate);
        TextView tvShare = view.findViewById(R.id.tvShare);
        TextView tvHelp = view.findViewById(R.id.tvHelp);

        // ⭐ Update text toggle đúng với chế độ hiện tại
        if (isGrid) {
            ((TextView) tvToggleView.findViewById(android.R.id.text1))
                    .setText("Hiển thị dạng danh sách");
        } else {
            ((TextView) tvToggleView.findViewById(android.R.id.text1))
                    .setText("Hiển thị dạng lưới");
        }

        // ⭐ Đổi chế độ GRID / LIST
        tvToggleView.setOnClickListener(v -> {
            if (isGrid) setupList();
            else setupGrid();
            dialog.dismiss();
        });

        tvDelete.setOnClickListener(v -> {
            Toast.makeText(this, "Chức năng xóa đang phát triển", Toast.LENGTH_SHORT).show();
            dialog.dismiss();
        });

        tvDuplicate.setOnClickListener(v -> {
            Toast.makeText(this, "Chức năng tạo bản sao đang phát triển", Toast.LENGTH_SHORT).show();
            dialog.dismiss();
        });

        tvShare.setOnClickListener(v -> {
            Toast.makeText(this, "Chia sẻ đang phát triển", Toast.LENGTH_SHORT).show();
            dialog.dismiss();
        });

        tvHelp.setOnClickListener(v -> {
            Toast.makeText(this, "Trợ giúp đang phát triển", Toast.LENGTH_SHORT).show();
            dialog.dismiss();
        });

        dialog.setContentView(view);
        dialog.show();
    }

    private void openAddNewCategoryDialog() {

        AddCategoryDialog dialog = new AddCategoryDialog(
                this,
                "income",
                (name, type) -> saveCategory(name, type)
        );

        dialog.show();
    }

    private void saveCategory(String name, String type) {

        CategoryEntity entity = new CategoryEntity(
                name,
                type,
                R.drawable.ic_default_category
        );

        if (categoryRepository != null) {
            categoryRepository.insert(entity);
        }

        Toast.makeText(this, "Đã thêm danh mục!", Toast.LENGTH_SHORT).show();
        loadCategoriesFromDB();
    }
}
