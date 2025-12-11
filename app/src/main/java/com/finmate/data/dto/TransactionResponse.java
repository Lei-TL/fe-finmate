package com.finmate.data.dto;

/**
 * DTO map với TransactionResponse từ BE.
 * Anh chỉnh field cho khớp với backend (id, amount, occurredAt...).
 */
public class TransactionResponse {

    private String id;
    private String categoryName;
    private String walletName;
    private double amount;
    private String type;       // "INCOME" / "EXPENSE" / "TRANSFER"
    private String occurredAt; // ISO datetime dạng String
    private String note;

    public TransactionResponse() {
    }

    public String getId() {
        return id;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public String getWalletName() {
        return walletName;
    }

    public double getAmount() {
        return amount;
    }

    public String getType() {
        return type;
    }

    public String getOccurredAt() {
        return occurredAt;
    }

    public String getNote() {
        return note;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public void setWalletName(String walletName) {
        this.walletName = walletName;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setOccurredAt(String occurredAt) {
        this.occurredAt = occurredAt;
    }

    public void setNote(String note) {
        this.note = note;
    }
}
