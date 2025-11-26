package com.finmate.models;

public class Transaction {
    private String name;
    private String category;
    private String amount;
    private String wallet;
    private String date;

    public Transaction(String name, String category, String amount, String wallet, String date) {
        this.name = name;
        this.category = category;
        this.amount = amount;
        this.wallet = wallet;
        this.date = date;
    }

    public String getName() { return name; }
    public String getCategory() { return category; }
    public String getAmount() { return amount; }
    public String getWallet() { return wallet; }
    public String getDate() { return date; }
}
