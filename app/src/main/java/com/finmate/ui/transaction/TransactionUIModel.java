package com.finmate.ui.transaction;

// Model này chỉ chứa dữ liệu đã được định dạng cho tầng UI
public class TransactionUIModel {

    private final String name;
    private final String category;
    private final String amount;
    private final String wallet;
    private final String date;
    private final String type; // "INCOME" / "EXPENSE" / "TRANSFER"

    public TransactionUIModel(String name, String category, String amount, String wallet, String date, String type) {
        this.name = name;
        this.category = category;
        this.amount = amount;
        this.wallet = wallet;
        this.date = date;
        this.type = type != null ? type : "EXPENSE";
    }

    // Getters
    public String getName() {
        return name;
    }

    public String getCategory() {
        return category;
    }

    public String getAmount() {
        return amount;
    }

    public String getWallet() {
        return wallet;
    }

    public String getDate() {
        return date;
    }

    public String getType() {
        return type;
    }
}
