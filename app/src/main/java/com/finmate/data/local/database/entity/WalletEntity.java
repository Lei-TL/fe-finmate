package com.finmate.data.local.database.entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity(tableName = "wallets")
public class WalletEntity {

    @PrimaryKey
    @NonNull
    public String id;

    public String name;
    public String balance;
    public double currentBalance;
    public double initialBalance;
    public int iconRes;

    public WalletEntity(String id, String name, String balance, double currentBalance, double initialBalance, int iconRes) {
        this.id = id;
        this.name = name;
        this.balance = balance;
        this.currentBalance = currentBalance;
        this.initialBalance = initialBalance;
        this.iconRes = iconRes;
    }

    @Ignore
    public WalletEntity(String id, String name, String balance, int iconRes) {
        this.id = id;
        this.name = name;
        this.balance = balance;
        this.iconRes = iconRes;
        this.currentBalance = 0.0;
        this.initialBalance = 0.0;
    }
}
