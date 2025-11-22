package com.finmate;



import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.finmate.adapters.CategoryGridAdapter;
import com.finmate.adapters.CategoryListAdapter;
import com.finmate.models.CategoryModel;

import java.util.ArrayList;
import java.util.List;

public class CategoryExpenseActivity extends AppCompatActivity {

    RecyclerView rvCategories;
    List<CategoryModel> list;
    CategoryGridAdapter adapterGrid;
    CategoryListAdapter adapterList;
    boolean isGrid = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category_expense);

        rvCategories = findViewById(R.id.rvCategories);

        loadExpenseCategories();
        showGrid();
    }

    private void loadExpenseCategories() {
        list = new ArrayList<>();
        list.add(new CategoryModel(R.drawable.ic_food, "Thực phẩm"));
        list.add(new CategoryModel(R.drawable.ic_drink, "Chế độ ăn"));
        list.add(new CategoryModel(R.drawable.ic_travel, "Di chuyển"));
    }

    private void showGrid() {
        adapterGrid = new CategoryGridAdapter(this, list);
        rvCategories.setLayoutManager(new GridLayoutManager(this, 3));
        rvCategories.setAdapter(adapterGrid);
        isGrid = true;
    }

    private void showList() {
        adapterList = new CategoryListAdapter(this, list);
        rvCategories.setLayoutManager(new LinearLayoutManager(this));
        rvCategories.setAdapter(adapterList);
        isGrid = false;
    }
}
