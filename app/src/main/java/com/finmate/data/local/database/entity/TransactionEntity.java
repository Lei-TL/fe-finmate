package com.finmate.data.local.database.entity;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity(tableName = "transactions")
public class TransactionEntity {

    @PrimaryKey(autoGenerate = true)
    public int id;


    public String remoteId;

    public String name;
    public String category;
    public String amount;
    public String wallet;
    public String date;
    

    public String type;
    public double amountDouble;

    public TransactionEntity(String name, String category, String amount, String wallet, String date, String type, double amountDouble) {
        this.name = name;
        this.category = category;
        this.amount = amount;
        this.wallet = wallet;
        this.date = date;
        this.type = type != null ? type : "EXPENSE";
        this.amountDouble = amountDouble;
        this.remoteId = null;
    }
    
    @Ignore
    public TransactionEntity(String remoteId, String name, String category, String amount, String wallet, String date, String type, double amountDouble) {
        this.remoteId = remoteId;
        this.name = name;
        this.category = category;
        this.amount = amount;
        this.wallet = wallet;
        this.date = date;
        this.type = type != null ? type : "EXPENSE";
        this.amountDouble = amountDouble;
    }
    
    @Ignore
    public TransactionEntity(String name, String category, String amount, String wallet, String date) {
        this.name = name;
        this.category = category;
        this.amount = amount;
        this.wallet = wallet;
        this.date = date;
        this.type = "EXPENSE"; // Default
        this.amountDouble = 0.0;
    }
}
