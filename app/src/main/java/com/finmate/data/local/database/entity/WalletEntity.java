package com.finmate.data.local.database.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "wallets")
public class WalletEntity {

    @PrimaryKey(autoGenerate = true)
    public int id;

    public String name;
    public String balance;
    public int iconRes;

    public WalletEntity(String name, String balance, int iconRes) {
        this.name = name;
        this.balance = balance;
        this.iconRes = iconRes;
    }
}
