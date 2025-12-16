package com.finmate.data.local.database.entity;

import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity(
    tableName = "transactions",
    indices = {
        @Index(value = {"date"}), // ✅ Index cho date column để query nhanh hơn
        @Index(value = {"type"}), // ✅ Index cho type column
        @Index(value = {"wallet"}), // ✅ Index cho wallet column
        @Index(value = {"remoteId"}) // ✅ Index cho remoteId column
    }
)
public class TransactionEntity {

    @PrimaryKey(autoGenerate = true)
    public int id;

    // ✅ Lưu ID từ backend để identify transaction khi sync (tránh duplicate)
    public String remoteId;

    public String name;
    public String category;
    public String amount; // Formatted string for display
    public String wallet;
    public String date;
    
    // ✅ Thêm field để tính toán
    public String type; // "INCOME" / "EXPENSE" / "TRANSFER"
    public double amountDouble; // Raw amount for calculations

    // ✅ Constructor chính cho Room (không có @Ignore)
    public TransactionEntity(String name, String category, String amount, String wallet, String date, String type, double amountDouble) {
        this.name = name;
        this.category = category;
        this.amount = amount;
        this.wallet = wallet;
        this.date = date;
        this.type = type != null ? type : "EXPENSE";
        this.amountDouble = amountDouble;
        this.remoteId = null; // Mặc định null cho transactions tạo local
    }
    
    // ✅ Constructor với remoteId (dùng khi sync từ backend)
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
    
    // ✅ Constructor cũ - đánh dấu @Ignore để Room không dùng
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
