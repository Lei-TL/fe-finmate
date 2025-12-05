package com.finmate.ui.models;

// Model này chỉ chứa dữ liệu đã được định dạng cho tầng UI
public class TransactionUIModel {

    private final String name;
    private final String category;
    private final String amount;
    private final String wallet;
    private final String date;

    public TransactionUIModel(String name, String category, String amount, String wallet, String date) {
        this.name = name;
        this.category = category;
        this.amount = amount;
        this.wallet = wallet;
        this.date = date;
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
}
