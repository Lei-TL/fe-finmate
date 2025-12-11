package com.finmate.ui.transaction;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.finmate.R;

import java.util.List;

public class CategoryGridAdapter extends RecyclerView.Adapter<CategoryGridAdapter.ViewHolder> {

    public interface OnCategoryClickListener {
        void onCategoryClick(CategoryUIModel category);
    }

    private final Context context;
    private List<CategoryUIModel> list;
    private final OnCategoryClickListener listener;

    public CategoryGridAdapter(Context context, List<CategoryUIModel> list) {
        this.context = context;
        this.list = list;
        this.listener = null;
    }

    public CategoryGridAdapter(Context context, List<CategoryUIModel> list, OnCategoryClickListener listener) {
        this.context = context;
        this.list = list;
        this.listener = listener;
    }

    public void updateList(List<CategoryUIModel> newList) {
        this.list = newList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_category_grid, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CategoryUIModel model = list.get(position);

        // Icon
        holder.imgIcon.setImageResource(model.getIcon());

        // Text
        holder.txtName.setText(model.getName());

        // Tint icon theo màu FinMate
        holder.imgIcon.setColorFilter(0xFF65C18C); // màu xanh FinMate


        // Xử lý click
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onCategoryClick(model);
        });
    }

    @Override
    public int getItemCount() {
        return list == null ? 0 : list.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        ImageView imgIcon;
        TextView txtName;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            imgIcon = itemView.findViewById(R.id.imgIcon);
            txtName = itemView.findViewById(R.id.txtName);
        }
    }
}
