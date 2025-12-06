package com.finmate.ui.transaction;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.finmate.R;

import java.util.List;

public class TransactionAdapter extends RecyclerView.Adapter<TransactionAdapter.TransactionViewHolder> {

    private List<TransactionUIModel> transactionList;

    public TransactionAdapter(List<TransactionUIModel> transactionList) {
        this.transactionList = transactionList;
    }

    // === PHƯƠNG THỨC ĐỂ CẬP NHẬT DỮ LIỆU ===
    public void updateList(List<TransactionUIModel> newList) {
        this.transactionList.clear();
        this.transactionList.addAll(newList);
        notifyDataSetChanged(); // Báo cho RecyclerView cập nhật lại
    }

    @NonNull
    @Override
    public TransactionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_transaction, parent, false);
        return new TransactionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TransactionViewHolder holder, int position) {
        TransactionUIModel transaction = transactionList.get(position);
        holder.tvName.setText(transaction.getName());
        holder.tvGroup.setText(transaction.getCategory());
        holder.tvMoney.setText(transaction.getAmount());
        holder.tvWallet.setText(transaction.getWallet());
    }

    @Override
    public int getItemCount() {
        return transactionList.size();
    }

    public static class TransactionViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvGroup, tvMoney, tvWallet;

        public TransactionViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvName);
            tvGroup = itemView.findViewById(R.id.tvGroup);
            tvMoney = itemView.findViewById(R.id.tvMoney);
            tvWallet = itemView.findViewById(R.id.tvWallet);
        }
    }
}
