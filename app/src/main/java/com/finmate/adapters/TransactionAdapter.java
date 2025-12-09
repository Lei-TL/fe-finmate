package com.finmate.adapters;

import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.finmate.R;
import com.finmate.models.Transaction;

import java.util.List;

public class TransactionAdapter extends RecyclerView.Adapter<TransactionAdapter.TransactionViewHolder> {

    private List<Transaction> transactionList;
    private int longClickedPosition = -1;

    public TransactionAdapter(List<Transaction> transactionList) {
        this.transactionList = transactionList;
    }

    @NonNull
    @Override
    public TransactionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_transaction, parent, false);
        return new TransactionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TransactionViewHolder holder, int position) {
        Transaction transaction = transactionList.get(position);
        holder.tvName.setText(transaction.getName());
        holder.tvGroup.setText(transaction.getGroup());
        holder.tvMoney.setText(transaction.getMoney());
        holder.tvWallet.setText(transaction.getWallet());

        holder.itemView.setOnLongClickListener(v -> {
            setLongClickedPosition(holder.getAdapterPosition());
            return false; // return false to allow the context menu to be shown
        });
    }

    @Override
    public int getItemCount() {
        return transactionList.size();
    }

    public int getLongClickedPosition() {
        return longClickedPosition;
    }

    public void setLongClickedPosition(int position) {
        this.longClickedPosition = position;
    }

    public Transaction getTransactionAt(int position) {
        return transactionList.get(position);
    }

    // ViewHolder class
    public static class TransactionViewHolder extends RecyclerView.ViewHolder implements View.OnCreateContextMenuListener {
        TextView tvName, tvGroup, tvMoney, tvWallet;

        public TransactionViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvName);
            tvGroup = itemView.findViewById(R.id.tvGroup);
            tvMoney = itemView.findViewById(R.id.tvMoney);
            tvWallet = itemView.findViewById(R.id.tvWallet);
            itemView.setOnCreateContextMenuListener(this);
        }

        @Override
        public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
            // Inflate the menu from xml
            menu.add(this.getAdapterPosition(), R.id.action_edit, 0, R.string.edit);
            menu.add(this.getAdapterPosition(), R.id.action_delete, 1, R.string.delete);
        }
    }
}
