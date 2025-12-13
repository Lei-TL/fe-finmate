package com.finmate.data.local.database.entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity(tableName = "wallets")
public class WalletEntity {

    @PrimaryKey
    @NonNull
    public String id; // Đổi từ int sang String để lưu UUID từ backend

    public String name;
    public String balance; // Formatted string for display (deprecated, giữ lại để backward compatibility)
    public double currentBalance; // ✅ Số dư hiện tại (từ current_balance)
    public double initialBalance; // ✅ Số dư ban đầu (từ initial_balance)
    public int iconRes;

    // ✅ Constructor chính cho Room (với đầy đủ fields)
    public WalletEntity(String id, String name, String balance, double currentBalance, double initialBalance, int iconRes) {
        this.id = id;
        this.name = name;
        this.balance = balance;
        this.currentBalance = currentBalance;
        this.initialBalance = initialBalance;
        this.iconRes = iconRes;
    }

    // ✅ Constructor cũ - đánh dấu @Ignore để Room không dùng
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
