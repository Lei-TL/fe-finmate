package com.finmate.data.remote.dto;

import java.math.BigDecimal;

/**
 * DTO matching backend TransactionRequest
 */
public class CreateTransactionRequest {
    private String walletId;
    private String categoryId;
    private String type; // INCOME / EXPENSE / TRANSFER
    private BigDecimal amount;
    private String currency;
    private String occurredAt; // ISO datetime string
    private String note;
    private String transferRefId; // optional

    public CreateTransactionRequest() {
    }

    public CreateTransactionRequest(String walletId, String categoryId, String type, 
                                   BigDecimal amount, String currency, String occurredAt, 
                                   String note, String transferRefId) {
        this.walletId = walletId;
        this.categoryId = categoryId;
        this.type = type;
        this.amount = amount;
        this.currency = currency;
        this.occurredAt = occurredAt;
        this.note = note;
        this.transferRefId = transferRefId;
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
}
