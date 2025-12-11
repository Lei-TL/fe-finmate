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

public class CategoryListAdapter extends RecyclerView.Adapter<CategoryListAdapter.ViewHolder> {

    public interface OnCategoryClickListener {
        void onCategoryClick(CategoryUIModel model);
    }

    private final Context context;
    private List<CategoryUIModel> list;
    private final OnCategoryClickListener listener;

    // Constructor đơn giản (không click)
    public CategoryListAdapter(Context context, List<CategoryUIModel> list) {
        this.context = context;
        this.list = list;
        this.listener = null;
    }

    // Constructor có click listener
    public CategoryListAdapter(Context context, List<CategoryUIModel> list, OnCategoryClickListener listener) {
        this.context = context;
        this.list = list;
        this.listener = listener;
    }

    // ⭐ UPDATE LIST an toàn – KHÔNG crash
    public void updateList(List<CategoryUIModel> newList) {
        this.list = newList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_category_list, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        CategoryUIModel model = list.get(position);

        // icon
        holder.imgIcon.setImageResource(model.getIcon());
        holder.imgIcon.setColorFilter(0xFF65C18C); // tint xanh FinMate

        // text
        holder.txtName.setText(model.getName());
        holder.txtName.setTextColor(0xFFFFFFFF); // trắng

        // click
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
