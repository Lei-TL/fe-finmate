package com.finmate.data.remote.dto;

// Placeholder DTO
public class UpdateTransactionRequest {
    private String name;
    private String category;
    private double amount;

    public UpdateTransactionRequest(String name, String category, double amount) {
        this.name = name;
        this.category = category;
        this.amount = amount;
    }
}
