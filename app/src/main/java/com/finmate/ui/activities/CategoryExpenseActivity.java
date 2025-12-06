package com.finmate.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.finmate.R;
import com.finmate.ui.transaction.CategoryUIModel;
import com.finmate.ui.transaction.CategoryGridAdapter;
import com.finmate.ui.transaction.CategoryListAdapter;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.util.ArrayList;
import java.util.List;

public class CategoryExpenseActivity extends BaseActivity {

    RecyclerView rvCategories;
    List<CategoryUIModel> list = new ArrayList<>();

    CategoryGridAdapter adapterGrid;
    CategoryListAdapter adapterList;

    boolean isGrid = true;

    ImageView btnBack, btnMore;
    TextView tabExpense, tabIncome;
    LinearLayout layoutAddNew;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category_expense);

        initViews();
        loadExpenseCategories();
        showGrid();
        setupEvents();
    }

    private void initViews() {
        rvCategories = findViewById(R.id.rvCategories);
        btnBack = findViewById(R.id.btnBack);
        btnMore = findViewById(R.id.btnMore);
        tabExpense = findViewById(R.id.tabExpense);
        tabIncome = findViewById(R.id.tabIncome);
        layoutAddNew = findViewById(R.id.layoutAddNew);
    }

    private void loadExpenseCategories() {
        list.clear();
        list.add(new CategoryUIModel(R.drawable.ic_food, "Thực phẩm"));
        list.add(new CategoryUIModel(R.drawable.ic_eat, "Chế độ ăn"));
        list.add(new CategoryUIModel(R.drawable.ic_travel, "Di chuyển"));
        list.add(new CategoryUIModel(R.drawable.ic_fashion, "Thời trang"));
        list.add(new CategoryUIModel(R.drawable.ic_drink, "Chế độ uống"));
        list.add(new CategoryUIModel(R.drawable.ic_pet, "Thú cưng"));
        list.add(new CategoryUIModel(R.drawable.ic_education, "Giáo dục"));
        list.add(new CategoryUIModel(R.drawable.ic_health, "Sức khỏe"));
        list.add(new CategoryUIModel(R.drawable.ic_trip, "Du lịch"));
        list.add(new CategoryUIModel(R.drawable.ic_entertain, "Giải trí"));
        list.add(new CategoryUIModel(R.drawable.ic_waterbill, "Hóa đơn nước"));
        list.add(new CategoryUIModel(R.drawable.ic_electricbill, "Hóa đơn điện"));
        list.add(new CategoryUIModel(R.drawable.ic_bill, "Hóa đơn"));
        list.add(new CategoryUIModel(R.drawable.ic_gift, "Quà tặng"));
    }

    private void showGrid() {
        isGrid = true;

        rvCategories.setLayoutManager(new GridLayoutManager(this, 3));
        adapterGrid = new CategoryGridAdapter(this, list);
        rvCategories.setAdapter(adapterGrid);

        highlightTabs();
    }

    private void showList() {
        isGrid = false;

        rvCategories.setLayoutManager(new LinearLayoutManager(this));
        adapterList = new CategoryListAdapter(this, list);
        rvCategories.setAdapter(adapterList);

        highlightTabs();
    }

    private void highlightTabs() {
        tabExpense.setBackgroundResource(R.drawable.tab_active_bg);
        tabIncome.setBackgroundResource(R.drawable.tab_inactive_bg);

        tabExpense.setTextColor(getColor(R.color.white));
        tabIncome.setTextColor(getColor(R.color.gray));
    }

    private void setupEvents() {
        btnBack.setOnClickListener(v -> finish());

        tabIncome.setOnClickListener(v -> {
            startActivity(new Intent(this, CategoryIncomeActivity.class));
            finish();
        });

        btnMore.setOnClickListener(v -> showMoreMenu());

        layoutAddNew.setOnClickListener(v -> showAddNewCategoryDialog());
    }

    private void showMoreMenu() {
        BottomSheetDialog dialog = new BottomSheetDialog(this);
        View view = getLayoutInflater().inflate(R.layout.bottom_sheet_category_options, null);

        LinearLayout tvDelete = view.findViewById(R.id.tvDelete);
        LinearLayout tvToggleView = view.findViewById(R.id.tvToggleView);
        LinearLayout tvDuplicate = view.findViewById(R.id.tvDuplicate);
        LinearLayout tvShare = view.findViewById(R.id.tvShare);
        LinearLayout tvHelp = view.findViewById(R.id.tvHelp);

        TextView txtToggle = view.findViewById(R.id.tvToggleText);
        // nếu có text trong layout

        // set text toggle đúng
        if (txtToggle != null)
            txtToggle.setText(isGrid ? "Dạng danh sách" : "Dạng lưới");

        tvToggleView.setOnClickListener(v -> {
            if (isGrid) showList();
            else showGrid();
            dialog.dismiss();
        });

        tvDelete.setOnClickListener(v -> dialog.dismiss());
        tvDuplicate.setOnClickListener(v -> dialog.dismiss());
        tvShare.setOnClickListener(v -> dialog.dismiss());
        tvHelp.setOnClickListener(v -> dialog.dismiss());

        dialog.setContentView(view);
        dialog.show();
    }

    private void showAddNewCategoryDialog() {
        BottomSheetDialog dialog = new BottomSheetDialog(this);
        View view = getLayoutInflater().inflate(R.layout.dialog_add_category, null);

        TextView btnCancel = view.findViewById(R.id.btnCancel);
        TextView btnSave = view.findViewById(R.id.btnSave);

        btnCancel.setOnClickListener(v -> dialog.dismiss());
        btnSave.setOnClickListener(v -> dialog.dismiss());

        dialog.setContentView(view);
        dialog.show();
    }
}
