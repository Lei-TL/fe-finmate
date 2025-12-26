package com.finmate.data.dto;

/**
 * DTO map với TransactionResponse từ BE.
 * Anh chỉnh field cho khớp với backend (id, amount, occurredAt...).
 */
public class TransactionResponse {

    private String id;
    private String categoryId; // ✅ Backend trả về categoryId
    private String categoryName; // ✅ Có thể null nếu backend không trả về
    private String walletId; // ✅ Backend trả về walletId
    private String walletName; // ✅ Có thể null, sẽ tìm từ walletId
    private java.math.BigDecimal amount; // ✅ Backend trả về BigDecimal
    private String currency; // ✅ Backend trả về currency
    private String type;       // "INCOME" / "EXPENSE" / "TRANSFER"
    private String occurredAt; // ISO datetime dạng String (sẽ parse từ Instant)
    private String note;

    public TransactionResponse() {
    }

    public String getId() {
        return id;
    }

    public String getCategoryId() {
        return categoryId;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public String getWalletId() {
        return walletId;
    }

    public String getWalletName() {
        return walletName;
    }

    public java.math.BigDecimal getAmount() {
        return amount;
    }

    public String getCurrency() {
        return currency;
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

    public void setCategoryId(String categoryId) {
        this.categoryId = categoryId;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public void setWalletId(String walletId) {
        this.walletId = walletId;
    }

    public void setWalletName(String walletName) {
        this.walletName = walletName;
    }

    public void setAmount(java.math.BigDecimal amount) {
        this.amount = amount;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
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
