package com.finmate.data.dto;

import java.math.BigDecimal;

/**
 * DTO matching backend WalletRequest
 */
public class WalletRequest {
    private String name;
    private String type; // CASH, BANK, ...
    private String currency; // VND, USD...
    private BigDecimal initialBalance;
    private Boolean archived;
    private String color;

    public WalletRequest() {
    }

    public WalletRequest(String name, String type, String currency, BigDecimal initialBalance, Boolean archived, String color) {
        this.name = name;
        this.type = type;
        this.currency = currency;
        this.initialBalance = initialBalance;
        this.archived = archived;
        this.color = color;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public BigDecimal getInitialBalance() {
        return initialBalance;
    }

    public void setInitialBalance(BigDecimal initialBalance) {
        this.initialBalance = initialBalance;
    }

    public Boolean getArchived() {
        return archived;
    }

    public void setArchived(Boolean archived) {
        this.archived = archived;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }
}


