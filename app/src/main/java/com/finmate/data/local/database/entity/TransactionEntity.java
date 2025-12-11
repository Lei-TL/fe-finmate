package com.finmate.data.local.database.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "transactions")
public class TransactionEntity {

    @PrimaryKey(autoGenerate = true)
    public int id;

    public String name;
    public String category;
    public String amount;
    public String wallet;
    public String date;

    public TransactionEntity(String name, String category, String amount, String wallet, String date) {
        this.name = name;
        this.category = category;
        this.amount = amount;
        this.wallet = wallet;
        this.date = date;
    }
}
