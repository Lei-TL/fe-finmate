package com.finmate.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.finmate.R;
import com.finmate.UI.models.CategoryUIModel;

import java.util.List;

public class  CategoryListAdapter extends RecyclerView.Adapter<CategoryListAdapter.ViewHolder> {

    private final Context context;
    private List<CategoryUIModel> list; // Giữ lại private để đảm bảo tính đóng gói

    public CategoryListAdapter(Context context, List<CategoryUIModel> list) {
        this.context = context;
        this.list = list;
    }

    // ⭐ THÊM PHƯƠNG THỨC UPDATE CÒN THIẾU
    public void updateList(List<CategoryUIModel> newList) {
        list.clear();
        list.addAll(newList);
        notifyDataSetChanged(); // Báo cho RecyclerView vẽ lại
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
        holder.imgIcon.setImageResource(model.getIcon());
        holder.txtName.setText(model.getName());
    }

    @Override
    public int getItemCount() {
        return list.size();
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
