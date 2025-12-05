package com.finmate.data.dto;

/**
 * DTO map với JSON từ BE /wallets.
 * Anh cần chỉnh field name cho khớp với WalletResponse bên BE.
 */
public class WalletResponse {

    private String id;
    private String name;
    private String currency;
    private double currentBalance;

    public WalletResponse() {
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getCurrency() {
        return currency;
    }

    public double getCurrentBalance() {
        return currentBalance;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public void setCurrentBalance(double currentBalance) {
        this.currentBalance = currentBalance;
    }
}
