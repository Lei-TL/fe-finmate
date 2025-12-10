package com.finmate.data.remote.dto;

// This is a placeholder. You need to fill this with fields required by your backend API.
public class CreateTransactionRequest {
    private String name;
    private String category;
    private double amount;
    private String walletId;
    private String date;

    public CreateTransactionRequest(String name, String category, double amount, String walletId, String date) {
        this.name = name;
        this.category = category;
        this.amount = amount;
        this.walletId = walletId;
        this.date = date;
    }
}
