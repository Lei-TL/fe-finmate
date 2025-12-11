package com.finmate.data.dto;

import java.math.BigDecimal;

/**
 * DTO map với TransactionResponse từ BE.
 * Updated to match backend structure.
 */
public class TransactionResponse {

    private String id;
    private String walletId;
    private String categoryId;
    private String type;       // "INCOME" / "EXPENSE" / "TRANSFER"
    private BigDecimal amount;
    private String currency;
    private String occurredAt; // ISO datetime string
    private String note;
    private String transferRefId;
    private boolean deleted;
    private String createdAt;
    private String updatedAt;

    public TransactionResponse() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getWalletId() {
        return walletId;
    }

    public void setWalletId(String walletId) {
        this.walletId = walletId;
    }

    public String getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(String categoryId) {
        this.categoryId = categoryId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getOccurredAt() {
        return occurredAt;
    }

    public void setOccurredAt(String occurredAt) {
        this.occurredAt = occurredAt;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public String getTransferRefId() {
        return transferRefId;
    }

    public void setTransferRefId(String transferRefId) {
        this.transferRefId = transferRefId;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }
}
