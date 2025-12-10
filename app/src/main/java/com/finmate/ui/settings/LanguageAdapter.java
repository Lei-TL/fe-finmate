package com.finmate.ui.settings;

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

public class LanguageAdapter extends RecyclerView.Adapter<LanguageAdapter.LanguageViewHolder> {

    private Context context;
    private List<LanguageUIModel> languageList;
    private OnItemClickListener listener;
    private int selectedPosition = -1;

    public interface OnItemClickListener {
        void onItemClick(int position);
    }

    public LanguageAdapter(Context context, List<LanguageUIModel> languageList, OnItemClickListener listener) {
        this.context = context;
        this.languageList = languageList;
        this.listener = listener;
    }
    
    public void setSelectedPosition(int position) {
        int oldPosition = selectedPosition;
        selectedPosition = position;
        if (oldPosition != -1) notifyItemChanged(oldPosition);
        if (selectedPosition != -1) notifyItemChanged(selectedPosition);
    }

    @NonNull
    @Override
    public LanguageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_language, parent, false);
        return new LanguageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull LanguageViewHolder holder, int position) {
        LanguageUIModel language = languageList.get(position);
        holder.tvLanguageName.setText(language.getLanguageName());
        holder.imgFlag.setImageResource(language.getFlagRes());

        if (position == selectedPosition) {
            holder.ivCheckmark.setVisibility(View.VISIBLE);
        } else {
            holder.ivCheckmark.setVisibility(View.GONE);
        }

        holder.itemView.setOnClickListener(v -> {
            setSelectedPosition(position);
            if (listener != null) {
                listener.onItemClick(position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return languageList.size();
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
