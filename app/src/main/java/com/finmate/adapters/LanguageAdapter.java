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
import com.finmate.ui.models.LanguageUIModel;

import java.util.List;

public class LanguageAdapter extends RecyclerView.Adapter<LanguageAdapter.LanguageViewHolder> {

    private final Context context;
    private final List<LanguageUIModel> languageList;
    private final OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(int position);
    }

    public LanguageAdapter(Context context, List<LanguageUIModel> languageList, OnItemClickListener listener) {
        this.context = context;
        this.languageList = languageList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public LanguageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater
                .from(parent.getContext())
                .inflate(R.layout.item_language, parent, false);

        return new LanguageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull LanguageViewHolder holder, int position) {
        LanguageUIModel language = languageList.get(position);

        // Set tên + icon quốc kỳ
        holder.tvLanguageName.setText(language.getName());
        holder.imgFlag.setImageResource(language.getFlagResId());

        // Hiện hoặc ẩn tick
        holder.ivCheckmark.setVisibility(language.isSelected() ? View.VISIBLE : View.GONE);

        // Bắt sự kiện click
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onItemClick(position);
        });
    }

    @Override
    public int getItemCount() {
        return (languageList != null) ? languageList.size() : 0;
    }

    public static class LanguageViewHolder extends RecyclerView.ViewHolder {
        ImageView imgFlag, ivCheckmark;
        TextView tvLanguageName;

        public LanguageViewHolder(@NonNull View itemView) {
            super(itemView);
            imgFlag = itemView.findViewById(R.id.imgFlag);
            tvLanguageName = itemView.findViewById(R.id.tvLanguageName);
            ivCheckmark = itemView.findViewById(R.id.ivCheckmark);
        }
    }
}
